package com.hyphenate.chatdemo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.hyphenate.chatdemo.repository.EMClientRepository
import kotlinx.coroutines.flow.flow

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository: EMClientRepository = EMClientRepository()

    fun loginData() = flow {
        emit(mRepository.loadAllInfoFromHX())
    }

}