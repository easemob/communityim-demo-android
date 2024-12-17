package com.hyphenate.chatdemo.callkit

import android.app.Application
import android.content.Context
import android.content.Intent
import android.text.SpannableStringBuilder
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.hyphenate.chatdemo.DemoHelper
import com.hyphenate.chatdemo.R
import com.hyphenate.chatdemo.callkit.extensions.getStringOrNull
import com.hyphenate.chatdemo.common.DemoConstant
import com.hyphenate.chatdemo.utils.ToastUtils
import com.hyphenate.easecallkit.EaseCallKit
import com.hyphenate.easecallkit.base.EaseCallEndReason
import com.hyphenate.easecallkit.base.EaseCallKitConfig
import com.hyphenate.easecallkit.base.EaseCallKitListener
import com.hyphenate.easecallkit.base.EaseCallKitTokenCallback
import com.hyphenate.easecallkit.base.EaseCallType
import com.hyphenate.easecallkit.base.EaseGetUserAccountCallback
import com.hyphenate.easecallkit.base.EaseUserAccount
import com.hyphenate.easeui.ChatUIKitClient
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatError
import com.hyphenate.easeui.common.ChatHttpClientManagerBuilder
import com.hyphenate.easeui.common.ChatHttpResponse
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.bus.ChatUIKitFlowBus
import com.hyphenate.easeui.common.dialog.SimpleListSheetDialog
import com.hyphenate.easeui.common.dialog.SimpleSheetType
import com.hyphenate.easeui.common.extensions.mainScope
import com.hyphenate.easeui.interfaces.SimpleListSheetItemClickListener
import com.hyphenate.easeui.model.ChatUIKitEvent
import com.hyphenate.easeui.model.ChatUIKitMenuItem
import com.hyphenate.easeui.provider.getSyncUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.TimeZone

object CallKitManager {

    private const val TAG = "EaseCallKitManager"
    private const val FETCH_TOKEN_URL = com.hyphenate.chatdemo.BuildConfig.APP_SERVER_PROTOCOL + "://" + com.hyphenate.chatdemo.BuildConfig.APP_SERVER_DOMAIN +
            com.hyphenate.chatdemo.BuildConfig.APP_RTC_TOKEN_URL
    private const val FETCH_USER_MAPPER = com.hyphenate.chatdemo.BuildConfig.APP_SERVER_PROTOCOL + "://" + com.hyphenate.chatdemo.BuildConfig.APP_SERVER_DOMAIN +
            com.hyphenate.chatdemo.BuildConfig.APP_RTC_CHANNEL_MAPPER_URL
    private const val PARAM_USER = "user"
    private const val PARAM_CHANNEL_NAME = "channelName"
    private const val PARAM_USER_APPKEY = "appkey"
    private const val RESULT_PARAM_TOKEN = "accessToken"
    private const val RESULT_PARAM_UID = "agoraUid"
    private const val RESULT_PARAM_RESULT = "result"
    const val KEY_GROUPID = "groupId"
    const val EXTRA_CONFERENCE_GROUP_ID = "group_id"
    const val EXTRA_CONFERENCE_GROUP_EXIT_MEMBERS = "exist_members"
    const val MSG_ATTR_CONF_ID = "conferenceId"

    /**
     * Whether it is a rtc call.
     */
    var isRtcCall = false

    /**
     * Rtc call type.
     */
    var rtcType = 0

    /**
     * If multiple call, should set groupId.
     */
    var currentCallGroupId: String? = null

