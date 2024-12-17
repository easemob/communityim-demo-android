package com.hyphenate.chatdemo.ui.contact

import androidx.lifecycle.ViewModelProvider
import com.hyphenate.chatdemo.viewmodel.ChatContactViewModel
import com.hyphenate.easeui.common.extensions.showToast
import com.hyphenate.easeui.feature.invitation.ChatUIKitNewRequestsActivity

class ChatNewRequestActivity : ChatUIKitNewRequestsActivity(){

    private val chatContactViewModel by lazy { ViewModelProvider(this)[ChatContactViewModel::class.java] }
    override fun initView() {
        super.initView()
        setContactViewModel(chatContactViewModel)
    }

    override fun addContactFail(code: Int, error: String) {
        if (code == 200 ){
            mContext.showToast(error)
        }else if (code == 404){
            mContext.showToast(error)
        }
    }

}