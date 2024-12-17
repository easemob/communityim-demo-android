package com.hyphenate.chatdemo.viewmodel

import androidx.lifecycle.ViewModel
import com.hyphenate.chatdemo.repository.PushRepository
import com.hyphenate.easeui.common.ChatPushRemindType
import com.hyphenate.easeui.common.ChatSilentModeParam
import com.hyphenate.easeui.common.ChatSilentModelType
import kotlinx.coroutines.flow.flow

class PushViewModel: ViewModel() {
    private val mRepository = PushRepository()

    fun setSilentModeForApp() =
            flow {
                emit(mRepository.setSilentModeForApp(ChatSilentModeParam(ChatSilentModelType.REMIND_TYPE).setRemindType(ChatPushRemindType.NONE)))
            }

    fun clearSilentModeForApp() =
            flow {
                emit(mRepository.setSilentModeForApp(ChatSilentModeParam(ChatSilentModelType.REMIND_TYPE).setRemindType(ChatPushRemindType.ALL)))
            }

    fun getSilentModeForApp() =
            flow {
                emit(mRepository.getSilentModeForApp())
            }
}