package com.hyphenate.chatdemo.repository

import com.hyphenate.chatdemo.common.suspend.fetchUserPresenceStatus
import com.hyphenate.chatdemo.common.suspend.publishExtPresence
import com.hyphenate.chatdemo.common.suspend.subscribeUsersPresence
import com.hyphenate.chatdemo.common.suspend.unSubscribeUsersPresence
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatPresenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatPresenceRepository(
    private val presenceManager: ChatPresenceManager = ChatClient.getInstance().presenceManager(),
) {

    suspend fun publishPresence(customStatus: String) =
        withContext(Dispatchers.IO) {
            presenceManager.publishExtPresence(customStatus)
        }


    suspend fun subscribePresences(userIds:MutableList<String>,expiry:Long) =
        withContext(Dispatchers.IO) {
            presenceManager.subscribeUsersPresence(userIds,expiry)
        }


    suspend fun unSubscribePresences(userIds:MutableList<String>) =
        withContext(Dispatchers.IO) {
            presenceManager.unSubscribeUsersPresence(userIds)
        }

    suspend fun fetchPresenceStatus(userIds:MutableList<String>) =
        withContext(Dispatchers.IO) {
            presenceManager.fetchUserPresenceStatus(userIds)
        }


}