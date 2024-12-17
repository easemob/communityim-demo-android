package com.hyphenate.chatdemo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.hyphenate.chatdemo.repository.EMClientRepository
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow

class LoginFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository: EMClientRepository = EMClientRepository()

    /**
     * Login to Chat Server.
     * @param userName
     * @param pwd
     * @param isTokenFlag
     */
    fun login(userName: String, pwd: String, isTokenFlag: Boolean = false) =
        flow {
            emit(mRepository.loginToServer(userName, pwd, isTokenFlag))
        }

    /**
     * Login from app server.
     * @param userName
     * @param userPassword
     */
    fun loginFromAppServer(userName: String, userPassword: String) =
        flow {
            emit(mRepository.loginFromServer(userName, userPassword))
        }
            .flatMapConcat { result ->
                flow { emit(mRepository.loginToServer(result?.username!!, result.token!!, true)) }
            }

    /**
     * Get verification code.
     */
    fun getVerificationCode(phoneNumber: String?) =
        flow {
            emit(mRepository.getVerificationCode(phoneNumber))
        }

}
