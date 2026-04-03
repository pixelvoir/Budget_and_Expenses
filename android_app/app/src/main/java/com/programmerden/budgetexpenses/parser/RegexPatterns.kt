package com.programmerden.budgetexpenses.parser

object RegexPatterns {
    val amountPatterns = listOf(
        Regex("Rs\\.?\\s*([\\d,]+(?:\\.\\d+)?)", RegexOption.IGNORE_CASE),
        Regex("INR\\s*([\\d,]+(?:\\.\\d+)?)", RegexOption.IGNORE_CASE),
        Regex("₹\\s*([\\d,]+(?:\\.\\d+)?)", RegexOption.IGNORE_CASE)
    )

    val creditMessagePatterns = listOf(
        Regex("\\bcredited\\s+with\\b", RegexOption.IGNORE_CASE),
        Regex("\\bcredited\\s+to\\b", RegexOption.IGNORE_CASE),
        Regex("\\bis\\s+credited\\b", RegexOption.IGNORE_CASE)
    )

    val datePatterns = listOf(
        Regex("on\\s*(\\d{1,2}-[A-Za-z]{3}-\\d{2,4})", RegexOption.IGNORE_CASE),
        Regex("on\\s*(\\d{1,2}/\\d{1,2}/\\d{2,4})", RegexOption.IGNORE_CASE),
        Regex("on\\s*(\\d{4}-\\d{2}-\\d{2})", RegexOption.IGNORE_CASE),
        Regex("on\\s*(\\d{1,2}-\\d{1,2}-\\d{2,4})", RegexOption.IGNORE_CASE),
        Regex("(?:on|date|value\\s*date)\\s*(\\d{1,2}/[A-Za-z]{3}/\\d{2,4})", RegexOption.IGNORE_CASE),
        Regex("date\\s*(\\d{1,2}[A-Za-z]{3}\\d{2})", RegexOption.IGNORE_CASE)
    )

    val creditMerchantPatterns = listOf(
        // Generic credit hook for bank credits that say the source is introduced with "from".
        Regex("\\bfrom\\s+([^\\.\\r\\n]+?)(?=\\.|\\s+UPI|\\s+IMPS|\\s+Ref\\b|\\s+on\\b|$)", RegexOption.IGNORE_CASE),
        // Generic credit hook for bank credits that say the source is introduced with "by".
        Regex("\\bby\\s+([^\\.\\r\\n]+?)(?=\\.|\\s+UPI|\\s+IMPS|\\s+Ref\\b|\\s+on\\b|$)", RegexOption.IGNORE_CASE),
        // Fallback for "credited to" messages with account info (when no sender is mentioned).
        Regex("credited\\s+to\\s+([A-Za-z0-9\\s\\-]+?)(?=\\s+(?:on|AC|$)|\\.|Ref\\b)", RegexOption.IGNORE_CASE)
    )

    val debitMerchantPatterns = listOf(
        // Axis Bank UPI format: UPI/P2P or UPI/P2M/.../Merchant
        Regex("UPI/(?:P2P|P2M)/[^/]+/([A-Za-z0-9&.\\s*\\-]+?)(?:\\r?\\n|\\.|$)", RegexOption.IGNORE_CASE),
        // ICICI Bank credit card debit format: ... debited for INR ... for UPI-<id>-Merchant
        Regex("for\\s+UPI-\\d+-([A-Za-z0-9&.\\s*\\-]+?)(?:\\.|\\s+To\\s+dispute|\\s+and\\s+this|$)", RegexOption.IGNORE_CASE),
        Regex(";\\s*([A-Za-z0-9&.\\*\\s]+)\\s+credited", RegexOption.IGNORE_CASE),
        // UPI debit with email/UPI ID: to [merchant@bank] or similar (includes @ for email addresses)
        Regex("(?:trf to|paid to|to)\\s*([A-Za-z0-9@&.\\s*\\-]+?)(?:\\.|\\s+on|\\s+via|\\s+Ref|$)", RegexOption.IGNORE_CASE),
        Regex("at\\s*([A-Za-z0-9@&.\\s*\\-]+?)(?:\\.|\\s+on|\\s+via|\\s+Ref|$)", RegexOption.IGNORE_CASE),
        Regex("for\\s*([A-Za-z0-9@&.\\s*\\-]+?)(?:\\.|\\s+on|\\s+via|\\s+Ref|$)", RegexOption.IGNORE_CASE)
    )
}
