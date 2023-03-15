package com.chen.beeaudio.utils

object TextLengthCounter {

    /* 计算长度 */
    fun lengthCounter(
        context : String
    ): Int{
        var valueLength: Float = 0f
        val chineseRegex = "[\u0391-\uFFE5]".toRegex()
        for (item in context) {
            valueLength += if (chineseRegex.containsMatchIn(input = item.toString())) {
                1f
            } else {
                0.5f
            }
        }
        return valueLength.toInt()
    }

}