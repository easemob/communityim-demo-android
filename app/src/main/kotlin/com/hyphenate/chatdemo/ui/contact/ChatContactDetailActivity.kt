package com.hyphenate.chatdemo.ui.contact

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.hyphenate.chatdemo.DemoHelper
import com.hyphenate.chatdemo.R
import com.hyphenate.chatdemo.callkit.CallKitManager
import com.hyphenate.chatdemo.common.DemoConstant
import com.hyphenate.chatdemo.common.PresenceCache
import com.hyphenate.chatdemo.common.ReportHelper
import com.hyphenate.chatdemo.common.room.entity.parse
import com.hyphenate.chatdemo.common.room.extensions.parseToDbBean
import com.hyphenate.chatdemo.interfaces.IPresenceResultView
import com.hyphenate.chatdemo.utils.EasePresenceUtil
import com.hyphenate.chatdemo.viewmodel.PresenceViewModel
import com.hyphenate.chatdemo.viewmodel.ProfileInfoViewModel
import com.hyphenate.easeui.ChatUIKitClient
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.ChatPresence
import com.hyphenate.easeui.common.ChatUserInfoType
import com.hyphenate.easeui.common.bus.ChatUIKitFlowBus
import com.hyphenate.easeui.common.extensions.catchChatException
import com.hyphenate.easeui.common.extensions.toProfile
import com.hyphenate.easeui.feature.contact.ChatUIKitContactDetailsActivity
import com.hyphenate.easeui.model.ChatUIKitEvent
import com.hyphenate.easeui.model.ChatUIKitMenuItem
import com.hyphenate.easeui.widget.ChatUIKitArrowItemView
import kotlinx.coroutines.launch


class ChatContactDetailActivity:ChatUIKitContactDetailsActivity(), IPresenceResultView {
    private lateinit var model: ProfileInfoViewModel
    private lateinit var presenceModel: PresenceViewModel
    private val remarkItem: ChatUIKitArrowItemView by lazy { findViewById(R.id.item_remark) }
    private val spacing: View by lazy { findViewById(R.id.item_spacing) }

    companion object {
        private const val TAG = "ChatContactDetailActivity"
        private const val REQUEST_UPDATE_REMARK = 100
        private const val RESULT_UPDATE_REMARK = "result_update_remark"
    }

