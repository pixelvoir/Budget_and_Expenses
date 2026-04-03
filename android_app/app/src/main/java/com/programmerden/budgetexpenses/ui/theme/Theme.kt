package com.programmerden.budgetexpenses.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    background = Color(0xFFF4F6F8),
    onBackground = Color(0xFF10151D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF10151D),
    primary = Color(0xFF10151D),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF5A6472),
    onSecondary = Color(0xFFFFFFFF)
)

private val DarkColors = darkColorScheme(
    background = Color(0xFF0C1017),
    onBackground = Color(0xFFF6F7FA),
    surface = Color(0xFF141B24),
    onSurface = Color(0xFFF6F7FA),
    primary = Color(0xFFF6F7FA),
    onPrimary = Color(0xFF0C1017),
    secondary = Color(0xFFA9B3C4),
    onSecondary = Color(0xFF0C1017)
)

@Composable
fun BudgetExpensesTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
