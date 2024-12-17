package com.hyphenate.chatdemo.repository

import com.hyphenate.chatdemo.BuildConfig
import com.hyphenate.cloud.HttpClientManager
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatError
import com.hyphenate.easeui.common.ChatException
import com.hyphenate.easeui.common.ChatGroup
import com.hyphenate.easeui.common.ChatValueCallback
import com.hyphenate.easeui.common.impl.OnError
import com.hyphenate.easeui.common.impl.OnSuccess
import com.hyphenate.util.EMLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GroupRepository: BaseRepository() {
    val groupManager = ChatClient.getInstance().groupManager()

    companion object{
        const val baseGroupUrl = BuildConfig.APP_SERVER_PROTOCOL + "://" + BuildConfig.APP_SERVER_DOMAIN + BuildConfig.APP_BASE_GROUP
    }

    /**
     * Suspend method for [ChatGroupManager.asyncGetJoinedGroupsFromServer()]
     */
    suspend fun asyncGetJoinedGroupsFromServer(): List<ChatGroup> {
        return suspendCoroutine { continuation->
            groupManager.asyncGetJoinedGroupsFromServer(object : ChatValueCallback<MutableList<ChatGroup>> {
                override fun onSuccess(value: MutableList<ChatGroup>) {
                    continuation.resume(value)
                }
                override fun onError(error: Int, errorMsg: String?) {
                    continuation.resumeWithException(ChatException(error, errorMsg))
                }
            })
        }
    }

    suspend fun reportGroupIdToServer(
        group:ChatGroup
    ):ChatGroup =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                reportToAppServer(
                    group,
                    onSuccess = {
                        continuation.resume(group)
                    },
                    onError = { code, error ->
                        continuation.resumeWithException(ChatException(code, error))
                    })
            }
        }


    private fun reportToAppServer(group:ChatGroup,onSuccess: OnSuccess, onError: OnError){
        val reportUrl = baseGroupUrl + "/" + group.groupId + "?appkey=" + ChatClient.getInstance().options.appKey
        val headers: MutableMap<String, String> = java.util.HashMap()
        headers["Content-Type"] = "application/json"
        EMLog.d("reportToAppServer url : ", reportUrl)
        val response = HttpClientManager.httpExecute(reportUrl, headers, null, HttpClientManager.Method_POST)
        val code = response.code
        val responseInfo = response.content
        try {
            if(code == 200) {
                onSuccess()
            }else{
                if (responseInfo != null && responseInfo.isNotEmpty()) {
                    var errorInfo: String? = null
                    try {
                        val responseObject = JSONObject(responseInfo)
                        errorInfo = responseObject.getString("error")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        errorInfo = responseInfo
                    }
                    onError(code, errorInfo)
                }else{
                    onError(code, responseInfo)
                }
            }
        }catch (e: java.lang.Exception) {
            onError(ChatError.NETWORK_ERROR, e.message)
        }
    }

}