package com.hyphenate.chatdemo.common.extensions.internal

import android.os.CountDownTimer
import android.widget.TextView
import androidx.core.content.ContextCompat

internal fun TextView.setCountDown(
    millisInFuture: Long,
    countDownInterval: Long,
    goingText: Int,
    endText: Int,
    goingColor: Int,
    endColor: Int
) {
    object : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onTick(millisUntilFinished: Long) {
            isClickable = false
            text = context.getString(
                goingText,
                (millisUntilFinished / 1000).toInt()
            )
            setTextColor(
                ContextCompat.getColor(
                    context,
                    goingColor
                )
            )
        }

        override fun onFinish() {
            text = context.getString(endText)
            setTextColor(
                ContextCompat.getColor(
                    context,
                    endColor
                )
            )
            isClickable = true
        }
    }.start()
}