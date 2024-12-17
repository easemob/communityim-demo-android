package com.hyphenate.chatdemo.common.extensions.internal

import com.hyphenate.easeui.common.ChatGroup
import com.hyphenate.easeui.model.ChatUIKitGroupProfile

internal fun ChatGroup.parse(): ChatUIKitGroupProfile {
    return ChatUIKitGroupProfile(groupId, groupName, extension)
}