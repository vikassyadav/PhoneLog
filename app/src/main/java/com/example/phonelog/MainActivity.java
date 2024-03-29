package com.example.phonelog;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.Manifest;
import android.util.Log;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG_SEND_DATA = "Sending data to server";
    private ArrayList<Model> callLogModelArrayList;
    private RecyclerView rv_call_logs;
    private Adapter callLogAdapter;

    public String str_number, str_contact_name, str_call_type, str_call_full_date,
            str_call_date, str_call_time, str_call_time_formatted, str_call_duration;

    private SwipeRefreshLayout swipeRefreshLayout;

    // Request code. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_CODE = 999;
    private String[] appPermissions = {
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.READ_PHONE_STATE
    };
    private int flag = 0;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize our views and variables
        Init();

        //check for permission
        if(CheckAndRequestPermission()){
            FetchCallLogs();
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //check for permission
                if(CheckAndRequestPermission()){
                    FetchCallLogs();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });

//        SettingUpPeriodicWork();
    }
    public boolean CheckAndRequestPermission() {
        //checking which permissions are granted
        List<String> listPermissionNeeded = new ArrayList<>();
        for (String item: appPermissions){
            if(ContextCompat.checkSelfPermission(this, item)!= PackageManager.PERMISSION_GRANTED)
                listPermissionNeeded.add(item);
        }

        //Ask for non-granted permissions
        if (!listPermissionNeeded.isEmpty()){
            ActivityCompat.requestPermissions(this, listPermissionNeeded.toArray(new String[listPermissionNeeded.size()]),
                    PERMISSIONS_REQUEST_CODE);
            return false;
        }
        //App has all permissions. Proceed ahead
        return true;
    }

    private void Init() {
        swipeRefreshLayout = findViewById(R.id.activity_main_swipe_refresh_layout);
        rv_call_logs = findViewById(R.id.activity_main_rv);
        rv_call_logs.setHasFixedSize(true);
        rv_call_logs.setLayoutManager(new LinearLayoutManager(this));
        callLogModelArrayList = new ArrayList<>();
        callLogAdapter = new Adapter(this, callLogModelArrayList);
        rv_call_logs.setAdapter(callLogAdapter);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if(grantResults[i]==PackageManager.PERMISSION_DENIED){
                    flag = 1;
                    break;
                }
            }
            if (flag==0)
                FetchCallLogs();
        }
    }

    @SuppressLint({"Range", "NotifyDataSetChanged"})
    public void FetchCallLogs() {
        // reading all data in descending order according to DATE
        String sortOrder = android.provider.CallLog.Calls.DATE + " DESC LIMIT 20";

        Cursor cursor = this.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                null,
                null,
                sortOrder);

        //clearing the arraylist
        callLogModelArrayList.clear();

        //looping through the cursor to add data into arraylist
        while (cursor.moveToNext()){
            str_number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            str_contact_name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
            str_contact_name = str_contact_name==null || str_contact_name.equals("") ? "Unknown" : str_contact_name;
            str_call_type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
            str_call_full_date = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));
            str_call_duration = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION));

            SimpleDateFormat dateFormatter = new SimpleDateFormat(
                    "dd MMM yyyy");
            str_call_date = dateFormatter.format(new Date(Long.parseLong(str_call_full_date)));

            SimpleDateFormat timeFormatter = new SimpleDateFormat(
                    "HH:mm:ss");
            str_call_time = timeFormatter.format(new Date(Long.parseLong(str_call_full_date)));

            //str_call_time = getFormatedDateTime(str_call_time, "HH:mm:ss", "hh:mm ss");

            str_call_duration = DurationFormat(str_call_duration);

            switch(Integer.parseInt(str_call_type)){
                case CallLog.Calls.INCOMING_TYPE:
                    str_call_type = "Incoming";
                    break;
                case CallLog.Calls.OUTGOING_TYPE:
                    str_call_type = "Outgoing";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    str_call_type = "Missed";
                    break;
                case CallLog.Calls.VOICEMAIL_TYPE:
                    str_call_type = "Voicemail";
                    break;
                case CallLog.Calls.REJECTED_TYPE:
                    str_call_type = "Rejected";
                    break;
                case CallLog.Calls.BLOCKED_TYPE:
                    str_call_type = "Blocked";
                    break;
                case CallLog.Calls.ANSWERED_EXTERNALLY_TYPE:
                    str_call_type = "Externally Answered";
                    break;
                default:
                    str_call_type = "NA";
            }

            Model callLogItem = new Model(str_number, str_contact_name, str_call_type,
                    str_call_date, str_call_time, str_call_duration);

            callLogModelArrayList.add(callLogItem);
            SendDataToServer(callLogItem);
        }
        callLogAdapter.notifyDataSetChanged();
    }

    private String DurationFormat(String duration) {
        String durationFormatted=null;
        if(Integer.parseInt(duration) < 60){
            durationFormatted = duration+" Sec";
        }
        else{
            int min = Integer.parseInt(duration)/60;
            int sec = Integer.parseInt(duration)%60;

            if(sec==0)
                durationFormatted = min + " Min" ;
            else
                durationFormatted = min + " Min " + sec + " Sec";

        }
        return durationFormatted;
    }

    private void SendDataToServer(Model callLogItem) {
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference("CallLog")
//                .child(getDeviceName())
//                .child(callLogItem.getCallDate())
//                .child(callLogItem.getCallTime());
//        myRef.setValue(callLogItem);

        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Define a collection reference for the "CallLogs" collection
        CollectionReference callLogCollection = db.collection("CallLogs");

        // Query the existing data to determine the number of documents
        callLogCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null) {
                        int documentCount = querySnapshot.size();

                        // If there are more than 20 documents, delete excess documents
                        if (documentCount > 20) {
                            List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                            for (int i = 0; i < documentCount - 20; i++) {
                                DocumentSnapshot documentToDelete = documents.get(i);
                                callLogCollection.document(documentToDelete.getId()).delete();
                            }
                        }
                    }

                    // Create a new document reference for the new data
                    // The document ID can be a combination of device name, call date, and call time
                    DocumentReference newCallLogRef = callLogCollection
                            .document(getDeviceName() + "_" + callLogItem.getCallDate() + "_" + callLogItem.getCallTime());

                    // Set the data of the document to the callLogItem object
                    newCallLogRef.set(callLogItem,SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Data successfully written to Firestore
                                    Log.d(TAG_SEND_DATA, "Call log data added to Firestore");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle errors writing data to Firestore
                                    Log.e(TAG_SEND_DATA, "Error adding call log data to Firestore", e);
                                }
                            });
                } else {
                    // Handle failures while querying Firestore
                    Log.e(TAG_SEND_DATA, "Error querying Firestore", task.getException());
                }
            }
        });


//        myRef.setValue(callLogItem)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        // Data sent successfully, now fetch the data
//                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                // Clear existing data
//                                callLogModelArrayList.clear();
//                                // Iterate through dataSnapshot to fetch data
//                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                                    Model retrievedCallLog = snapshot.getValue(Model.class);
//                                    if (retrievedCallLog != null) {
//                                        // Add fetched data to the list
//                                        callLogModelArrayList.add(retrievedCallLog);
//                                    }
//                                }
//                                // Notify the adapter about the changes
//                                callLogAdapter.notifyDataSetChanged();
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//                                // Handle error
//                                Log.e(TAG_SEND_DATA, "Data fetch cancelled: " + databaseError.getMessage());
//                            }
//                        });
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        // Handle failure
//                        Log.e(TAG_SEND_DATA, "Failed to send data: " + e.getMessage());
//                    }
//                });
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}