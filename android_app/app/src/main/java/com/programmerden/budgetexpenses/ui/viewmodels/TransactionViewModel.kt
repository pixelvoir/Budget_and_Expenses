package com.programmerden.budgetexpenses.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.programmerden.budgetexpenses.data.Transaction
import com.programmerden.budgetexpenses.parser.SmsParser
import com.programmerden.budgetexpenses.storage.AppDatabase
import com.programmerden.budgetexpenses.storage.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TransactionRepository(
        AppDatabase.getInstance(application).transactionDao()
    )
    private val parser = SmsParser()

    val transactions: StateFlow<List<Transaction>> = repository.observeTransactions()
        .map { list -> list.sortedByDescending { it.timestamp } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun parseAndStoreSampleSms(text: String) {
        val parsed = parser.parse(text) ?: return

        val transaction = Transaction(
            amount = parsed.amount,
            merchant = parsed.merchant,
            timestamp = System.currentTimeMillis(),
            parsedDate = parsed.parsedDate
        )

        viewModelScope.launch {
            repository.insert(transaction)
        }
    }
}
