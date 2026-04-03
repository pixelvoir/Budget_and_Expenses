package com.programmerden.budgetexpenses

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.programmerden.budgetexpenses.ui.screens.TransactionListScreen
import com.programmerden.budgetexpenses.ui.theme.BudgetExpensesTheme
import com.programmerden.budgetexpenses.ui.viewmodels.TransactionViewModel

class MainActivity : ComponentActivity() {

    private val themePrefs by lazy { getSharedPreferences("budget_expenses_prefs", Context.MODE_PRIVATE) }

    private val viewModel: TransactionViewModel by viewModels()

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // UI is resilient either way; manual sample mode always remains available.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestSmsPermissionsIfNeeded()

        // Load sample transactions on first launch
        if (!themePrefs.getBoolean("sample_data_loaded", false)) {
            viewModel.loadSampleTransactionsFromJson()
            themePrefs.edit().putBoolean("sample_data_loaded", true).apply()
        }

        setContent {
            val transactions by viewModel.transactions.collectAsState()
            var darkTheme by remember {
                mutableStateOf(themePrefs.getBoolean("dark_theme", false))
            }

            BudgetExpensesTheme(darkTheme = darkTheme) {
                TransactionListScreen(
                    transactions = transactions,
                    darkTheme = darkTheme,
                    onThemeToggle = { enabled ->
                        darkTheme = enabled
                        themePrefs.edit().putBoolean("dark_theme", enabled).apply()
                    },
                    onUpdateTransaction = viewModel::updateTransaction,
                    onAddManualTransaction = { amount, merchant, date, isCredit, category ->
                        viewModel.addManualTransaction(amount, merchant, date, isCredit, category)
                    },
                    onDeleteTransaction = viewModel::deleteTransaction,
                    onClearAllData = viewModel::clearAllTransactions,
                    onRestoreSampleData = viewModel::restoreSampleData,
                    viewModel = viewModel
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
