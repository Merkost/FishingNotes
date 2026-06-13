package com.mobileprism.fishing.ui.home.new_catch

object CatchSummaryFormatter {

    fun format(
        fishName: String,
        amount: Int,
        weight: Double,
        kgSuffix: String,
        countTemplate: String,
        separator: String,
    ): String {
        val builder = StringBuilder(fishName)
        if (amount > 0) {
            if (builder.isNotEmpty()) builder.append(' ')
            builder.append(formatCount(amount, countTemplate))
        }
        if (weight > 0.0) {
            if (builder.isNotEmpty()) builder.append(separator)
            builder.append(formatWeight(weight)).append(kgSuffix)
        }
        return builder.toString()
    }

    fun formatWeight(weight: Double): String {
        val rounded = (weight * 10.0).toLong() / 10.0
        return if (rounded % 1.0 == 0.0) {
            rounded.toLong().toString()
        } else {
            rounded.toString()
        }
    }

    private fun formatCount(amount: Int, countTemplate: String): String =
        countTemplate.replace("%1\$d", amount.toString())
}
