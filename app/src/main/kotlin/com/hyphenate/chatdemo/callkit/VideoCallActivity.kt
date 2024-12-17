package com.hyphenate.chatdemo.callkit

import android.graphics.Color
import com.hyphenate.chatdemo.callkit.extensions.setFitSystemForTheme
import com.hyphenate.easecallkit.base.EaseCallType
import com.hyphenate.easecallkit.ui.EaseVideoCallActivity
import com.hyphenate.easeui.common.utils.StatusBarCompat

class VideoCallActivity: EaseVideoCallActivity() {

    override fun initView() {
        setFitSystemForTheme(true)
        if (callType == EaseCallType.SINGLE_VIDEO_CALL) {
            StatusBarCompat.compat(this, Color.parseColor("#000000"))
        } else {
            StatusBarCompat.compat(this, Color.parseColor("#bbbbbb"))
        }
        super.initView()
    }

}