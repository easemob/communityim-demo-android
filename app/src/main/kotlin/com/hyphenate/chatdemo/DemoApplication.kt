package com.hyphenate.chatdemo

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.hyphenate.chatdemo.base.UserActivityLifecycleCallbacks
import com.hyphenate.chatdemo.bean.LanguageType
import com.hyphenate.chatdemo.common.DemoConstant
import com.hyphenate.chatdemo.common.LanguageUtil
import com.hyphenate.chatdemo.common.PreferenceManager
import com.hyphenate.easeui.ChatUIKitClient
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.bugly.crashreport.CrashReport

class DemoApplication: Application() {
    private val mLifecycleCallbacks = UserActivityLifecycleCallbacks()
    override fun onCreate() {
        super.onCreate()
        instance = this
        registerActivityLifecycleCallbacks()

        DemoHelper.getInstance().init(this)
        initSDK()
        initFeatureConfig()
        initBugly()
    }

    private fun initSDK() {
        if (DemoHelper.getInstance().getDataModel().isAgreeAgreement()) {
            DemoHelper.getInstance().initSDK()
        }
    }

    private fun initFeatureConfig(){
        // Call this method after ChatUIKitClient#init
        val isBlack = DemoHelper.getInstance().getDataModel().getBoolean(DemoConstant.IS_BLACK_THEME)
        AppCompatDelegate.setDefaultNightMode(if (isBlack) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

        val enableTranslation = DemoHelper.getInstance().getDataModel().getBoolean(DemoConstant.FEATURES_TRANSLATION,true)
        val enableThread = DemoHelper.getInstance().getDataModel().getBoolean(DemoConstant.FEATURES_THREAD,true)
        val enableReaction = DemoHelper.getInstance().getDataModel().getBoolean(DemoConstant.FEATURES_REACTION,true)
        val enableTyping = DemoHelper.getInstance().getDataModel().getBoolean(DemoConstant.IS_TYPING_ON,false)

        PreferenceManager.getValue(DemoConstant.MSG_STYLE,true).let {
            ChatUIKitClient.getConfig()?.chatConfig?.enableWxMessageStyle = it
        }
        PreferenceManager.getValue(DemoConstant.EXTEND_STYLE,true).let {
            ChatUIKitClient.getConfig()?.chatConfig?.enableWxExtendStyle = it
        }

        val appLanguage = PreferenceManager.getValue(DemoConstant.APP_LANGUAGE,"")
        appLanguage.let {
            if (it.isNotEmpty()){
                LanguageUtil.changeLanguage(it)
            }
        }
        val targetLanguage = PreferenceManager.getValue(DemoConstant.TARGET_LANGUAGE,LanguageType.EN.value)
        ChatUIKitClient.getConfig()?.chatConfig?.targetTranslationLanguage = targetLanguage

        ChatUIKitClient.getConfig()?.chatConfig?.enableTranslationMessage = enableTranslation
        ChatUIKitClient.getConfig()?.chatConfig?.enableChatThreadMessage = enableThread
        ChatUIKitClient.getConfig()?.chatConfig?.enableMessageReaction = enableReaction
        ChatUIKitClient.getConfig()?.chatConfig?.enableChatTyping = enableTyping
    }

    private fun initBugly(){
        CrashReport.initCrashReport(applicationContext)
    }

    private fun registerActivityLifecycleCallbacks() {
        this.registerActivityLifecycleCallbacks(mLifecycleCallbacks)
    }

    fun getLifecycleCallbacks(): UserActivityLifecycleCallbacks {
        return mLifecycleCallbacks
    }

    /**
     * Set default settings for SmartRefreshLayout
     */

    init {
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
            ClassicsHeader(context)
        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, layout ->
            ClassicsFooter(context)
        }
    }

    companion object {
        private lateinit var instance: DemoApplication
        fun getInstance(): DemoApplication {
            return instance
        }
    }

}