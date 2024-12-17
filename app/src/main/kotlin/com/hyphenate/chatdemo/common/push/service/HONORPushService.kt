package com.hyphenate.chatdemo.common.push.service

import com.hihonor.push.sdk.HonorMessageService
import com.hihonor.push.sdk.HonorPushDataMsg
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatLog

class HONORPushService : HonorMessageService() {
    //Token发生变化时，会以onNewToken方法返回
    override fun onNewToken(token: String?) {
        if (token.isNullOrEmpty().not()) {
            //没有失败回调，假定token失败时token为null
            ChatLog.d("HONORPush", "service register honor push token success token:$token")
            ChatClient.getInstance().sendHonorPushTokenToServer(token)
        } else {
            ChatLog.e("HONORPush", "service register honor push token fail!")
        }
    }

    override fun onMessageReceived(honorPushDataMsg: HonorPushDataMsg) {
        ChatLog.d("HONORPush", "onMessageReceived" + honorPushDataMsg.data)
    }
}