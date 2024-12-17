package com.hyphenate.chatdemo.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.hyphenate.chatdemo.R
import com.hyphenate.chatdemo.base.BaseInitActivity
import com.hyphenate.chatdemo.common.DemoConstant
import com.hyphenate.chatdemo.databinding.DemoActivityLoginBinding
import com.hyphenate.easeui.common.bus.ChatUIKitFlowBus

class LoginActivity : BaseInitActivity<DemoActivityLoginBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        supportFragmentManager.beginTransaction().replace(R.id.fl_fragment, LoginFragment())
            .commit()
    }

    override fun getViewBinding(inflater: LayoutInflater): DemoActivityLoginBinding? {
        return DemoActivityLoginBinding.inflate(inflater)
    }

    override fun setActivityTheme() {
        setFitSystemForTheme(false, ContextCompat.getColor(this, R.color.transparent), true)
    }

    override fun initData() {
        super.initData()
        initEvent()
    }

    private fun initEvent() {
        ChatUIKitFlowBus.with<String>(DemoConstant.SKIP_DEVELOPER_CONFIG).register(this) {
            if (it == LoginFragment::class.java.simpleName) {
                replace(ServerSetFragment())
            }
        }
    }

    private fun replace(fragment: Fragment) {
        supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_in_from_right,
            R.anim.slide_out_to_left,
            R.anim.slide_in_from_left,
            R.anim.slide_out_to_right
        ).replace(R.id.fl_fragment, fragment).addToBackStack(null).commit()
    }

    companion object {
        fun startAction(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
    }
}