package com.hyphenate.chatdemo.ui.me

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.hyphenate.chatdemo.BuildConfig
import com.hyphenate.chatdemo.R
import com.hyphenate.chatdemo.common.ReportHelper
import com.hyphenate.chatdemo.databinding.DemoActivityAboutBinding
import com.hyphenate.easeui.base.ChatUIKitBaseActivity
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.permission.PermissionCompat

class AboutActivity:ChatUIKitBaseActivity<DemoActivityAboutBinding>(), View.OnClickListener {
    companion object{
        private const val RESULT_CALL_PHONE = 111
    }

    override fun getViewBinding(inflater: LayoutInflater): DemoActivityAboutBinding? {
        return DemoActivityAboutBinding.inflate(inflater)
    }

    private val requestCallPhonePermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            onRequestResult(
                result,
                RESULT_CALL_PHONE
            )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListener()
    }

    private fun initView(){
        binding.let {
            it.tvVersion.text = getString(R.string.about_version,BuildConfig.VERSION_NAME)
            it.tvKitVersion.text = getString(R.string.about_uikit_version,BuildConfig.VERSION_NAME)
        }
    }

    private fun initListener(){
        binding.let {
            it.titleBar.setNavigationOnClickListener{
                mContext.onBackPressed()
            }
            it.arrowItemOfficialWebsite.setOnClickListener(this)
            it.arrowItemServiceHotline.setOnClickListener(this)
            it.arrowItemBusinessCooperation.setOnClickListener(this)
            it.arrowItemChannelCooperation.setOnClickListener(this)
            it.arrowItemComplaintSuggestions.setOnClickListener(this)
            it.arrowItemPrivacyPolicy.setOnClickListener(this)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.arrow_item_official_website -> {
                WebViewActivity.actionStart(this@AboutActivity,WebViewLoadType.RemoteUrl)
            }
            R.id.arrow_item_service_hotline -> {
                if (PermissionCompat.checkPermission(
                        mContext,
                        requestCallPhonePermission,
                        Manifest.permission.CALL_PHONE
                    )
                ) {
                    makingCall()
                }
            }
            R.id.arrow_item_business_cooperation -> {
                ReportHelper.openEmailClient(this,getString(R.string.about_business_cooperation_email),"")
            }
            R.id.arrow_item_channel_cooperation -> {
                ReportHelper.openEmailClient(this,getString(R.string.about_channel_cooperation_email),"")
            }
            R.id.arrow_item_complaint_suggestions -> {
                ReportHelper.openEmailClient(this)
            }
            R.id.arrow_item_privacy_policy -> {
                WebViewActivity.actionStart(this@AboutActivity,WebViewLoadType.LocalHtml)
            }
            else -> {}
        }
    }

    private fun onRequestResult(result: Map<String, Boolean>?, requestCode: Int) {
        if (!result.isNullOrEmpty()) {
            for ((key, value) in result) {
                ChatLog.e("AboutActivity", "onRequestResult: $key  $value")
            }
            if (PermissionCompat.getMediaAccess(mContext) !== PermissionCompat.StorageAccess.Denied) {
                if (requestCode == RESULT_CALL_PHONE) {
                    makingCall()
                }
            }
        }
    }

    private fun makingCall(){
        val dialIntent = Intent(
            Intent.ACTION_DIAL,
            Uri.parse("tel:4006221776")
        )
        startActivity(dialIntent)
    }

}