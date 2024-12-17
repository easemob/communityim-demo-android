package com.hyphenate.chatdemo.repository

import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.feature.invitation.helper.ChatUIKitNotificationMsgManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * As the repository of ChatManager, handles ChatManager related logic
 */
class ChatClientRepository: BaseRepository() {

    /**
     * Get all unread message count.
     */
    suspend fun getAllUnreadMessageCount(): Int =
        withContext(Dispatchers.IO) {
            val systemConversation = ChatUIKitNotificationMsgManager.getInstance().getConversation()
            val systemUnread = systemConversation.unreadMsgCount
            val allUnread = ChatClient.getInstance().chatManager().unreadMessageCount
            allUnread - systemUnread
        }

    /**
     * Get all unread request count.
     */
    suspend fun getRequestUnreadCount():Int =
        withContext(Dispatchers.IO) {
            val systemConversation = ChatUIKitNotificationMsgManager.getInstance().getConversation()
            systemConversation.unreadMsgCount
        }

}