    private val launcherToUpdateRemark: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> onActivityResult(result, REQUEST_UPDATE_REMARK) }

    override fun initView() {
        super.initView()
        model = ViewModelProvider(this)[ProfileInfoViewModel::class.java]
        presenceModel = ViewModelProvider(this)[PresenceViewModel::class.java]
        presenceModel.attachView(this)
        updateUserInfo()
    }

    override fun initListener() {
        super.initListener()
        remarkItem.setOnClickListener{
            user?.let {
                launcherToUpdateRemark.launch(ChatContactRemarkActivity.createIntent(mContext,it.userId))
            }
        }
    }

    override fun initEvent() {
        super.initEvent()
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name).register(this) {
            if (it.isPresenceChange ) {
                updatePresence()
            }
        }
    }

    override fun initData() {
        super.initData()
        user?.let {
            presenceModel.fetchChatPresence(mutableListOf(it.userId))
        }
        lifecycleScope.launch {
            user?.let { user->
                model.fetchUserInfoAttribute(listOf(user.userId), listOf(ChatUserInfoType.NICKNAME, ChatUserInfoType.AVATAR_URL))
                    .catchChatException {
                        ChatLog.e("ContactDetail", "fetchUserInfoAttribute error: ${it.description}")
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
            binding.epPresence.setUserAvatarData(it.parse())
            binding.tvName.text = it.parse().getRemarkOrName()
            binding.tvNumber.text = it.userId
            remarkItem.setContent(it.remark)
        }
    }

    override fun getDetailItem(): MutableList<ChatUIKitMenuItem>? {
        val list = super.getDetailItem()
        val audioItem = ChatUIKitMenuItem(
            title = getString(R.string.detail_item_audio),
            resourceId = R.drawable.uikit_phone_pick,
            menuId = R.id.contact_item_audio_call,
            titleColor = ContextCompat.getColor(this, com.hyphenate.easeui.R.color.ease_color_primary),
            order = 2
        )

        val videoItem = ChatUIKitMenuItem(
            title = getString(R.string.detail_item_video),
            resourceId = R.drawable.uikit_video_camera,
            menuId = R.id.contact_item_video_call,
            titleColor = ContextCompat.getColor(this, com.hyphenate.easeui.R.color.ease_color_primary),
            order = 3
        )
        list?.add(audioItem)
        list?.add(videoItem)
        return list
    }

    override fun onMenuItemClick(item: ChatUIKitMenuItem?, position: Int): Boolean {
        item?.let {
            when(item.menuId){
                R.id.contact_item_audio_call -> {
                    CallKitManager.startSingleAudioCall(user?.userId)
                    return true
                }
                R.id.contact_item_video_call -> {
                    CallKitManager.startSingleVideoCall(user?.userId)
                    return true
                }
                else -> {
                    return super.onMenuItemClick(item, position)
                }
            }
        }
        return false
    }

    override fun getDeleteDialogMenu(): MutableList<ChatUIKitMenuItem>? {
        val menu = super.getDeleteDialogMenu()
        menu?.add( 0,ChatUIKitMenuItem(
            menuId = R.id.contact_complaint,
            title = getString(R.string.demo_report_title),
            titleColor = ContextCompat.getColor(this, com.hyphenate.easeui.R.color.ease_color_primary),
        ))
        return menu
    }

    override fun simpleSheetMenuItemClick(position: Int, menu: ChatUIKitMenuItem) {
        super.simpleSheetMenuItemClick(position, menu)
        if (menu.menuId == R.id.contact_complaint){
            ReportHelper.openEmailClient(this,user?.userId)
        }
    }

    private fun onActivityResult(result: ActivityResult, requestCode: Int) {
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            when (requestCode) {
                REQUEST_UPDATE_REMARK ->{
                    data?.let {
                        if (it.hasExtra(RESULT_UPDATE_REMARK)){
                            notifyUpdateRemarkEvent()
                            updateUserInfo()
                        }
                    }
                }
                else -> {}
            }
        }
    }

    private fun notifyUpdateRemarkEvent() {
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE + ChatUIKitEvent.TYPE.CONTACT + DemoConstant.EVENT_UPDATE_USER_SUFFIX)
            .post(lifecycleScope, ChatUIKitEvent(DemoConstant.EVENT_UPDATE_USER_SUFFIX, ChatUIKitEvent.TYPE.CONTACT, user?.userId))
    }

    private fun updatePresence(isRefreshAvatar:Boolean = false){
        val map = PresenceCache.getPresenceInfo
        user?.let { user->
            map.let {
                binding.epPresence.getStatusView().visibility = View.VISIBLE
                if (isRefreshAvatar){
                    binding.epPresence.setUserAvatarData(user.toProfile())
                }
                binding.epPresence.setUserStatusData(EasePresenceUtil.getPresenceIcon(mContext,it[user.userId]))
            }
        }
    }

    override fun fetchChatPresenceSuccess(presence: MutableList<ChatPresence>) {
        ChatLog.e(TAG,"fetchChatPresenceSuccess $presence")
        updatePresence()
    }

    override fun fetchChatPresenceFail(code: Int, error: String) {
        ChatLog.e(TAG,"fetchChatPresenceFail $code $error")
    }

    override fun updateBlockLayout(isChecked: Boolean) {
        super.updateBlockLayout(isChecked)
        if (isChecked){
            remarkItem.visibility = View.GONE
            spacing.visibility = View.GONE
        }else{
            remarkItem.visibility = View.VISIBLE
            spacing.visibility = View.VISIBLE
        }
    }

}