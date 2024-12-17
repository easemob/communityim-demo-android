package com.hyphenate.chatdemo.common

import com.hyphenate.chatdemo.DemoHelper
import com.hyphenate.chatdemo.R
import com.hyphenate.easeui.ChatUIKitClient
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatConversationType
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.ChatMessageStatus
import com.hyphenate.easeui.common.ChatMessageType
import com.hyphenate.easeui.common.ChatTextMessageBody
import com.hyphenate.easeui.common.ChatType
import com.hyphenate.easeui.common.ChatUIKitConstant
import com.hyphenate.easeui.provider.getSyncUser

object LocalNotifyHelper {
    /**
     * Create a local message when receive a unsent message.
     */
    fun createContactNotifyMessage(userId:String?): ChatMessage? {
        DemoHelper.getInstance().context.resources?.let {
            val user = ChatUIKitClient.getUserProvider()?.getSyncUser(userId)
            val msgNotification = ChatMessage.createReceiveMessage(ChatMessageType.TXT)
            val text = it.getString(R.string.demo_contact_added_notify,(user?.getRemarkOrName())?:"$userId")
            val txtBody = ChatTextMessageBody(text)
            msgNotification.addBody(txtBody)
            msgNotification.to = ChatUIKitClient.getCurrentUser()?.id
            msgNotification.from = userId
            msgNotification.msgTime = System.currentTimeMillis()
            msgNotification.chatType = ChatType.Chat
            msgNotification.setLocalTime(System.currentTimeMillis())
            msgNotification.setAttribute(ChatUIKitConstant.MESSAGE_TYPE_CONTACT_NOTIFY, true)
            msgNotification.setStatus(ChatMessageStatus.SUCCESS)
            msgNotification.setIsChatThreadMessage(false)
            return msgNotification
        }
       return null
    }

    /**
     * Remove a local message when receive contact notify message.
     */
    fun removeContactNotifyMessage(userId:String?){
        val conversation = ChatClient.getInstance().chatManager().getConversation(userId, ChatConversationType.Chat )
        conversation?.let {
            it.allMessages.map { msg->
                if (msg.ext().containsKey(ChatUIKitConstant.MESSAGE_TYPE_CONTACT_NOTIFY)){
                    it.removeMessage(msg.msgId)
                }
            }
        }
    }
}