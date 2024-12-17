package com.hyphenate.chatdemo.callkit

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.hyphenate.chatdemo.R
import com.hyphenate.chatdemo.base.BaseInitActivity
import com.hyphenate.chatdemo.databinding.DemoActivityConferenceInviteBinding
import com.hyphenate.easecallkit.EaseCallKit
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.extensions.showToast
import com.hyphenate.easeui.interfaces.OnContactSelectedListener

class ConferenceInviteActivity: BaseInitActivity<DemoActivityConferenceInviteBinding>() {
    private val existMembers = mutableListOf<String>()
    private var groupId: String? = null
    private var selectedMembers:MutableList<String> = mutableListOf()

    companion object {
        private const val TAG = "ConferenceInvite"
    }

    override fun getViewBinding(inflater: LayoutInflater): DemoActivityConferenceInviteBinding? {
        return DemoActivityConferenceInviteBinding.inflate(inflater)
    }

    override fun initIntent(intent: Intent?) {
        super.initIntent(intent)
        intent?.let {
            groupId = it.getStringExtra(CallKitManager.EXTRA_CONFERENCE_GROUP_ID)
            it.getStringArrayExtra(CallKitManager.EXTRA_CONFERENCE_GROUP_EXIT_MEMBERS)?.let { members->
                if (members.isNotEmpty()) {
                    existMembers.addAll(members)
                }
            } ?: kotlin.run {
                existMembers.add(ChatClient.getInstance().currentUser)
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        if (groupId.isNullOrEmpty()) {
            ChatLog.e(TAG, "groupId is null or empty")
            finish()
            return
        }

        val fragment = ConferenceInviteFragment.newInstance(groupId!!, existMembers)
        fragment.setOnGroupMemberSelectedListener(object : OnContactSelectedListener {
            override fun onContactSelectedChanged(v: View, selectedMembers: MutableList<String>) {
                this@ConferenceInviteActivity.selectedMembers = selectedMembers
                resetMenuInfo(selectedMembers.size + existMembers.size)
            }
        })
        supportFragmentManager.beginTransaction().replace(binding.flFragment.id, fragment).commit()

        resetMenuInfo(existMembers.size)
    }

    private fun resetMenuInfo(size: Int) {
        binding.titleBar.getToolBar().menu.findItem(R.id.chat_menu_member_call).let {
            it.isEnabled = size - existMembers.size > 0
            it.title = getString(R.string.menu_member_call, size)
            it.title?.let { title->
                if (it.isEnabled) {
                    SpannableStringBuilder(title).let { span ->
                        span.setSpan(ForegroundColorSpan(ContextCompat.getColor(mContext, com.hyphenate.easeui.R.color.ease_color_primary))
                            , 0, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        it.title = span
                    }
                }
            }
        }
    }

    override fun initListener() {
        super.initListener()

        binding.titleBar.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.chat_menu_member_call -> {
                    if (selectedMembers.isEmpty()) {
                        showToast(R.string.tips_select_contacts_first)
                        return@setOnMenuItemClickListener true
                    }
                    val members = selectedMembers.toTypedArray()
                    val params: Map<String, Any> = mutableMapOf(CallKitManager.KEY_GROUPID to groupId!!)
                    EaseCallKit.getInstance().startInviteMultipleCall(members, params)
                    finish()
                    true
                }
                else -> false
            }
        }
        binding.titleBar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
        EaseCallKit.getInstance().startInviteMultipleCall(null, null)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            EaseCallKit.getInstance().startInviteMultipleCall(null, null)
        }
        return super.onKeyDown(keyCode, event)
    }

}