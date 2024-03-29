# Fetching Call Log from Android Phone 

## Introduction

This Android application allows users to fetch call logs from their device and display them in a list. It provides details such as phone number, contact name (if available), call type, date, time, and duration of each call.

## Prerequisites

Before running the application, ensure that you have the following prerequisites:

1. **Android Studio**: Install Android Studio on your development machine.
2. **Android Device**: Connect an Android device to your computer via USB debugging mode or use an Android emulator.
3. **Permissions**: The application requires the following permissions to access call logs:
   - `READ_CALL_LOG`: Allows reading the call log.
   - `PROCESS_OUTGOING_CALLS`: Allows processing outgoing calls.
   - `READ_PHONE_STATE`: Allows reading phone state, including the current call state.

## Installation and Setup

Follow these steps to install and set up the application:

1. Clone the repository to your local machine:

    ```bash 
    git clone https://github.com/vikassyadav/PhoneLog
    ```

2. Open the project in Android Studio.

3. Connect your Android device to your computer or start an Android emulator.

4. Build and run the application on your device/emulator.

5. Grant the necessary permissions when prompted.

## Usage

Once the application is installed and running on your device, follow these steps to fetch and display call logs:

1. Launch the application from your device's app drawer.

2. The application will request permission to access call logs. Grant the necessary permissions when prompted.

3. Once permissions are granted, the application will fetch the call logs from your device and display them in a list.

4. You can swipe down to refresh the call logs list.

## Required Methods to Fetch Call Logs

The application uses the following methods to fetch call logs:

1. **getContentResolver().query(Uri, String[], String, String[], String)**:
   - This method retrieves call logs from the `CallLog.Calls.CONTENT_URI`.
   - It queries the call log database and returns a Cursor containing the results.

2. **Cursor.moveToNext()**:
   - This method moves the Cursor to the next row.
   - It iterates through the Cursor to read each call log entry.

3. **Cursor.getString(int)**:
   - This method retrieves the value of the specified column as a String.
   - It extracts various call log details such as phone number, contact name, call type, date, time, and duration.

4. **SimpleDateFormat**:
   - This class is used to format dates and times.
   - It formats the call date and time into human-readable formats.

5. **Model** (Custom Class):
   - This class represents a single call log entry.
   - It encapsulates call log details such as phone number, contact name, call type, date, time, and duration.

6. **ArrayList.add(E)**:
   - This method adds an object to the ArrayList.
   - It populates an ArrayList with Model objects representing call log entries.

7. **Adapter.notifyDataSetChanged()**:
   - This method notifies the RecyclerView Adapter that the underlying dataset has changed.
   - It updates the UI with the fetched call logs.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

[Vikas Yadav ](https://github.com/vikassyadav)
