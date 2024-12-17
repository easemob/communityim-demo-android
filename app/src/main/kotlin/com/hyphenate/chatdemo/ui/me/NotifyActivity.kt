package com.hyphenate.chatdemo.ui.me

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.hyphenate.chatdemo.DemoHelper
import com.hyphenate.chatdemo.databinding.DemoActivityNotifyBinding
import com.hyphenate.chatdemo.viewmodel.PushViewModel
import com.hyphenate.easeui.base.ChatUIKitBaseActivity
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.ChatPushRemindType
import com.hyphenate.easeui.common.extensions.catchChatException
import kotlinx.coroutines.launch

class NotifyActivity:ChatUIKitBaseActivity<DemoActivityNotifyBinding>() {

    private  var pushViewModel: PushViewModel? = null

    override fun getViewBinding(inflater: LayoutInflater): DemoActivityNotifyBinding? {
        return DemoActivityNotifyBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListener()
        initData()
    }

    private fun initData() {
        pushViewModel = ViewModelProvider(this)[PushViewModel::class.java]
        pushViewModel?.let {
            lifecycleScope.launch {
                it.getSilentModeForApp()
                    .catchChatException {
                        ChatLog.e("notify", "initData: ${it.description}")
                    }
                    .collect {
                        it.remindType?.let { remindType ->
                            if (remindType == ChatPushRemindType.MENTION_ONLY) {
                                DemoHelper.getInstance().getDataModel().setAppPushSilent(true)
                                binding.switchItemNotify.setChecked(true)
                            } else {
                                DemoHelper.getInstance().getDataModel().setAppPushSilent(false)
                                binding.switchItemNotify.setChecked(false)
                            }
                        }
                    }
            }
        }
    }

    private fun initView(){
        initSwitch()
    }

    private fun initSwitch(){
        binding.switchItemNotify.setSwitchTarckDrawable(com.hyphenate.easeui.R.drawable.uikit_switch_track_selector)
        binding.switchItemNotify.setSwitchThumbDrawable(com.hyphenate.easeui.R.drawable.uikit_switch_thumb_selector)
    }

    private fun initListener(){
        binding.let {
            it.titleBar.setNavigationOnClickListener{
                mContext.onBackPressed()
            }
            it.switchItemNotify.setOnClickListener {
                binding.switchItemNotify.switch?.let { switch ->
                    val isChecked = switch.isChecked
                    changeAppSilentModel(isChecked.not())
                }
            }
        }
    }

    private fun changeAppSilentModel(checked: Boolean) {
        lifecycleScope.launch {
            if (checked) {
                pushViewModel?.let {
                    it.setSilentModeForApp()
                        .catchChatException {
                            ChatLog.e("notify", "changeAppSilentModel: ${it.description}")
                        }
                        .collect {
                            DemoHelper.getInstance().getDataModel().setAppPushSilent(true)
                            binding.switchItemNotify.setChecked(true)
                        }
                }
            } else {
                pushViewModel?.let {
                    it.clearSilentModeForApp()
                        .catchChatException {
                            ChatLog.e("notify", "changeAppSilentModel: ${it.description}")
                        }
                        .collect {
                            DemoHelper.getInstance().getDataModel().setAppPushSilent(false)
                            binding.switchItemNotify.setChecked(false)
                        }
                }
            }
        }
    }
}