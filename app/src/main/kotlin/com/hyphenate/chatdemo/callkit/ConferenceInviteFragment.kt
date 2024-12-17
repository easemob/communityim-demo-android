package com.hyphenate.chatdemo.callkit

import android.os.Bundle
import android.view.View
import com.hyphenate.easeui.base.ChatUIKitBaseRecyclerViewAdapter
import com.hyphenate.easeui.common.ChatUIKitConstant
import com.hyphenate.easeui.feature.group.fragments.ChatUIKitGroupMemberFragment
import com.hyphenate.easeui.feature.search.interfaces.OnContactSelectListener
import com.hyphenate.easeui.interfaces.OnContactSelectedListener
import com.hyphenate.easeui.model.ChatUIKitUser

class ConferenceInviteFragment: ChatUIKitGroupMemberFragment() {
    companion object {
        fun newInstance(groupId: String, existMembers: MutableList<String>): ConferenceInviteFragment {
            val fragment = ConferenceInviteFragment()
            val bundle = Bundle()
            bundle.putString(ChatUIKitConstant.EXTRA_CONVERSATION_ID, groupId)
            bundle.putStringArrayList(CallKitManager.EXTRA_CONFERENCE_GROUP_EXIT_MEMBERS, ArrayList(existMembers))
            fragment.arguments = bundle
            return fragment
        }
    }

    private var existMembers: MutableList<String> = mutableListOf()
    private var selectedMembers:MutableList<String> = mutableListOf()
    private var contactSelectedListener: OnContactSelectedListener? = null

    override fun initAdapter(): ChatUIKitBaseRecyclerViewAdapter<ChatUIKitUser> {
        return ConferenceInviteAdapter(groupId)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        arguments?.let {
            existMembers = it.getStringArrayList(CallKitManager.EXTRA_CONFERENCE_GROUP_EXIT_MEMBERS) ?: mutableListOf()
        }
        binding?.srlContactRefresh?.setEnableLoadMore(false)
    }

    override fun initListener() {
        super.initListener()
        if (mListAdapter is ConferenceInviteAdapter) {
            (mListAdapter as ConferenceInviteAdapter).setCheckBoxSelectListener(object :
                OnContactSelectListener {

                override fun onContactSelectedChanged(
                    v: View,
                    userId: String,
                    isSelected: Boolean
                ) {
                    if (isSelected){
                        if (!selectedMembers.contains(userId)){
                            selectedMembers.add(userId)
                        }
                    }else{
                        if (selectedMembers.contains(userId)){
                            selectedMembers.remove(userId)
                        }
                    }
                    contactSelectedListener?.onContactSelectedChanged(v,selectedMembers)
                }
            })
        }
    }

    override fun initData() {
        super.initData()
        if (mListAdapter is ConferenceInviteAdapter) {
            (mListAdapter as ConferenceInviteAdapter).setExistMembers(existMembers)
        }
    }

    /**
     * Set the listener for the group member selection.
     */
    fun setOnGroupMemberSelectedListener(listener: OnContactSelectedListener?){
        this.contactSelectedListener = listener
    }
}