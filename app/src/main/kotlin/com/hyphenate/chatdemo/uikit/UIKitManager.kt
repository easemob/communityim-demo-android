package com.hyphenate.chatdemo.uikit

import android.content.Context
import android.content.Intent
import com.hyphenate.chatdemo.DemoHelper
import com.hyphenate.chatdemo.common.extensions.internal.toProfile
import com.hyphenate.chatdemo.ui.chat.ChatActivity
import com.hyphenate.chatdemo.ui.contact.ChatContactCheckActivity
import com.hyphenate.chatdemo.ui.contact.ChatContactDetailActivity
import com.hyphenate.chatdemo.ui.group.ChatGroupDetailActivity
import com.hyphenate.chatdemo.ui.group.ChatCreateGroupActivity
import com.hyphenate.chatdemo.repository.ProfileInfoRepository
import com.hyphenate.chatdemo.ui.contact.ChatNewRequestActivity
import com.hyphenate.easeui.ChatUIKitClient
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatException
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.ChatUserInfoType
import com.hyphenate.easeui.common.extensions.toProfile
import com.hyphenate.easeui.common.impl.OnValueSuccess
import com.hyphenate.easeui.feature.chat.activities.UIKitChatActivity
import com.hyphenate.easeui.feature.contact.ChatUIKitContactCheckActivity
import com.hyphenate.easeui.feature.contact.ChatUIKitContactDetailsActivity
import com.hyphenate.easeui.feature.group.ChatUIKitCreateGroupActivity
import com.hyphenate.easeui.feature.group.ChatUIKitGroupDetailActivity
import com.hyphenate.easeui.feature.invitation.ChatUIKitNewRequestsActivity
import com.hyphenate.easeui.model.ChatUIKitGroupProfile
import com.hyphenate.easeui.model.ChatUIKitProfile
import com.hyphenate.easeui.provider.ChatUIKitCustomActivityRoute
import com.hyphenate.easeui.provider.ChatUIKitGroupProfileProvider
import com.hyphenate.easeui.provider.ChatUIKitSettingsProvider
import com.hyphenate.easeui.provider.ChatUIKitUserProfileProvider
import com.hyphenate.easeui.widget.ChatUIKitImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object UIKitManager {

    fun addUIKitSettings(context: Context) {
        addProviders(context)
        setUIKitConfigs(context)
    }

    fun addProviders(context: Context) {
        ChatUIKitClient.setUserProfileProvider(object : ChatUIKitUserProfileProvider {
                override fun getUser(userId: String?): ChatUIKitProfile? {
                    return DemoHelper.getInstance().getDataModel().getAllContacts()[userId]?.toProfile()
                }

                override fun fetchUsers(
                    userIds: List<String>,
                    onValueSuccess: OnValueSuccess<List<ChatUIKitProfile>>
                ) {
                    // fetch users from server and call call onValueSuccess.onSuccess(users) after successfully getting users
                    CoroutineScope(Dispatchers.IO).launch {
                        if (userIds.isEmpty()) {
                            onValueSuccess(mutableListOf())
                            return@launch
                        }
                        try {
                            val users = ProfileInfoRepository().getUserInfoAttribute(userIds, mutableListOf(ChatUserInfoType.NICKNAME, ChatUserInfoType.AVATAR_URL))
                            val callbackList = users.values.map { it.toProfile() }.map {
                                DemoHelper.getInstance().getDataModel().getUser(it.id)?.remark?.let { remark->
                                    it.remark = remark
                                }
                                it
                            }
                            if (callbackList.isNotEmpty()) {
                                DemoHelper.getInstance().getDataModel().insertUsers(callbackList)
                                DemoHelper.getInstance().getDataModel().updateUsersTimes(callbackList)
                                ChatUIKitClient.updateUsersInfo(callbackList)
                            }
                            onValueSuccess(callbackList)
                        }catch (e:ChatException){
                            ChatLog.e("fetchUsers", "fetchUsers error: ${e.description}")
                        }
                    }
                }
            })
            .setGroupProfileProvider(object : ChatUIKitGroupProfileProvider {

                override fun getGroup(id: String?): ChatUIKitGroupProfile? {
                    ChatClient.getInstance().groupManager().getGroup(id)?.let {
                        return ChatUIKitGroupProfile(it.groupId, it.groupName, it.extension)
                    }
                    return null
                }

                override fun fetchGroups(
                    groupIds: List<String>,
                    onValueSuccess: OnValueSuccess<List<ChatUIKitGroupProfile>>
                ) {

                }
            })
            .setSettingsProvider(object : ChatUIKitSettingsProvider {
                override fun isMsgNotifyAllowed(message: ChatMessage?): Boolean {
                    return true
                }

                override fun isMsgSoundAllowed(message: ChatMessage?): Boolean {
                    return false
                }

                override fun isMsgVibrateAllowed(message: ChatMessage?): Boolean {
                    return false
                }

                override val isSpeakerOpened: Boolean
                    get() = true

            })
            .setCustomActivityRoute(object : ChatUIKitCustomActivityRoute {
                override fun getActivityRoute(intent: Intent): Intent? {
                    intent.component?.className?.let {
                        when(it) {
                            UIKitChatActivity::class.java.name -> {
                                intent.setClass(context, ChatActivity::class.java)
                            }
                            ChatUIKitGroupDetailActivity::class.java.name -> {
                                intent.setClass(context, ChatGroupDetailActivity::class.java)
                            }
                            ChatUIKitContactDetailsActivity::class.java.name -> {
                                intent.setClass(context, ChatContactDetailActivity::class.java)
                            }
                            ChatUIKitCreateGroupActivity::class.java.name -> {
                                intent.setClass(context, ChatCreateGroupActivity::class.java)
                            }
                            ChatUIKitContactCheckActivity::class.java.name ->{
                                intent.setClass(context, ChatContactCheckActivity::class.java)
                            }
                            ChatUIKitNewRequestsActivity::class.java.name ->{
                                intent.setClass(context, ChatNewRequestActivity::class.java)
                            }
                            else -> {
                                return intent
                            }
                        }
                    }
                    return intent
                }

            })
    }

    fun setUIKitConfigs(context: Context) {
        ChatUIKitClient.getConfig()?.avatarConfig?.let {
            it.avatarShape = ChatUIKitImageView.ShapeType.RECTANGLE
            it.avatarRadius = context.resources.getDimensionPixelSize(com.hyphenate.easeui.R.dimen.ease_corner_extra_small)
        }
    }
}