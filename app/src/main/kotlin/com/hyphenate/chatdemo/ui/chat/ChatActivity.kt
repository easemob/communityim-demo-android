package com.hyphenate.chatdemo.ui.chat

import com.hyphenate.chatdemo.R
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.extensions.showToast
import com.hyphenate.easeui.feature.chat.UIKitChatFragment
import com.hyphenate.easeui.feature.chat.activities.UIKitChatActivity
import com.hyphenate.easeui.feature.chat.interfaces.OnMessageForwardCallback
import com.hyphenate.easeui.feature.chat.interfaces.OnModifyMessageListener
import com.hyphenate.easeui.feature.chat.interfaces.OnSendCombineMessageCallback

class ChatActivity: UIKitChatActivity() {

    override fun setChildSettings(builder: UIKitChatFragment.Builder) {
        super.setChildSettings(builder)
        builder.setOnMessageForwardCallback(object : OnMessageForwardCallback {
            override fun onForwardSuccess(message: ChatMessage?) {
                showToast(R.string.message_forward_success)
            }

            override fun onForwardError(code: Int, errorMsg: String?) {
                showToast(R.string.message_forward_fail)
            }
        })
        builder.setOnSendCombineMessageCallback(object : OnSendCombineMessageCallback {
            override fun onSendCombineSuccess(message: ChatMessage?) {
                showToast(R.string.message_combine_success)
            }

            override fun onSendCombineError(message: ChatMessage?, code: Int, errorMsg: String?) {
                showToast(R.string.message_combine_fail)
            }
        })
        builder.setOnModifyMessageListener(object : OnModifyMessageListener{
            override fun onModifyMessageSuccess(messageModified: ChatMessage?) {

            }

            override fun onModifyMessageFailure(messageId: String?, code: Int, error: String?) {
                showToast(R.string.message_modify_fail)
            }
        })
        builder.setCustomFragment(ChatFragment())
            .setCustomAdapter(CustomMessagesAdapter())
    }
}