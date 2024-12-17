package com.hyphenate.chatdemo.ui.chat

import android.text.TextUtils
import android.view.ViewGroup
import com.hyphenate.chatdemo.callkit.viewholder.ChatConferenceInviteViewHolder
import com.hyphenate.chatdemo.callkit.viewholder.ChatVoiceCallViewHolder
import com.hyphenate.chatdemo.callkit.views.ChatRowConferenceInvite
import com.hyphenate.chatdemo.callkit.views.ChatRowVoiceCall
import com.hyphenate.easecallkit.base.EaseCallType
import com.hyphenate.easecallkit.utils.EaseMsgUtils
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.ChatMessageDirection
import com.hyphenate.easeui.feature.chat.adapter.ChatUIKitMessagesAdapter

class CustomMessagesAdapter: ChatUIKitMessagesAdapter() {

    companion object {
        const val VIEW_TYPE_MESSAGE_CALL_SEND = 1000
        const val VIEW_TYPE_MESSAGE_CALL_RECEIVE = 1001
        const val VIEW_TYPE_MESSAGE_INVITE_SEND = 1002
        const val VIEW_TYPE_MESSAGE_INVITE_RECEIVE = 1003
    }
    //继承EaseMessagesAdapter 重写getItemNotEmptyViewType 添加自定义ViewType
    //下方示例 增加自定义 call 消息提醒类型
    override fun getItemNotEmptyViewType(position: Int): Int {
        getItem(position)?.let {
            val msgType = it.getStringAttribute(EaseMsgUtils.CALL_MSG_TYPE,"")
            val callType = it.getIntAttribute(EaseMsgUtils.CALL_TYPE, 0)
            if (TextUtils.equals(msgType, EaseMsgUtils.CALL_MSG_INFO)) {
                if (callType == EaseCallType.CONFERENCE_CALL.ordinal) {
                    return if (it.direct() == ChatMessageDirection.SEND) VIEW_TYPE_MESSAGE_INVITE_SEND else VIEW_TYPE_MESSAGE_INVITE_RECEIVE
                }
                return if (it.direct() == ChatMessageDirection.SEND) VIEW_TYPE_MESSAGE_CALL_SEND else VIEW_TYPE_MESSAGE_CALL_RECEIVE
            }
        }
        return super.getItemNotEmptyViewType(position)
    }

    // 继承EaseMessagesAdapter getViewHolder 添加自定义ViewHolder 和 ui布局
    // 下方示例 增加单聊、群聊 call 消息提醒类型布局和事件处理
    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatMessage> {
        when (viewType) {
            VIEW_TYPE_MESSAGE_CALL_SEND, VIEW_TYPE_MESSAGE_CALL_RECEIVE -> {
                return ChatVoiceCallViewHolder(ChatRowVoiceCall(parent.context, isSender = viewType == VIEW_TYPE_MESSAGE_CALL_SEND))
            }
            VIEW_TYPE_MESSAGE_INVITE_SEND, VIEW_TYPE_MESSAGE_INVITE_RECEIVE -> {
                return ChatConferenceInviteViewHolder(ChatRowConferenceInvite(parent.context, isSender = viewType == VIEW_TYPE_MESSAGE_INVITE_SEND))
            }
        }
        return super.getViewHolder(parent, viewType)
    }
}