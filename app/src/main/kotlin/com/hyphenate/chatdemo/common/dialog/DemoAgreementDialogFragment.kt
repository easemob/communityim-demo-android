package com.hyphenate.chatdemo.common.dialog

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.hyphenate.chatdemo.R
import com.hyphenate.chatdemo.common.DemoConstant
import com.hyphenate.chatdemo.common.PreferenceManager
import java.util.Locale

class DemoAgreementDialogFragment : DemoDialogFragment() {
    override val middleLayoutId: Int
        get() = R.layout.demo_fragment_middle_agreement

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val tv_privacy = findViewById<TextView>(R.id.tv_privacy)
        tv_privacy?.text = spannable
        tv_privacy?.movementMethod = LinkMovementMethod.getInstance()
        mBtnDialogConfirm?.setTextColor(ContextCompat.getColor(requireContext(), com.hyphenate.easeui.R.color.ease_color_primary))
    }

    override fun initData() {
        super.initData()
        if (dialog != null) {
            dialog!!.setCancelable(false)
            dialog!!.setCanceledOnTouchOutside(false)
        }
    }

    private val spannable: SpannableString
        private get() {
            val language = PreferenceManager.getValue(DemoConstant.APP_LANGUAGE, Locale.getDefault().language)
            val isZh = language.startsWith("zh")
            val spanStr = SpannableString(getString(R.string.demo_login_dialog_content_privacy))
            var start1 = 18
            var end1 = 25
            var start2 = 30
            var end2 = 44
            if (isZh) {
                start1 = 5
                end1 = 13
                start2 = 14
                end2 = 22
            }
            //设置下划线
            //spanStr.setSpan(new UnderlineSpan(), 3, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanStr.setSpan(object : MyClickableSpan() {
                override fun onClick(widget: View) {
                    jumpToAgreement()
                }
            }, start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spanStr.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), com.hyphenate.easeui.R.color.ease_color_primary)),
                start1,
                end1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            //spanStr.setSpan(new UnderlineSpan(), 10, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanStr.setSpan(object : MyClickableSpan() {
                override fun onClick(widget: View) {
                    jumpToProtocol()
                }
            }, start2, end2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spanStr.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), com.hyphenate.easeui.R.color.ease_color_primary)),
                start2,
                end2,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spanStr
        }

    private fun jumpToAgreement() {
        val uri = Uri.parse("http://www.easemob.com/agreement")
        val it = Intent(Intent.ACTION_VIEW, uri)
        startActivity(it)
    }

    private fun jumpToProtocol() {
        val uri = Uri.parse("http://www.easemob.com/protocol")
        val it = Intent(Intent.ACTION_VIEW, uri)
        startActivity(it)
    }

    private abstract inner class MyClickableSpan : ClickableSpan() {
        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.bgColor = Color.TRANSPARENT
        }
    }

    class Builder(context: AppCompatActivity) : DemoDialogFragment.Builder(context) {
        override val fragment: DemoDialogFragment
            protected get() = DemoAgreementDialogFragment()
    }
}