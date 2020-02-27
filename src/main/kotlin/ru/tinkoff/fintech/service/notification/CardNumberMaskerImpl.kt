package ru.tinkoff.fintech.service.notification

class CardNumberMaskerImpl : CardNumberMasker {

    override fun mask(cardNumber: String, maskChar: Char, start: Int, end: Int): String =
        when {
            end < start -> throw IllegalArgumentException()
            start == end -> cardNumber
            else -> cardNumber
                .mapIndexed { index, c -> if (index in start until end) maskChar else c }
                .joinToString("")
        }
}