package com.hyphenate.chatdemo.common.suspend

import com.hyphenate.easeui.ChatUIKitClient
import com.hyphenate.easeui.common.ChatContactManager
import com.hyphenate.easeui.common.ChatException
import com.hyphenate.easeui.common.extensions.toUser
import com.hyphenate.easeui.common.impl.ValueCallbackImpl
import com.hyphenate.easeui.model.ChatUIKitUser
import com.hyphenate.easeui.provider.getSyncUser
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/**
 * Suspend method for [ChatContactManager.fetchContactsFromServer()]
 * @return List<ChatUIKitUser> User Information List
 */
suspend fun ChatContactManager.fetchResultContactsFromServer():List<ChatUIKitUser>{
    return suspendCoroutine{ continuation ->
        asyncFetchAllContactsFromServer(ValueCallbackImpl(
            onSuccess = { value ->
                value?.let {
                    val list = it.map { contact ->
                        ChatUIKitClient.getUserProvider()?.getSyncUser(contact.username)?.toUser() ?: ChatUIKitUser(contact.username)
                    }
                    continuation.resume(list)
                }
            },
            onError = {code,message-> continuation.resumeWithException(ChatException(code, message)) }
        ))
    }
}