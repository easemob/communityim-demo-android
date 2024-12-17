package com.hyphenate.chatdemo.ui.group

import androidx.core.content.ContextCompat
import com.hyphenate.chatdemo.R
import com.hyphenate.chatdemo.callkit.CallKitManager
import com.hyphenate.chatdemo.common.ReportHelper
import com.hyphenate.chatdemo.common.extensions.internal.parse
import com.hyphenate.easeui.ChatUIKitClient
import com.hyphenate.easeui.common.ChatGroup
import com.hyphenate.easeui.feature.group.ChatUIKitGroupDetailActivity
import com.hyphenate.easeui.model.ChatUIKitMenuItem

class ChatGroupDetailActivity :ChatUIKitGroupDetailActivity(){

    override fun getDetailItem(): MutableList<ChatUIKitMenuItem>? {
        val list = super.getDetailItem()
        val videoItem = ChatUIKitMenuItem(
            title = getString(R.string.menu_video_call),
            resourceId = R.drawable.uikit_video_camera,
            menuId = R.id.group_item_video_call,
            titleColor = ContextCompat.getColor(this, com.hyphenate.easeui.R.color.ease_color_primary),
            order = 2,
            resourceTintColor = ContextCompat.getColor(this, com.hyphenate.easeui.R.color.ease_color_primary)
        )
        list?.add(videoItem)
        return list
    }

    override fun onMenuItemClick(item: ChatUIKitMenuItem?, position: Int): Boolean {
        item?.let {menu->
            return when(menu.menuId){
                R.id.group_item_video_call -> {
                    CallKitManager.startConferenceCall(this, groupId)
                    true
                }

                else -> {
                    super.onMenuItemClick(item, position)
                }
            }
        }
        return false
    }

    override fun fetchGroupDetailSuccess(group: ChatGroup) {
        ChatUIKitClient.updateGroupInfo(listOf(group.parse()))
        super.fetchGroupDetailSuccess(group)
    }

    override fun getBottomSheetMenu(): MutableList<ChatUIKitMenuItem>? {
        val menu = super.getBottomSheetMenu()
        menu?.add( 0,ChatUIKitMenuItem(
            menuId = R.id.contact_complaint,
            title = getString(R.string.demo_report_title),
            titleColor = ContextCompat.getColor(this, com.hyphenate.easeui.R.color.ease_color_primary),
        ))
        return menu
    }

    override fun simpleMenuItemClickListener(position: Int, menu: ChatUIKitMenuItem) {
        super.simpleMenuItemClickListener(position, menu)
        if (menu.menuId == R.id.contact_complaint){
            ReportHelper.openEmailClient(this,groupId)
        }
    }
}