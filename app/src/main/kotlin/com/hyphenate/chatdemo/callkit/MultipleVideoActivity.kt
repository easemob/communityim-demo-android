package com.hyphenate.chatdemo.callkit

import android.graphics.Color
import com.hyphenate.chatdemo.callkit.extensions.setFitSystemForTheme
import com.hyphenate.easecallkit.ui.EaseMultipleVideoActivity
import com.hyphenate.easeui.common.utils.StatusBarCompat

class MultipleVideoActivity: EaseMultipleVideoActivity() {

    override fun initView() {
        setFitSystemForTheme(true)
        StatusBarCompat.compat(this, Color.parseColor("#858585"))
        super.initView()
    }
}