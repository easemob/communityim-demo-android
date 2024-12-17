package com.hyphenate.chatdemo.ui.contact

import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import com.hyphenate.chatdemo.DemoHelper
import com.hyphenate.chatdemo.common.DemoConstant
import com.hyphenate.chatdemo.common.room.entity.parse
import com.hyphenate.chatdemo.common.room.extensions.parseToDbBean
import com.hyphenate.chatdemo.viewmodel.ProfileInfoViewModel
import com.hyphenate.easeui.ChatUIKitClient
import com.hyphenate.easeui.R
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.ChatUserInfoType
import com.hyphenate.easeui.common.bus.ChatUIKitFlowBus
import com.hyphenate.easeui.common.extensions.catchChatException
import com.hyphenate.easeui.feature.contact.ChatUIKitContactCheckActivity
import com.hyphenate.easeui.model.ChatUIKitEvent
import kotlinx.coroutines.launch

class ChatContactCheckActivity:ChatUIKitContactCheckActivity() {
    private lateinit var model: ProfileInfoViewModel

    override fun initData() {
        super.initData()
        model = ViewModelProvider(this)[ProfileInfoViewModel::class.java]
        lifecycleScope.launch {
            user?.let { user->
                model.fetchUserInfoAttribute(listOf(user.userId), listOf(ChatUserInfoType.NICKNAME, ChatUserInfoType.AVATAR_URL))
                    .catchChatException {
                        ChatLog.e("ChatContactCheckActivity", "fetchUserInfoAttribute error: ${it.description}")
                    }
                    .collect {
                        it[user.userId]?.parseToDbBean()?.let {u->
                            u.parse().apply {
                                remark = ChatClient.getInstance().contactManager().fetchContactFromLocal(id)?.remark
                                ChatUIKitClient.updateUsersInfo(mutableListOf(this))
                                DemoHelper.getInstance().getDataModel().insertUser(this)
                            }
                            updateUserInfo()
                            notifyUpdateRemarkEvent()
                        }
                    }
            }
        }
    }

    private fun updateUserInfo() {
        DemoHelper.getInstance().getDataModel().getUser(user?.userId)?.let {
            val ph = AppCompatResources.getDrawable(this, R.drawable.uikit_default_avatar)
            val ep = AppCompatResources.getDrawable(this, R.drawable.uikit_default_avatar)
            binding.ivAvatar.load(it.parse().avatar ?: ph) {
                placeholder(ph)
                error(ep)
            }
            binding.tvName.text = it.name?.ifEmpty { it.userId } ?: it.userId
        }
    }

    private fun notifyUpdateRemarkEvent() {
        DemoHelper.getInstance().getDataModel().updateUserCache(user?.userId)
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE + ChatUIKitEvent.TYPE.CONTACT + DemoConstant.EVENT_UPDATE_USER_SUFFIX)
            .post(lifecycleScope, ChatUIKitEvent(DemoConstant.EVENT_UPDATE_USER_SUFFIX, ChatUIKitEvent.TYPE.CONTACT, user?.userId))
    }


}