package com.hyphenate.chatdemo.common

import android.os.CountDownTimer
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.hyphenate.chatdemo.R

class CustomCountDownTimer(
    private val mTextView: TextView,
    millisInFuture: Long,
    countDownInterval: Long,
    @StringRes private val goingText: Int = R.string.em_login_get_code_again_time,
    @StringRes private val endText: Int = R.string.em_login_get_code_again,
    @ColorRes private val goingColor: Int = com.hyphenate.easeui.R.color.ease_color_on_background_high,
    @ColorRes private val endColor: Int = com.hyphenate.easeui.R.color.ease_color_primary
) : CountDownTimer(millisInFuture, countDownInterval) {
    override fun onTick(millisUntilFinished: Long) {
        mTextView.isClickable = false
        mTextView.text = mTextView.context.getString(
            goingText,
            (millisUntilFinished / 1000).toInt()
        )
        mTextView.setTextColor(
            ContextCompat.getColor(
                mTextView.context,
                goingColor
            )
        )
    }

    override fun onFinish() {
        mTextView.text = mTextView.context.getString(endText)
        mTextView.setTextColor(
            ContextCompat.getColor(
                mTextView.context,
                endColor
            )
        )
        mTextView.isClickable = true
    }
}