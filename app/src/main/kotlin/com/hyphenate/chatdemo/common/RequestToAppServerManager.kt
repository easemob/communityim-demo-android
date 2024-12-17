package com.hyphenate.chatdemo.common

import com.hyphenate.chatdemo.repository.GroupRepository
import com.hyphenate.easeui.common.ChatGroup
import com.hyphenate.easeui.common.extensions.catchChatException
import com.hyphenate.easeui.common.impl.OnError
import com.hyphenate.easeui.common.impl.OnValueSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

object RequestToAppServerManager {
    fun reportGroupIdToServer(
        group:ChatGroup,
        onSuccess: OnValueSuccess<ChatGroup>,
        onError: OnError
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            flow {
                emit(GroupRepository().reportGroupIdToServer(group))
            }
            .catchChatException { onError.invoke(it.errorCode, it.message) }
            .collect {
                onSuccess.invoke(it)
            }
        }
    }
}