package com.programmerden.budgetexpenses.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.programmerden.budgetexpenses.data.Transaction
import com.programmerden.budgetexpenses.parser.SmsParser
import com.programmerden.budgetexpenses.storage.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val parser = SmsParser()

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        val pendingResult = goAsync()
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val dao = AppDatabase.getInstance(context).transactionDao()

        scope.launch {
            try {
                messages.forEach { sms ->
                    val parsed = parser.parse(sms.messageBody) ?: return@forEach

                    val transaction = Transaction(
                        amount = parsed.amount,
                        merchant = parsed.merchant,
                        timestamp = sms.timestampMillis,
                        parsedDate = parsed.parsedDate,
                        isCredit = parsed.isCredit
                    )

                    dao.insert(transaction)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
