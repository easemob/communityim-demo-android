package com.hyphenate.chatdemo.common.push.receiver

import android.content.Context
import com.hyphenate.chatdemo.callkit.CallKitManager
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.push.platform.mi.EMMiMsgReceiver
import com.xiaomi.mipush.sdk.MiPushMessage
import org.json.JSONObject

/**
 * 获取有关小米音视频推送消息
 */
class MiMsgReceiver : EMMiMsgReceiver() {
    override fun onNotificationMessageClicked(context: Context, message: MiPushMessage) {
        ChatLog.i(TAG, "onNotificationMessageClicked is called. $message")
        val extStr: String = message.content
        ChatLog.i(TAG, "onReceivePassThroughMessage get extras: $extStr")
        try {
            val extras = JSONObject(extStr)
            ChatLog.i(TAG, "onReceivePassThroughMessage get extras: $extras")
            val ext = extras.getJSONObject("e")
            if (ext != null) {
                CallKitManager.isRtcCall = ext.getBoolean("isRtcCall")
                CallKitManager.rtcType = ext.getInt("callType")
                ChatLog.i(TAG, "onReceivePassThroughMessage get type: " + CallKitManager.rtcType)
            }
        } catch (e: Exception) {
            e.stackTrace
        }
        super.onNotificationMessageClicked(context, message)
    }

    companion object {
        private const val TAG = "MiMsgReceiver"
    }
}