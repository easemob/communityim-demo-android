package com.hyphenate.chatdemo.common.push

import android.app.Application
import android.content.Context
import android.text.TextUtils
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import com.heytap.msp.push.HeytapPushManager
import com.hihonor.push.sdk.HonorPushCallback
import com.hihonor.push.sdk.HonorPushClient
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException
import com.hyphenate.chat.EMClient
import com.hyphenate.chatdemo.DemoHelper
import com.hyphenate.chatdemo.common.PushActivityLifecycleCallback
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.ChatPushHelper
import com.hyphenate.easeui.common.ChatPushListener
import com.hyphenate.easeui.common.ChatPushType
import com.hyphenate.easeui.common.extensions.isMainProcess
import com.hyphenate.push.EMPushConfig
import com.hyphenate.push.EMPushType

object PushManager {

    /**
     * Initialize push.
     */
    fun initPush(context: Context) {
        if (context.isMainProcess()) {
            // Register push activity lifecycle callback.
            (context.applicationContext as? Application)?.registerActivityLifecycleCallbacks(PushActivityLifecycleCallback())
            //OPPO SDK升级到2.1.0后需要进行初始化
            HeytapPushManager.init(context, true)

            // 荣耀推送 7.0.41.301及以上版本
            // 无需调用init初始化SDK即可调用
            val isSupportHonor = HonorPushClient.getInstance().checkSupportHonorPush(context)
            if (isSupportHonor) {
                // true，调用初始化接口时SDK会同时进行异步请求PushToken。会触发HonorMessageService.onNewToken(String)回调。
                // false，不会异步请求PushToken，需要应用主动请求获取PushToken。
                HonorPushClient.getInstance().init(context, false)
            }

            // Set pushListener to control the push type.
            ChatPushHelper.getInstance().setPushListener(object : ChatPushListener() {
                override fun onBindTokenSuccess(pushType: EMPushType?, pushToken: String?) {
                    ChatLog.d("PushManager", "onBindTokenSuccess: pushType: $pushType, pushToken: $pushToken")
                }

                override fun onError(pushType: EMPushType?, errorCode: Long) {
                    // 返回的errorCode仅9xx为环信内部错误，可从EMError中查询，其他错误请根据pushType去相应第三方推送网站查询。
                    ChatLog.e("PushManager", "onError: pushType: $pushType, errorCode: $errorCode")
                }

                override fun isSupportPush(
                    pushType: EMPushType?,
                    pushConfig: EMPushConfig?
                ): Boolean {
                    if (pushType == ChatPushType.FCM) {
                        ChatLog.d("FCM",
                            "GooglePlayServiceCode:" + GoogleApiAvailabilityLight.getInstance()
                                .isGooglePlayServicesAvailable(context)
                        )
                        return DemoHelper.getInstance().getDataModel().isUseFCM()
                                && GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
                    } else if (pushType == ChatPushType.HONORPUSH) {
                        return isSupportHonor
                    }
                    return super.isSupportPush(pushType, pushConfig)
                }

            })
        }
    }

    /**
     * Get the push token and send to Chat Server.
     */
    fun getPushTokenAndSend(context: Context) {
        // Get the honor push token.
        if (HonorPushClient.getInstance().checkSupportHonorPush(context)) {
            // get the honor push token.
            HonorPushClient.getInstance().getPushToken(object : HonorPushCallback<String> {
                override fun onSuccess(token: String?) {
                    ChatLog.d("HonorPushClient", "getHonorPushToken onSuccess: $token")
                    ChatClient.getInstance().sendHonorPushTokenToServer(token)
                }

                override fun onFailure(code: Int, error: String?) {
                    ChatLog.e(
                        "HonorPushClient",
                        "getPushToken onFailure: $code error:$error"
                    )
                }

            } )
        } else {
            // Get HMS push token.
            getHMSToken(context)
        }

        // Get FCM push token.
        getFCMTokenAndSend(context)
    }


    /**
     * 申请华为Push Token
     * 1、getToken接口只有在AppGallery Connect平台开通服务后申请token才会返回成功。
     *
     * 2、EMUI10.0及以上版本的华为设备上，getToken接口直接返回token。如果当次调用失败Push会缓存申请，之后会自动重试申请，成功后则以onNewToken接口返回。
     *
     * 3、低于EMUI10.0的华为设备上，getToken接口如果返回为空，确保Push服务开通的情况下，结果后续以onNewToken接口返回。
     *
     * 4、服务端识别token过期后刷新token，以onNewToken接口返回。
     */
    private fun getHMSToken(context: Context) {
        // If turn on the FCM push, return
        if (ChatClient.getInstance().isFCMAvailable) {
            return
        }
        try {
            if (Class.forName("com.huawei.hms.api.HuaweiApiClient") != null) {
                val classType = Class.forName("android.os.SystemProperties")
                val getMethod = classType.getDeclaredMethod(
                    "get", *arrayOf<Class<*>>(
                        String::class.java
                    )
                )
                val buildVersion =
                    getMethod.invoke(classType, *arrayOf<Any>("ro.build.version.emui")) as String
                //在某些手机上，invoke方法不报错
                if (!TextUtils.isEmpty(buildVersion)) {
                    ChatLog.d("HWHMSPush", "huawei hms push is available!")
                    object : Thread() {
                        override fun run() {
                            try {
                                // read from agconnect-services.json
//                                String appId = AGConnectServicesConfig.fromContext(activity).getString("client/app_id");
                                val appId = AGConnectOptionsBuilder().build(context)
                                    .getString("client/app_id")
                                ChatLog.e("AGConnectOptionsBuilder", "appId:$appId")
                                // 申请华为推送token
                                val token =
                                    HmsInstanceId.getInstance(context).getToken(appId, "HCM")
                                ChatLog.d("HWHMSPush", "get huawei hms push token:$token")
                                if (token != null && token != "") {
                                    //没有失败回调，假定token失败时token为null
                                    ChatLog.d(
                                        "HWHMSPush",
                                        "register huawei hms push token success token:$token"
                                    )
                                    // 上传华为推送token
                                    EMClient.getInstance().sendHMSPushTokenToServer(token)
                                } else {
                                    ChatLog.e("HWHMSPush", "register huawei hms push token fail!")
                                }
                            } catch (e: ApiException) {
                                ChatLog.e(
                                    "HWHMSPush",
                                    "get huawei hms push token failed, $e"
                                )
                            }
                        }
                    }.start()
                } else {
                    ChatLog.d("HWHMSPush", "huawei hms push is unavailable!")
                }
            } else {
                ChatLog.d("HWHMSPush", "no huawei hms push sdk or mobile is not a huawei phone")
            }
        } catch (e: Exception) {
            ChatLog.d("HWHMSPush", "no huawei hms push sdk or mobile is not a huawei phone")
        }
    }


    /**
     * Get FCM push token and send to Chat Server.
     */
    private fun getFCMTokenAndSend(context: Context) {
        if (DemoHelper.getInstance().getDataModel().isUseFCM()
            && GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
            // Enable FCM automatic initialization
            if (FirebaseMessaging.getInstance().isAutoInitEnabled.not()) {
                FirebaseMessaging.getInstance().isAutoInitEnabled = true
                FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(true)
            }
            // Get FCM push token and send to Chat Server.
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                if (it.isSuccessful.not()) {
                    ChatLog.e("FCM", "get FCM push token failed: ${it.exception}")
                    return@addOnCompleteListener
                }
                val token = it.result
                ChatLog.d("FCM", "get FCM push token: $token")
                ChatClient.getInstance().sendFCMTokenToServer(token)
            }
        }
    }
}