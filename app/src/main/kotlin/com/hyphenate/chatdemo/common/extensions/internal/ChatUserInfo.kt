package com.hyphenate.chatdemo.common.extensions.internal

import com.hyphenate.easeui.common.ChatUserInfo
import com.hyphenate.easeui.model.ChatUIKitProfile

internal fun ChatUserInfo.toProfile(): ChatUIKitProfile {
    return ChatUIKitProfile(
        id = userId,
        name = nickname,
        avatar = avatarUrl
    )
}