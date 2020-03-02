package ru.tinkoff.fintech.service.cashback

import ru.tinkoff.fintech.model.TransactionInfo
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.roundToLong

internal const val LOYALTY_PROGRAM_BLACK = "BLACK"
internal const val LOYALTY_PROGRAM_ALL = "ALL"
internal const val LOYALTY_PROGRAM_BEER = "BEER"
internal const val OLEGOV = "ОЛЕГОВ"
internal const val OLEG = "ОЛЕГ"
internal const val MAX_CASH_BACK = 3000.0
internal const val MCC_SOFTWARE = 5734
internal const val MCC_BEER = 5921
internal const val ZERO = 0.0
internal val MONTHS = mapOf(
    0 to "ЯНВАРЬ",
    1 to "ФЕВРАЛЬ",
    2 to "МАРТ",
    3 to "АПРЕЛЬ",
    4 to "МАЙ",
    5 to "ИЮНЬ",
    6 to "ИЮЛЬ",
    7 to "АВГУСТ",
    8 to "СЕНТЯБРЯ",
    9 to "ОКТЯБРЬ",
    10 to "НОЯБРЬ",
    11 to "ДЕКАБРЬ"
)

class CashbackCalculatorImpl : CashbackCalculator {

    override fun calculateCashback(transactionInfo: TransactionInfo): Double {
        val cashBackValue = when (transactionInfo.loyaltyProgramName) {
            LOYALTY_PROGRAM_BLACK -> calculateBlackProgram(transactionInfo)
            LOYALTY_PROGRAM_ALL -> calculateAllProgram(transactionInfo)
            LOYALTY_PROGRAM_BEER -> calculateBeerProgram(transactionInfo)
            else -> ZERO
        }

        val additionalCashBack = if (checkMultipleSum(transactionInfo.transactionSum, 666)) 6.66 else ZERO

        return checkCashBackTotalValue(transactionInfo.cashbackTotalValue, cashBackValue + additionalCashBack)
    }

    private fun calculateBlackProgram(transactionInfo: TransactionInfo) =
        calculateBySum(transactionInfo.transactionSum, 1.0)

    private fun calculateAllProgram(transactionInfo: TransactionInfo): Double {
        if (transactionInfo.mccCode == MCC_SOFTWARE && checkPalindrome(toPennyString(transactionInfo.transactionSum))) {
            return calculateBySum(
                transactionInfo.transactionSum,
                lcm(transactionInfo.firstName.length, transactionInfo.lastName.length).toDouble() / 1000
            )
        }
        return ZERO
    }

    private fun toPennyString(transactionSum: Double) = transactionSum.times(100).toInt().toString()

    private fun calculateBeerProgram(transactionInfo: TransactionInfo): Double {
        if (transactionInfo.mccCode == MCC_BEER) {
            val sum = transactionInfo.transactionSum
            val firstNameUC = transactionInfo.firstName.toUpperCase()
            return when {
                OLEG == firstNameUC && OLEGOV == transactionInfo.lastName.toUpperCase() -> calculateBySum(sum, 10.0)
                OLEG == firstNameUC -> calculateBySum(sum, 7.0)
                firstNameUC[0] == MONTHS[getOrdinalCurrentMonth()]?.get(0) -> calculateBySum(sum, 5.0)
                firstNameUC[0] == MONTHS[getOrdinalPreviousMonth()]?.get(0) || firstNameUC[0] == MONTHS[getOrdinalNextMonth()]?.get(
                    0
                ) -> calculateBySum(sum, 3.0)
                else -> calculateBySum(sum, 2.0)
            }
        }
        return ZERO
    }

    private fun checkPalindrome(str: String): Boolean {
        val n = str.length
        var count = 0
        for (i in 0 until n / 2) if (str[i] != str[n - i - 1]) ++count
        return count <= 1
    }

    private fun lcm(number1: Int, number2: Int): Int {
        if (number1 == 0 || number2 == 0) {
            return 0
        }
        val absNumber1 = abs(number1)
        val absNumber2 = abs(number2)
        val absHigherNumber = absNumber1.coerceAtLeast(absNumber2)
        val absLowerNumber = absNumber1.coerceAtMost(absNumber2)
        var lcm = absHigherNumber
        while (lcm % absLowerNumber != 0) {
            lcm += absHigherNumber
        }
        return lcm
    }

    private fun getOrdinalPreviousMonth(): Int {
        val currentMonth = getOrdinalCurrentMonth()
        return if (currentMonth == 0) 11 else currentMonth - 1
    }

    private fun getOrdinalNextMonth(): Int {
        val currentMonth = getOrdinalCurrentMonth()
        return if (currentMonth == 11) 0 else currentMonth + 1
    }

    private fun getOrdinalCurrentMonth() = LocalDate.now().month.ordinal

    private fun calculateBySum(transactionSum: Double, percent: Double) =
        transactionSum * (percent / 100)

    private fun checkCashBackTotalValue(totalValue: Double, value: Double): Double {
        val cashBack = if (totalValue + value > MAX_CASH_BACK) MAX_CASH_BACK - totalValue else value
        return (cashBack * 100).roundToLong() / 100.0
    }

    private fun checkMultipleSum(transactionSum: Double, number: Int) =
        transactionSum % number == 0.0

}