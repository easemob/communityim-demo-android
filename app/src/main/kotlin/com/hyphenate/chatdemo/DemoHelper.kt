package com.hyphenate.chatdemo

import android.content.Context
import android.util.Log
import com.hyphenate.chatdemo.callkit.CallKitManager
import com.hyphenate.chatdemo.common.DemoDataModel
import com.hyphenate.chatdemo.common.ListenersWrapper
import com.hyphenate.chatdemo.common.extensions.internal.checkAppKey
import com.hyphenate.chatdemo.common.push.PushManager
import com.hyphenate.chatdemo.uikit.UIKitManager
import com.hyphenate.easeui.ChatUIKitClient
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatOptions
import com.hyphenate.easeui.common.PushConfigBuilder

class DemoHelper private constructor(){

    private lateinit var dataModel: DemoDataModel
    var hasAppKey = false
    lateinit var context: Context

    @Synchronized
    fun init(context: Context) {
        this.context = context.applicationContext
        dataModel = DemoDataModel(context)
    }

    fun getDataModel(): DemoDataModel {
        return dataModel
    }

    /**
     * Check if the SDK has been initialized.
     */
    fun isSDKInited(): Boolean {
        return ChatUIKitClient.isInited()
    }

    /**
     * Initialize the SDK.
     */
    @Synchronized
    fun initSDK() {
        if (::context.isInitialized.not()) {
            Log.e(TAG, "Please call init method first.")
            return
        }
        initChatOptions(context).apply {
            hasAppKey = checkAppKey(context)
            if (!hasAppKey) {
                Log.e(TAG, "App key is null or empty.")
                return
            }
            // Register necessary listeners
            ListenersWrapper.registerListeners()
            ChatUIKitClient.init(context, this)
            if (ChatUIKitClient.isInited()) {
                // debug mode, you'd better set it to false, if you want release your App officially.
                ChatClient.getInstance().setDebugMode(true)
                // Initialize push.
                initPush()
                // Set the UIKit options.
                addUIKitSettings()
                // Initialize the callkit module.
                initCallKit()
            }
        }
    }

    private fun addUIKitSettings() {
        UIKitManager.addUIKitSettings(context)
    }

    private fun initPush() {
        PushManager.initPush(context)
    }

    /**
     * Get the notifier.
     */
    fun getNotifier() = ChatUIKitClient.getNotifier()

    private fun initCallKit() {
        CallKitManager.init(context)
    }

    /**
     * Set chat options.
     * Note: Developers need to set the options according to needs.
     */
    private fun initChatOptions(context: Context): ChatOptions {
        return ChatOptions().apply {
            // set the appkey
            appKey = BuildConfig.APPKEY
            // set if accept the invitation automatically, default true
            acceptInvitationAlways = false
            // set if you need read ack
            requireAck = true
            // Set whether the sent message is included in the message listener, default false
            isIncludeSendMessageInMessageListener = true

            getDataModel().setUseFCM(true)

            /**
             * Note: Developers need to apply your own push accounts and replace the following
             */
            pushConfig = PushConfigBuilder(context)
                .enableVivoPush(getDataModel().isAgreeAgreement())  // need to configure appid and appkey in AndroidManifest.xml
                .enableMiPush(BuildConfig.MI_PUSH_APPID, BuildConfig.MI_PUSH_APPKEY)
                .enableOppoPush(BuildConfig.OPPO_PUSH_APPKEY, BuildConfig.OPPO_PUSH_APPSECRET)
                .enableHWPush()                                     // need to configure appid in AndroidManifest.xml
                .enableFCM(BuildConfig.FCM_SENDERID)
                .enableHonorPush()                                  // need to configure appid in AndroidManifest.xml
                .build()

            if (dataModel.isDeveloperMode()) {

                if (dataModel.getCustomAppKey().isNotEmpty()){
                    dataModel.getCustomAppKey().let {
                        appKey = it
                    }
                }

                if (dataModel.isCustomServerEnable()) {
                    // Turn off DNS configuration
                    enableDNSConfig(false)
                    restServer = dataModel.getRestServer()?.ifEmpty { null }
                    setIMServer(dataModel.getIMServer()?.let {
                        if (it.contains(":")) {
                            imPort = it.split(":")[1].toInt()
                            it.split(":")[0]
                        } else {
                            it.ifEmpty { null }
                        }
                    })
                    val port = dataModel.getIMServerPort()
                    if (port != 0) {
                        imPort = port
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "DemoHelper"
        private var instance: DemoHelper? = null
        fun getInstance(): DemoHelper {
            if (instance == null) {
                synchronized(DemoHelper::class.java) {
                    if (instance == null) {
                        instance = DemoHelper()
                    }
                }
            }
            return instance!!
        }
    }
}