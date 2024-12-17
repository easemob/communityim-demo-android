package com.hyphenate.chatdemo.repository

import com.hyphenate.chatdemo.common.suspend.getSilentModeForApp
import com.hyphenate.chatdemo.common.suspend.setSilentModeForApp
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatSilentModeParam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PushRepository: BaseRepository() {
    private val pushManager = ChatClient.getInstance().pushManager()

    /**
     * Make app interruption-free
     */
    suspend fun setSilentModeForApp(silentModeParam: ChatSilentModeParam) =
        withContext(Dispatchers.IO) {
            pushManager.setSilentModeForApp(silentModeParam)
        }

    /**
     * Get the silent mode for the App.
     */
    suspend fun getSilentModeForApp() =
        withContext(Dispatchers.IO) {
            pushManager.getSilentModeForApp()
        }

}