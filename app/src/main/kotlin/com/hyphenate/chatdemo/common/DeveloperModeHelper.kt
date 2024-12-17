package com.hyphenate.chatdemo.common

import com.hyphenate.chatdemo.DemoHelper

object DeveloperModeHelper {
    fun isRequestToAppServer():Boolean{
        val developerMode = DemoHelper.getInstance().getDataModel().isDeveloperMode()
        return !developerMode
    }
}