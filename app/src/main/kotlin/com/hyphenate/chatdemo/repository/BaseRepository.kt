package com.hyphenate.chatdemo.repository

import android.content.Context
import com.hyphenate.chatdemo.DemoApplication

open class BaseRepository {
    fun getContext(): Context {
        return DemoApplication.getInstance()
    }
}