package ru.tinkoff.fintech.service.notification

import java.lang.Exception

class CardNumberMaskerImpl : CardNumberMasker {

    override fun mask(cardNumber: String, maskChar: Char, start: Int, end: Int): String {
        return when {
            end < start -> throw Exception()
            start == end -> cardNumber
            else -> cardNumber.toCharArray()
                .mapIndexed { index, c -> if (index in start until end) maskChar else c }
                .joinToString("")
        }
    }
}