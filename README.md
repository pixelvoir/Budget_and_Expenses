# SMS-Based Expense Tracker (Android)

Application that can automatically read, store and display your daily expenses in a user-friendly format, used to keep track of your budget for people like me who make frequent, small purchases that add up and quietly spiral out of control :D

## 1. How To Run And Implement

### Prerequisites
- Android Studio (latest stable)
- Android SDK 34
- JDK 17
- Kotlin + Gradle plugins (handled by project files)

## Run On An Android Phone
1. Enable Developer Options and USB debugging on the phone(can use wireless too).
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
- Trends like total amount spent per day or per month, avg amount spent in a week or by month, etc
- Persistent manual categorisation, most reliable and remembers the category when assigned once, subsequently shows category based trends.

## 3. Planned Features

- Sender/bank filtering to reduce unnecessary background parsing.
- Better regex and parsing for diverse SMS templates.
- Category classifier integration (first manual, then in the future ML/TFLite component).
- Better Analytics

## 4. Known Issues

- Merchant extraction regex is not the most consistent, considering the variety in the banks' style of messages.
- Merchant category classification may yet prove to be difficult given not all merchant names accurately reflect their products/category, a manual persistent categorisation is enough tbh
- Right now goes through each and every SMS received, clearly not the most efficient
