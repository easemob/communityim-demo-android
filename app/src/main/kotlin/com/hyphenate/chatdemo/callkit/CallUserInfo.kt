package com.hyphenate.chatdemo.callkit

import com.hyphenate.easecallkit.base.EaseCallUserInfo
import com.hyphenate.easeui.ChatUIKitClient
import com.hyphenate.easeui.model.ChatUIKitProfile
import com.hyphenate.easeui.provider.getSyncUser

data class CallUserInfo(
    val userId: String?,
    var nickName: String? = null,
    var headImage: String? = null
)

internal fun CallUserInfo.getUserInfo(groupId: String?): CallUserInfo {
    return if (!groupId.isNullOrEmpty()) {
        ChatUIKitProfile.getGroupMember(groupId, this.userId)?.let {
            this.nickName = it.getRemarkOrName()
            this.headImage = it.avatar
        }
        this
    } else {
        ChatUIKitClient.getUserProvider()?.getSyncUser(this.userId)?.let {
            this.nickName = it.getRemarkOrName()
            this.headImage = it.avatar
        }
        this
    }
}

/**
 * Parse to EaseCallUserInfo.
 */
internal fun CallUserInfo.parse(): EaseCallUserInfo {
    return EaseCallUserInfo(nickName, headImage).let {
        it.userId = this.userId
        it
    }
}
