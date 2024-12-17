package com.hyphenate.chatdemo.interfaces

import com.hyphenate.easeui.viewmodel.IAttachView

interface IMainRequest: IAttachView {

    /**
     * Get all unread message count.
     */
    fun getUnreadMessageCount()

    /**
     * Get all unread request count.
     */
    fun getRequestUnreadCount()
}