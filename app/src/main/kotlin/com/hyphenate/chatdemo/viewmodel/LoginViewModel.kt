package com.hyphenate.chatdemo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.hyphenate.chatdemo.repository.EMClientRepository
import kotlinx.coroutines.flow.flow

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository: EMClientRepository = EMClientRepository()

    /**
     * Register account.
     * @param userName
     * @param pwd
     * @return
     */
    fun register(userName: String?, pwd: String?) =
        flow {
            emit(mRepository.registerToHx(userName, pwd))
        }

    /**
     * Logout from Chat server.
     */
    fun logout() =
        flow {
            emit(mRepository.logout(true))
        }

    /**
     * Cancel Account.
     */
    fun cancelAccount() =
        flow {
            emit(mRepository.cancelAccount())
        }

}