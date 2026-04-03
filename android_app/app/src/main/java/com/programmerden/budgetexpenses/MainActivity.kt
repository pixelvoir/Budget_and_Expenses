package com.programmerden.budgetexpenses

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.programmerden.budgetexpenses.ui.screens.TransactionListScreen
import com.programmerden.budgetexpenses.ui.theme.BudgetExpensesTheme
import com.programmerden.budgetexpenses.ui.viewmodels.TransactionViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TransactionViewModel by viewModels()

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // UI is resilient either way; manual sample mode always remains available.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestSmsPermissionsIfNeeded()

        setContent {
            val transactions by viewModel.transactions.collectAsState()

            BudgetExpensesTheme {
                TransactionListScreen(
                    transactions = transactions,
                    onSampleSubmit = viewModel::parseAndStoreSampleSms
                )
            }
        }
    }

    private fun requestSmsPermissionsIfNeeded() {
        val readSmsGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED

        val receiveSmsGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (!readSmsGranted || !receiveSmsGranted) {
            smsPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_SMS,
                    Manifest.permission.RECEIVE_SMS
                )
            )
        }
    }
}
