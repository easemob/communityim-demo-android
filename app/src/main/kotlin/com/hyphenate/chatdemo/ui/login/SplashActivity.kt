package com.hyphenate.chatdemo.ui.login

import android.animation.Animator
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.hyphenate.chatdemo.DemoHelper
import com.hyphenate.chatdemo.MainActivity
import com.hyphenate.chatdemo.R
import com.hyphenate.chatdemo.base.BaseInitActivity
import com.hyphenate.chatdemo.common.dialog.DemoAgreementDialogFragment
import com.hyphenate.chatdemo.common.dialog.DemoDialogFragment
import com.hyphenate.chatdemo.common.dialog.SimpleDialog
import com.hyphenate.chatdemo.databinding.DemoSplashActivityBinding
import com.hyphenate.chatdemo.viewmodel.SplashViewModel
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.extensions.catchChatException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class SplashActivity : BaseInitActivity<DemoSplashActivityBinding>() {
    private lateinit var model: SplashViewModel

    override fun getViewBinding(inflater: LayoutInflater): DemoSplashActivityBinding? {
        return DemoSplashActivityBinding.inflate(inflater)
    }

    override fun setActivityTheme() {
        setFitSystemForTheme(false, ContextCompat.getColor(this, R.color.transparent), true)
    }

    override fun initData() {
        super.initData()
        model = ViewModelProvider(this)[SplashViewModel::class.java]
        binding.ivSplash.animate()
            .alpha(1f)
            .setDuration(500)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    checkIfAgreePrivacy()
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            .start()
        binding.tvProduct.animate()
            .alpha(1f)
            .setDuration(500)
            .start()
    }

    private fun checkIfAgreePrivacy() {
        if (DemoHelper.getInstance().getDataModel().isAgreeAgreement().not()) {
            showPrivacyDialog()
        } else {
            checkSDKValid()
        }
    }

    private fun checkSDKValid() {
        if (DemoHelper.getInstance().hasAppKey.not()) {
            showAlertDialog(R.string.splash_not_appkey)
        } else {
            if (DemoHelper.getInstance().isSDKInited().not()) {
                showAlertDialog(R.string.splash_not_init)
            } else {
                loginSDK()
            }
        }
    }

    private fun showPrivacyDialog() {
        DemoAgreementDialogFragment.Builder(mContext as AppCompatActivity)
            .setTitle(R.string.demo_login_dialog_title)
            .setOnConfirmClickListener(
                R.string.demo_login_dialog_confirm,
                object : DemoDialogFragment.OnConfirmClickListener {
                    override fun onConfirmClick(view: View?) {
                        DemoHelper.getInstance().getDataModel().setAgreeAgreement(true)
                        DemoHelper.getInstance().initSDK()
                        checkSDKValid()
                    }
                })
            .setOnCancelClickListener(
                R.string.demo_login_dialog_cancel,
                object : DemoDialogFragment.OnCancelClickListener {
                    override fun onCancelClick(view: View?) {
                        exitProcess(1)
                    }
                })
            .show()
    }

    private fun showAlertDialog(@StringRes title: Int) {
        SimpleDialog.Builder(mContext)
            .setTitle(getString(title))
            .setPositiveButton(getString(R.string.confirm)) {
                exitProcess(1)
            }
            .dismissNegativeButton()
            .show()
    }

    private fun loginSDK() {
        lifecycleScope.launch {
            model.loginData()
                .catchChatException { e ->
                    ChatLog.e("TAG", "error message = " + e.description)
                    LoginActivity.startAction(mContext)
                    finish()
                }
                .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(5000), false)
                .collect {
                    if (it) {
                        DemoHelper.getInstance().getDataModel().initDb()
                        startActivity(Intent(mContext, MainActivity::class.java))
                        finish()
                    }
                }
        }
    }
}