package com.hyphenate.chatdemo.common.push.service

import com.huawei.hms.push.HmsMessageService
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatLog

class HMSPushService : HmsMessageService() {
    override fun onNewToken(token: String?) {
        if (token.isNullOrEmpty().not()) {
            //没有失败回调，假定token失败时token为null
            ChatLog.d("HWHMSPush", "service register huawei hms push token success token:$token")
            ChatClient.getInstance().sendHMSPushTokenToServer(token)
        } else {
            ChatLog.e("HWHMSPush", "service register huawei hms push token fail!")
        }
    }
}