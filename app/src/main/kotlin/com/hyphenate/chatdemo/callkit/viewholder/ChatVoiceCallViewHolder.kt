package com.hyphenate.chatdemo.callkit.viewholder

import android.view.View
import com.hyphenate.chatdemo.callkit.VideoCallActivity
import com.hyphenate.easecallkit.EaseCallKit
import com.hyphenate.easecallkit.base.EaseCallType
import com.hyphenate.easecallkit.utils.EaseMsgUtils
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.ChatMessageDirection
import com.hyphenate.easeui.feature.chat.viewholders.ChatUIKitRowViewHolder

class ChatVoiceCallViewHolder(itemView: View): ChatUIKitRowViewHolder(itemView) {

    override fun onBubbleClick(message: ChatMessage?) {
        super.onBubbleClick(message)
        message?.let {
            if (it.getIntAttribute(EaseMsgUtils.CALL_TYPE, 0) == EaseCallType.SINGLE_VOICE_CALL.ordinal) {
                if (it.direct() == ChatMessageDirection.RECEIVE) {
                    // answer call
                    EaseCallKit.getInstance().startSingleCall(
                        EaseCallType.SINGLE_VOICE_CALL, message.from, null,
                        VideoCallActivity::class.java
                    )
                } else {
                    // make call
                    EaseCallKit.getInstance().startSingleCall(
                        EaseCallType.SINGLE_VOICE_CALL, message.to, null,
                        VideoCallActivity::class.java
                    )
                }
            } else {
                if (it.direct() == ChatMessageDirection.RECEIVE) {
                    // answer call
                    EaseCallKit.getInstance().startSingleCall(
                        EaseCallType.SINGLE_VIDEO_CALL, message.from, null,
                        VideoCallActivity::class.java
                    )
                } else {
                    // make call
                    EaseCallKit.getInstance().startSingleCall(
                        EaseCallType.SINGLE_VIDEO_CALL, message.to, null,
                        VideoCallActivity::class.java
                    )
                }
            }
        }
    }
}