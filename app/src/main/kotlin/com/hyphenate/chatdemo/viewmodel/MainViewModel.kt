package com.hyphenate.chatdemo.viewmodel

import androidx.lifecycle.viewModelScope
import com.hyphenate.chatdemo.interfaces.IMainRequest
import com.hyphenate.chatdemo.interfaces.IMainResultView
import com.hyphenate.chatdemo.repository.ChatClientRepository
import com.hyphenate.easeui.viewmodel.ChatUIKitBaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel: ChatUIKitBaseViewModel<IMainResultView>(), IMainRequest {
    private val chatRepository by lazy { ChatClientRepository() }
    override fun getUnreadMessageCount() {
        viewModelScope.launch {
            flow {
                emit(chatRepository.getAllUnreadMessageCount())
            }
            .map {
                if (it <= 0) {
                    null
                } else if (it > 99) {
                    "99+"
                } else {
                    it.toString()
                }
            }
            .flowOn(Dispatchers.Main)
            .collectLatest {
                view?.getUnreadCountSuccess(it)
            }
        }
    }

    override fun getRequestUnreadCount() {
        viewModelScope.launch {
            flow {
                emit(chatRepository.getRequestUnreadCount())
            }
                .map {
                    if (it <= 0) {
                        null
                    } else if (it > 99) {
                        "99+"
                    } else {
                        it.toString()
                    }
                }
                .flowOn(Dispatchers.Main)
                .collectLatest {
                    view?.getRequestUnreadCountSuccess(it)
                }
        }
    }
}