    private val callKitListener by lazy { object :EaseCallKitListener {
        override fun onInviteUsers(context: Context?, users: Array<out String>?, ext: JSONObject?) {
            currentCallGroupId = ext?.getStringOrNull(KEY_GROUPID)
            Intent(context, ConferenceInviteActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(EXTRA_CONFERENCE_GROUP_ID, currentCallGroupId)
                putExtra(EXTRA_CONFERENCE_GROUP_EXIT_MEMBERS, users)
                context?.startActivity(this)
            }
        }

        override fun onEndCallWithReason(
            callType: EaseCallType?,
            channelName: String?,
            reason: EaseCallEndReason?,
            callTime: Long
        ) {
            ChatLog.d(
                TAG,
                "onEndCallWithReason: " + (callType?.name
                    ?: " callType is null ") + " reason:" + reason + " time:" + callTime + "channelName:$channelName"
            )
            val formatter = SimpleDateFormat("mm:ss")
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            val callString: String = DemoHelper.getInstance().context.getString(R.string.call_duration, formatter.format(callTime))
            ToastUtils.showToast(callString)
        }

        override fun onReceivedCall(callType: EaseCallType?, userId: String?, ext: JSONObject?) {
            ChatLog.e(TAG, "onReceivedCall: $callType, userId: $userId")
            // Can get groupId from ext
            ext?.getStringOrNull(KEY_GROUPID)?.let { groupId ->
                currentCallGroupId = groupId
                CallUserInfo(userId).getUserInfo(groupId).parse().apply {
                    EaseCallKit.getInstance().callKitConfig.setUserInfo(userId, this)
                }
            } ?: kotlin.run {
                currentCallGroupId = null
                CallUserInfo(userId).apply {
                    ChatUIKitClient.getUserProvider()?.getSyncUser(userId)?.let { user ->
                        this.nickName = user.getRemarkOrName()
                        this.headImage = user.avatar
                    }
                    EaseCallKit.getInstance().callKitConfig.setUserInfo(userId, this.parse())
                } // Single call
            }
        }

        override fun onCallError(
            type: EaseCallKit.EaseCallError?,
            errorCode: Int,
            description: String?
        ) {
            ChatLog.e(TAG, "onCallError: $type, errorCode: $errorCode, description: $description")
        }

        override fun onInViteCallMessageSent() {
            if (ChatClient.getInstance().options.isIncludeSendMessageInMessageListener.not()) {
                ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.ADD + ChatUIKitEvent.TYPE.MESSAGE)
                    .post(DemoHelper.getInstance().context.mainScope(), ChatUIKitEvent(DemoConstant.CALL_INVITE_MESSAGE, ChatUIKitEvent.TYPE.MESSAGE))
            }
        }

        override fun onGenerateToken(
            userId: String?,
            channelName: String?,
            agoraAppId: String?,
            callback: EaseCallKitTokenCallback?
        ) {
            SpannableStringBuilder(FETCH_TOKEN_URL).apply {
                append("/$channelName/$PARAM_USER/$userId")
                getRtcToken(this.toString(), callback)
            }
        }

        override fun onRemoteUserJoinChannel(
            channelName: String?,
            userName: String?,
            uid: Int,
            callback: EaseGetUserAccountCallback?
        ) {
            // Only multi call callback this method
            if (userName.isNullOrEmpty()) {
                SpannableStringBuilder(FETCH_USER_MAPPER).apply {
                    append("?$PARAM_CHANNEL_NAME=$channelName")
                    getAllUsersByUid(this.toString(), callback)
                }
            } else {
                // Set user info to call kit.
                CallUserInfo(userName).getUserInfo(currentCallGroupId).parse().apply {
                    EaseCallKit.getInstance().callKitConfig.setUserInfo(userId, this)
                }
                callback?.onUserAccount(listOf(EaseUserAccount(uid, userName)))
            }
        }

    } }

    fun init(context: Context) {
        EaseCallKitConfig().apply {
            // Set call timeout.
            callTimeOut = 30 * 1000
            // Set RTC appId.
            agoraAppId = com.hyphenate.chatdemo.BuildConfig.RTC_APPID
            // Set whether token verification is required.
            isEnableRTCToken = true
            EaseCallKit.getInstance().init(context, this)
        }
        // Register the activityLifecycleCallbacks to monitor the activity lifecycle.
        (context.applicationContext as Application).registerActivityLifecycleCallbacks(CallKitActivityLifecycleCallback())
        // Register the activities which you have registered in manifest
        EaseCallKit.getInstance().registerVideoCallClass(VideoCallActivity::class.java)
        EaseCallKit.getInstance().registerMultipleVideoClass(MultipleVideoActivity::class.java)
        EaseCallKit.getInstance().setCallKitListener(callKitListener)
    }

    /**
     * Show single chat video call dialog.
     */
    fun showSelectDialog(context: Context, conversationId: String?) {
        val context = (context as FragmentActivity)
        val mutableListOf = mutableListOf(
            ChatUIKitMenuItem(
                menuId = R.id.chat_video_call_voice,
                title = context.getString(R.string.voice_call),
                resourceId = R.drawable.phone_pick,
                titleColor = ContextCompat.getColor(context, com.hyphenate.easeui.R.color.ease_color_primary),
                resourceTintColor = ContextCompat.getColor(context, com.hyphenate.easeui.R.color.ease_color_primary)
            ),
            ChatUIKitMenuItem(
                menuId = R.id.chat_video_call_video,
                title = context.getString(R.string.video_call),
                resourceId =  R.drawable.video_camera,
                titleColor = ContextCompat.getColor(context, com.hyphenate.easeui.R.color.ease_color_primary),
                resourceTintColor = ContextCompat.getColor(context, com.hyphenate.easeui.R.color.ease_color_primary)
            ),
        )
        val dialog = SimpleListSheetDialog(
            context = context,
            itemList = mutableListOf,
            type = SimpleSheetType.ITEM_LAYOUT_DIRECTION_START)
        dialog.setSimpleListSheetItemClickListener(object : SimpleListSheetItemClickListener {
            override fun onItemClickListener(position: Int, menu: ChatUIKitMenuItem) {
                dialog.dismiss()
                when(menu.menuId){
                    R.id.chat_video_call_voice -> {
                        startSingleAudioCall(conversationId)
                    }
                    R.id.chat_video_call_video -> {
                        startSingleVideoCall(conversationId)
                    }
                    else -> {}
                }
            }
        })
        context.supportFragmentManager.let { dialog?.show(it,"video_call_dialog") }
    }

    /**
     * Start single audio call.
     */
    fun startSingleAudioCall(conversationId: String?) {
        EaseCallKit.getInstance().startSingleCall(
            EaseCallType.SINGLE_VOICE_CALL, conversationId, null,
            VideoCallActivity::class.java
        )
    }

    /**
     * Start single video call.
     */
    fun startSingleVideoCall(conversationId: String?) {
        EaseCallKit.getInstance().startSingleCall(
            EaseCallType.SINGLE_VIDEO_CALL, conversationId, null,
            VideoCallActivity::class.java
        )
    }

    /**
     * Start conference call.
     */
    fun startConferenceCall(context: Context, groupId: String?) {
        val intent = Intent(context, ConferenceInviteActivity::class.java)
        intent.putExtra(EXTRA_CONFERENCE_GROUP_ID, groupId)
        context.startActivity(intent)
    }

    /**
     * Receive call push.
     */
    fun receiveCallPush(context: Context) {
        if (isRtcCall) {
            if (EaseCallType.getfrom(rtcType) != EaseCallType.CONFERENCE_CALL) {
                startVideoCallActivity(context)
            } else {
                startMultipleVideoActivity(context)
            }
            isRtcCall = false
        }
    }

    private fun startVideoCallActivity(context: Context) {
        Intent(context, VideoCallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(this)
        }
    }

    private fun startMultipleVideoActivity(context: Context) {
        Intent(context, MultipleVideoActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(this)
        }
    }

    /**
     * Get rtc token from server.
     */
    private fun getRtcToken(tokenUrl: String, callback: EaseCallKitTokenCallback?) {
        executeGetRequest(tokenUrl) {
            it?.let { response ->
                ChatLog.d(TAG, "getRtcToken: url:$tokenUrl ${response.code}, ${response.content}")
                if (response.code == 200) {
                    response.content?.let { body ->
                        try {
                            val result = JSONObject(body)
                            val token = result.getString(RESULT_PARAM_TOKEN)
                            val uid = result.getInt(RESULT_PARAM_UID)
                            callback?.onSetToken(token, uid)
                        } catch (e: Exception) {
                            e.stackTrace
                            callback?.onGetTokenError(ChatError.GENERAL_ERROR, e.message)
                        }
                    }
                } else {
                    callback?.onGetTokenError(response.code, response.content)
                }
            } ?: kotlin.run {
                callback?.onSetToken(null, 0)
            }
        }
    }

    private fun getAllUsersByUid(url: String, callback: EaseGetUserAccountCallback?) {
        executeGetRequest(url) {
            it?.let { response ->
                ChatLog.d(TAG, "getAllUsersByUid: url:$url ${response.code}, ${response.content}")
                if (response.code == 200) {
                    response.content?.let { body ->
                        try {
                            val result = JSONObject(body)
                            val userList = result.getJSONObject(RESULT_PARAM_RESULT)
                            val userAccountList = mutableListOf<EaseUserAccount>()
                            userList.keys().forEach { uIdStr ->
                                val userId = userList.optString(uIdStr)
                                // Set user info to call kit.
                                CallUserInfo(userId).getUserInfo(currentCallGroupId).parse().apply {
                                    EaseCallKit.getInstance().callKitConfig.setUserInfo(userId, this)
                                }
                                userAccountList.add(EaseUserAccount(uIdStr.toInt(), userId))
                            }
                            callback?.onUserAccount(userAccountList)
                        } catch (e: Exception) {
                            e.stackTrace
                            callback?.onSetUserAccountError(ChatError.GENERAL_ERROR, e.message)
                        }
                    }
                } else {
                    callback?.onSetUserAccountError(response.code, response.content)
                }
            } ?: kotlin.run {
                callback?.onSetUserAccountError(ChatError.GENERAL_ERROR, "response is null")
            }
        }
    }

    /**
     * Base get request.
     */
    private fun executeGetRequest(url: String, callback: (ChatHttpResponse?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            ChatHttpClientManagerBuilder()
                .get()
                .setUrl(url)
                .withToken(true)
                .execute()?.let { response ->
                    callback(response)
                } ?: kotlin.run {
                callback(null)
            }
        }
    }

}