# SMS-Based Expense Tracker (Android)

Application that can automatically read, store and display your daily expenses in a user-friendly format, used to keep track of your budget for people like me who make frequent, small purchases that add up and quietly spiral out of control :D

## 1. How To Run And Implement

### Prerequisites
- Android Studio (latest stable)
- Android SDK 34
- JDK 17
- Kotlin + Gradle plugins (handled by project files)

### Project Structure (Android)
- Root Android project: `android_app/`
- App module: `android_app/app/`

### A) Run On Windows Emulator
1. Open Android Studio and select `android_app` as the project root.
2. Let Gradle sync complete.
3. Open AVD Manager and create a virtual device (Pixel + Android 13/14 image).
4. Start the emulator.
5. Run the `app` configuration.
6. Grant SMS permissions when prompted.

Note: Emulators may not reliably receive real carrier SMS. Use manual sample mode in the app UI for parser testing.

### B) Run On Actual Android Phone
1. Enable Developer Options and USB debugging on the phone.
2. Connect phone to Windows via USB and trust the computer.
3. In Android Studio, select your physical device and run `app`.
4. Grant SMS permissions (`READ_SMS`, `RECEIVE_SMS`).
5. Send a test transaction SMS to the phone and verify it appears in the app.

## 2. Current Features

- Incoming SMS processing through BroadcastReceiver.
- Parser-first modular architecture:
	- `parser/RegexPatterns.kt`
	- `parser/SmsParser.kt`
- Multiple regex patterns per field for extensibility.
- Extraction only if both amt and merchant found (date is optional).
- Timestamp source of truth:
	- Uses device SMS `timestampMillis` for the time/date at which transaction occured
	- Parsed date is optional metadata only.
- Room database storage:
	- `transactions` table with indexed `timestamp`.
	- Sort order is latest first (`timestamp DESC`).
- Compose UI:
	- Transaction list screen.
	- Manual sample SMS input mode for debugging parser behavior without real SMS.

## 3. Planned Features

- Sender/bank filtering to reduce unnecessary background parsing.
- Better regex and parsing for diverse SMS templates.
- Transaction direction detection (debit vs credit).
- Category classifier integration (future ML/TFLite component).
- Analytics screens:
	- Monthly summaries
	- Category breakdown
	- Spending trends

## 4. Known Issues

- Debit vs credit intent is not yet reliably classified.
- Merchant extraction regex is not the most consistent, considering the variety in the banks' style of messages..
- Emulator SMS behavior may vary, so real-device testing is preferred for receiver validation.
