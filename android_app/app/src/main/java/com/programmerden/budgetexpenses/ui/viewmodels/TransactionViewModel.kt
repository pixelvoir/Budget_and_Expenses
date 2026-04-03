package com.programmerden.budgetexpenses.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.programmerden.budgetexpenses.R
import com.programmerden.budgetexpenses.data.Transaction
import com.programmerden.budgetexpenses.parser.SmsParser
import com.programmerden.budgetexpenses.storage.AppDatabase
import com.programmerden.budgetexpenses.storage.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TransactionRepository(
        AppDatabase.getInstance(application).transactionDao()
    )
    private val parser = SmsParser()
    private val context = application

    val transactions: StateFlow<List<Transaction>> = repository.observeTransactions()
        .map { list -> list.sortedByDescending { it.timestamp } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun parseAndStoreSampleSms(text: String) {
        val parsed = parser.parse(text) ?: return
        val merchant = parsed.merchant.trim()
        val mappedCategory = if (parsed.isCredit) null else getCategoryForMerchant(merchant)

        val transaction = Transaction(
            amount = parsed.amount,
            merchant = merchant,
            timestamp = System.currentTimeMillis(),
            parsedDate = parsed.parsedDate,
            isCredit = parsed.isCredit,
            category = mappedCategory
        )

        viewModelScope.launch {
            repository.insert(transaction)
        }
    }

    fun addManualTransaction(
        amount: Double,
        merchant: String,
        date: LocalDate,
        isCredit: Boolean,
        category: String? = null
    ) {
        val normalizedMerchant = merchant.trim()
        val mappedCategory = if (isCredit) null else category?.takeIf { it.isNotBlank() } ?: getCategoryForMerchant(normalizedMerchant)
        val timestamp = date
            .atTime(LocalTime.NOON)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val transaction = Transaction(
            amount = amount,
            merchant = normalizedMerchant,
            timestamp = timestamp,
            parsedDate = date.toString(),
            isCredit = isCredit,
            category = mappedCategory
        )

        viewModelScope.launch {
            repository.insert(transaction)
        }
    }

    fun loadSampleTransactionsFromJson() {
        viewModelScope.launch {
            try {
                val jsonString = context.resources.openRawResource(R.raw.transactions).bufferedReader().use { it.readText() }
                val jsonArray = Gson().fromJson(jsonString, JsonArray::class.java)
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val zoneId = ZoneId.systemDefault()

                for (element in jsonArray) {
                    val obj = element.asJsonObject
                    val amount = obj.get("amount").asDouble
                    val dateStr = obj.get("date").asString
                    val merchant = obj.get("merchant").asString
                    val isCredit = if (obj.has("isCredit")) obj.get("isCredit").asBoolean else false
                    val category = if (isCredit) null else if (obj.has("category")) obj.get("category").asString else getCategoryForMerchant(merchant)

                    val date = LocalDate.parse(dateStr, dateFormatter)
                    val timestamp = date.atStartOfDay(zoneId).toInstant().toEpochMilli()

                    val transaction = Transaction(
                        amount = amount,
                        merchant = merchant,
                        timestamp = timestamp,
                        parsedDate = dateStr,
                        isCredit = isCredit,
                        category = category
                    )

                    repository.insert(transaction)
                    if (category != null) {
                        mapMerchantToCategory(merchant, category)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun restoreSampleData() {
        viewModelScope.launch {
            repository.clearAll()
            loadSampleTransactionsFromJson()
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.update(transaction)
        }
    }

    fun deleteTransaction(transactionId: Int) {
        viewModelScope.launch {
            repository.delete(transactionId)
        }
    }

    fun clearAllTransactions() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    private val merchantCategoryPrefs by lazy {
        context.getSharedPreferences("merchant_categories", android.content.Context.MODE_PRIVATE)
    }

    private fun merchantKey(merchant: String): String {
        return merchant
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "")
    }

    fun mapMerchantToCategory(merchant: String, category: String) {
        merchantCategoryPrefs.edit().putString("category_${merchantKey(merchant)}", category).apply()
    }

    fun getCategoryForMerchant(merchant: String): String? {
        return merchantCategoryPrefs.getString("category_${merchantKey(merchant)}", null)
    }

    fun updateTransactionCategory(transactionId: Int, category: String) {
        viewModelScope.launch {
            val currentTransactions = transactions.value
            val transaction = currentTransactions.find { it.id == transactionId }
            if (transaction != null && !transaction.isCredit) {
                val updated = transaction.copy(category = category)
                repository.update(updated)
                if (category.isNotEmpty()) {
                    mapMerchantToCategory(transaction.merchant, category)
                }
            }
        }
    }

    fun getCategorySpending(transactions: List<Transaction>, monthYear: java.time.YearMonth? = null): Map<String, Double> {
        val debitTransactions = transactions.filterNot { it.isCredit }

        val filtered = if (monthYear != null) {
            debitTransactions.filter { transaction ->
                val transactionMonth = java.time.YearMonth.from(
                    java.time.Instant.ofEpochMilli(transaction.timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                )
                transactionMonth == monthYear
            }
        } else {
            debitTransactions
        }

        return filtered.groupBy { it.category ?: "Uncategorized" }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
    }
}
