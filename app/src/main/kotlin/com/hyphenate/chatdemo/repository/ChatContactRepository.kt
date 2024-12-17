package com.hyphenate.chatdemo.repository

import com.hyphenate.chatdemo.BuildConfig
import com.hyphenate.chatdemo.DemoApplication
import com.hyphenate.chatdemo.R
import com.hyphenate.chatdemo.common.suspend.fetchResultContactsFromServer
import com.hyphenate.cloud.HttpClientManager
import com.hyphenate.easeui.ChatUIKitClient
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatError
import com.hyphenate.easeui.common.ChatException
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.ChatValueCallback
import com.hyphenate.easeui.model.ChatUIKitUser
import com.hyphenate.easeui.repository.ChatUIKitContactListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ChatContactRepository: ChatUIKitContactListRepository() {

    companion object{
        const val checkUrl = BuildConfig.APP_SERVER_PROTOCOL + "://" + BuildConfig.APP_SERVER_DOMAIN + BuildConfig.APP_BASE_USER
        const val OPERATOR = "?operator="
    }

    /**
     * Load server contacts.
     */
    override suspend fun loadData():List<ChatUIKitUser> =
        withContext(Dispatchers.IO){
            chatContactManager.fetchResultContactsFromServer()
        }

    suspend fun checkPhoneNumOrIdFromServer(phoneNumberOrId:String):String? =
        withContext(Dispatchers.IO){
            suspendCoroutine { continuation ->
                checkByServer(phoneNumberOrId,object : ChatValueCallback<String>{
                    override fun onSuccess(value: String?) {
                        continuation.resume(value)
                    }

                    override fun onError(code: Int, errorMsg: String?) {
                        continuation.resumeWithException(ChatException(code, errorMsg))
                    }
                })
            }
        }

    private fun checkByServer(
        phoneNumberOrId:String,
        callBack: ChatValueCallback<String>
    ){
        try {
            val headers: MutableMap<String, String> = HashMap()
            headers["Content-Type"] = "application/json"
            headers["Authorization"] = ChatClient.getInstance().accessToken
            val url = "$checkUrl/$phoneNumberOrId$OPERATOR${ChatUIKitClient.getCurrentUser()?.id}"
            ChatLog.d(" checkByServer url : ", url )
            val response = HttpClientManager.httpExecute(
                url,
                headers,
                "",
                HttpClientManager.Method_GET
            )
            val code = response.code
            val responseInfo = response.content
            if (code == 200) {
                ChatLog.d("checkByServer success : ", responseInfo)
                val `object` = JSONObject(responseInfo)
                val chatUserName = `object`.getString("chatUserName")
                val localContacts = ChatClient.getInstance().contactManager().contactsFromLocal
                localContacts?.let {
                    if (it.contains(chatUserName)){
                        callBack.onError(code, DemoApplication.getInstance().getString(R.string.demo_add_contact_already_exist))
                        return
                    }
                }
                callBack.onSuccess(chatUserName)
            }else {
                if (responseInfo != null && responseInfo.isNotEmpty()) {
                    var errorInfo: String? = null
                    try {
                        val responseObject = JSONObject(responseInfo)
                        errorInfo = responseObject.getString("errorInfo")
                        if (errorInfo.contains("App user does not exist")) {
                            errorInfo = DemoApplication.getInstance().getString(R.string.demo_add_contact_not_exist)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        errorInfo = responseInfo
                    }
                    callBack.onError(code, errorInfo)
                } else {
                    callBack.onError(code, responseInfo?:"")
                }
            }
        } catch (e: Exception) {
            callBack.onError(ChatError.NETWORK_ERROR, e.message)
        }
    }

}