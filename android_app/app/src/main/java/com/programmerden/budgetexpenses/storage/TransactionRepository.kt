package com.programmerden.budgetexpenses.storage

import com.programmerden.budgetexpenses.data.Transaction
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao
) {
    fun observeTransactions(): Flow<List<Transaction>> = transactionDao.observeTransactions()

    suspend fun insert(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    suspend fun update(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    suspend fun delete(transactionId: Int) {
        transactionDao.deleteById(transactionId)
    }

    suspend fun clearAll() {
        transactionDao.clearAll()
    }
}
