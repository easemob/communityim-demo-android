package com.hyphenate.chatdemo.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.hyphenate.chatdemo.R
import com.hyphenate.easeui.common.extensions.showToast

object ReportHelper {

    fun openEmailClient(context: Context,conversationId:String? = "") {
        try {
            val emailIntent = Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                val content = if (conversationId.isNullOrEmpty()) "" else "($conversationId)"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(context.resources.getString(R.string.about_complaint_suggestions_email)))
                putExtra(Intent.EXTRA_SUBJECT, context.resources.getString(R.string.about_complaint_suggestions,content))
                putExtra(Intent.EXTRA_TEXT, context.resources.getString(R.string.about_report_body))
            }
            context.startActivity(Intent.createChooser(emailIntent,context.getString(R.string.demo_select_email)))
        }catch (e:android.content.ActivityNotFoundException){
            context.showToast(context.resources.getString(R.string.demo_email_available))
        }
    }

    fun openEmailClient(context: Context,email: String,subject: String) {
        try {
            val emailIntent = Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, subject)
            }
            context.startActivity(Intent.createChooser(emailIntent,context.getString(R.string.demo_select_email)))
        }catch (e:android.content.ActivityNotFoundException){
            context.showToast(context.resources.getString(R.string.demo_email_available))
        }
    }
}