package com.programmerden.budgetexpenses.parser

object RegexPatterns {
    val amountPatterns = listOf(
        Regex("Rs\\.?\\s*([\\d,]+(?:\\.\\d+)?)", RegexOption.IGNORE_CASE),
        Regex("INR\\s*([\\d,]+(?:\\.\\d+)?)", RegexOption.IGNORE_CASE),
        Regex("₹\\s*([\\d,]+(?:\\.\\d+)?)", RegexOption.IGNORE_CASE)
    )

    val datePatterns = listOf(
        Regex("on\\s*(\\d{1,2}-[A-Za-z]{3}-\\d{2,4})", RegexOption.IGNORE_CASE),
        Regex("on\\s*(\\d{1,2}/\\d{1,2}/\\d{2,4})", RegexOption.IGNORE_CASE),
        Regex("on\\s*(\\d{4}-\\d{2}-\\d{2})", RegexOption.IGNORE_CASE),
        Regex("on\\s*(\\d{1,2}-\\d{1,2}-\\d{2,4})", RegexOption.IGNORE_CASE),
        Regex("date\\s*(\\d{1,2}[A-Za-z]{3}\\d{2})", RegexOption.IGNORE_CASE)
    )

    val merchantPatterns = listOf(
        Regex(";\\s*([A-Za-z0-9&.\\*\\s]+)\\s+credited", RegexOption.IGNORE_CASE),
        Regex("(?:trf to|paid to|to)\\s*([A-Za-z0-9&.\\*\\s]+?)(?:\\.|\\s+on|\\s+via|\\s+Ref|$)", RegexOption.IGNORE_CASE),
        Regex("at\\s*([A-Za-z0-9&.\\*\\s]+?)(?:\\.|\\s+on|\\s+via|\\s+Ref|$)", RegexOption.IGNORE_CASE),
        Regex("for\\s*([A-Za-z0-9&.\\*\\s]+?)(?:\\.|\\s+on|\\s+via|\\s+Ref|$)", RegexOption.IGNORE_CASE)
    )
}
