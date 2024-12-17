package com.hyphenate.chatdemo.viewmodel

import androidx.lifecycle.viewModelScope
import com.hyphenate.chatdemo.DemoHelper
import com.hyphenate.chatdemo.common.LocalNotifyHelper
import com.hyphenate.chatdemo.common.room.entity.parse
import com.hyphenate.chatdemo.repository.ChatContactRepository
import com.hyphenate.easeui.ChatUIKitClient
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatContact
import com.hyphenate.easeui.common.ChatException
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.ChatValueCallback
import com.hyphenate.easeui.common.extensions.catchChatException
import com.hyphenate.easeui.common.extensions.toUser
import com.hyphenate.easeui.common.helper.ContactSortedHelper
import com.hyphenate.easeui.model.ChatUIKitProfile
import com.hyphenate.easeui.model.ChatUIKitUser
import com.hyphenate.easeui.model.setUserInitialLetter
import com.hyphenate.easeui.viewmodel.contacts.ChatUIKitContactListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatContactViewModel: ChatUIKitContactListViewModel() {
     private var contactRepository = ChatContactRepository()

    override fun loadData(fetchServerData: Boolean){
        viewModelScope.launch {
            if (fetchServerData) {
                flow {
                    emit(contactRepository.loadLocalContact())
                }
                .flatMapConcat {
                    val sortedList = it.map {
                            user -> user.setUserInitialLetter()
                            user
                        }
                        .let {list ->
                            ContactSortedHelper.sortedList(list).toMutableList()
                        }
                    view?.loadContactListSuccess(sortedList)
                    flow {
                        emit(contactRepository.loadData())
                    }
                }
            } else {
                flow {
                    emit(contactRepository.loadLocalContact())
                }
            }
                .catchChatException { e ->
                    view?.loadContactListFail(e.errorCode, e.description)
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), mutableListOf())
                .collect {
                    ChatClient.getInstance().contactManager().asyncFetchAllContactsFromLocal(object :
                        ChatValueCallback<MutableList<ChatContact>> {
                        override fun onSuccess(value: MutableList<ChatContact>?) {
                            value?.forEach { contact->
                               val data = it.map { user->
                                    val profile = DemoHelper.getInstance().getDataModel().getUser(user.userId)?.parse()?: ChatUIKitProfile(user.userId)
                                    if (contact.username == user.userId && contact.remark.isNotEmpty()){
                                        profile.remark = contact.remark
                                        DemoHelper.getInstance().getDataModel().insertUser(profile,false)
                                    }
                                    profile
                                } as MutableList<ChatUIKitProfile>?
                                data?.let { it1 ->
                                    ChatUIKitClient.updateUsersInfo(it1)
                                }
                                val result = data?.map { it.toUser() }
                                result?.map {
                                    it.setUserInitialLetter()
                                }
                                result?.let {
                                    val sortedList = ContactSortedHelper.sortedList(it)
                                    viewModelScope.launch {
                                        view?.loadContactListSuccess(sortedList.toMutableList())
                                    }
                                }
                            }
                        }

                        override fun onError(error: Int, errorMsg: String?) {
                            ChatLog.e("ChatContactViewModel","asyncFetchAllContactsFromLocal onError $error $errorMsg")
                        }
                    })
                }
        }
    }

    override fun fetchContactInfo(contactList: List<ChatUIKitUser>?) {
        contactList?.filter {
            val user = DemoHelper.getInstance().getDataModel().getUser(it.userId)
            (user == null || user.updateTimes == 0) &&
                    (it.nickname.isNullOrEmpty() || it.avatar.isNullOrEmpty())
        }?.apply {
            if (this.isNotEmpty()) {
                super.fetchContactInfo(this)
            }
        }
    }

    override fun addContact(userName: String, reason: String?) {
        viewModelScope.launch {
            flow {
                try {
                    emit(contactRepository.checkPhoneNumOrIdFromServer(userName))
                }catch (e:ChatException){
                    inMainScope {
                        view?.addContactFail(e.errorCode,e.description)
                    }
                }
            }
            .flatMapConcat { username->
                flow<String> {
                    username?.let {
                        super.addContact(it, reason)
                    }
                }
            }
            .catchChatException { e->
                view?.addContactFail(e.errorCode,e.description)
            }
           .collect{
                LocalNotifyHelper.createContactNotifyMessage(it)
                view?.addContactSuccess(it)
            }
        }
    }


    private fun inMainScope(scope: ()->Unit) {
        viewModelScope.launch(context = Dispatchers.Main) {
            scope()
        }
    }
}