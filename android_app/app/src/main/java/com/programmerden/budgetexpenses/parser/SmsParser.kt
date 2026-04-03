package com.programmerden.budgetexpenses.parser

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

data class ParsedTransaction(
    val amount: Double,
    val merchant: String,
    val parsedDate: String?,
    val isCredit: Boolean
)

class SmsParser(
    private val amountPatterns: List<Regex> = RegexPatterns.amountPatterns,
    private val creditMessagePatterns: List<Regex> = RegexPatterns.creditMessagePatterns,
    private val datePatterns: List<Regex> = RegexPatterns.datePatterns,
    private val creditMerchantPatterns: List<Regex> = RegexPatterns.creditMerchantPatterns,
    private val debitMerchantPatterns: List<Regex> = RegexPatterns.debitMerchantPatterns
) {

    private val dateFormats = listOf(
        DateTimeFormatter.ofPattern("d-MMM-yy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("d-MMM-yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("d/M/yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("d-M-yy", Locale.ENGLISH),
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("dMMMyy", Locale.ENGLISH)
    )

    fun extractAmount(text: String): Double? {
        for (pattern in amountPatterns) {
            val value = pattern.find(text)?.groupValues?.getOrNull(1) ?: continue
            return value.replace(",", "").toDoubleOrNull()
        }
        return null
    }

    fun extractDate(text: String): String? {
        for (pattern in datePatterns) {
            val value = pattern.find(text)?.groupValues?.getOrNull(1) ?: continue
            return normalizeDate(value)
        }
        return null
    }

    fun isCreditMessage(text: String): Boolean {
        return creditMessagePatterns.any { pattern -> pattern.containsMatchIn(text) }
    }

    fun extractMerchant(text: String, isCreditMessage: Boolean): String? {
        val patterns = if (isCreditMessage) creditMerchantPatterns else debitMerchantPatterns

        for (pattern in patterns) {
            val value = pattern.find(text)?.groupValues?.getOrNull(1)?.trim() ?: continue
            if (value.isNotEmpty()) return value
        }
        return null
    }

    fun parse(text: String): ParsedTransaction? {
        val amount = extractAmount(text)
        val parsedDate = extractDate(text)
        val isCredit = isCreditMessage(text)
        val merchant = extractMerchant(text, isCredit)

        if (amount == null || merchant == null) {
            return null
        }

        return ParsedTransaction(
            amount = amount,
            merchant = merchant,
            parsedDate = parsedDate,
            isCredit = isCredit
        )
    }

    private fun normalizeDate(input: String): String? {
        for (format in dateFormats) {
            try {
                return LocalDate.parse(input, format).toString()
            } catch (_: DateTimeParseException) {
                // Try next format
            }
        }
        return input
    }
}
