package com.hyphenate.chatdemo.common.extensions.internal

import android.content.Context
import android.content.pm.PackageManager
import com.hyphenate.easeui.common.ChatOptions

/**
 * Check if set the app key.
 */
internal fun ChatOptions.checkAppKey(context: Context): Boolean {
    if (appKey.isNullOrEmpty().not()) {
        return true
    }
    val appPackageName = context.packageName
    try {
        context.packageManager.getApplicationInfo(appPackageName, PackageManager.GET_META_DATA)?.let { info ->
            info.metaData?.getString("EASEMOB_APPKEY")?.let { key ->
                if (key.isNullOrEmpty().not()) {
                    return true
                }
            }
        }
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return false
}