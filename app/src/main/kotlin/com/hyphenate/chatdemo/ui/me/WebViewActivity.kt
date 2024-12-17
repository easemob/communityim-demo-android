package com.hyphenate.chatdemo.ui.me

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.webkit.WebViewClient
import com.hyphenate.chatdemo.R
import com.hyphenate.chatdemo.databinding.DemoActivityWebviewBinding
import com.hyphenate.easeui.base.ChatUIKitBaseActivity

class WebViewActivity : ChatUIKitBaseActivity<DemoActivityWebviewBinding>() {
    private var url = "https://www.easemob.com/"

    override fun getViewBinding(inflater: LayoutInflater): DemoActivityWebviewBinding? {
        return DemoActivityWebviewBinding.inflate(inflater)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if ( intent.hasExtra(LOAD_TYPE) ){
            val type =  WebViewLoadType.from(intent.getIntExtra(LOAD_TYPE,0))
            url = if (type == WebViewLoadType.LocalHtml){
                binding.titleBar.setTitle(getString(R.string.about_privacy_policy))
                "file:///android_asset/privacy_protocol.html"
            }else{
                "https://www.easemob.com/"
            }
        }

        binding.let {
            val webSettings = it.wbView.settings
            webSettings.javaScriptEnabled = true
            it.wbView.webViewClient = WebViewClient()
            it.wbView.loadUrl(url)
            it.titleBar.setNavigationOnClickListener{
                mContext.onBackPressed()
            }
        }
    }

    companion object {
        private const val LOAD_TYPE = "webView_load_type"
        fun actionStart(context: Context,type:WebViewLoadType) {
            Intent(context, WebViewActivity::class.java).apply {
                putExtra(LOAD_TYPE, type.ordinal)
                context.startActivity(this)
            }
        }
    }
}

enum class WebViewLoadType(val value:Int){
    RemoteUrl(0),
    LocalHtml(1);

    companion object {
        fun from(value: Int): WebViewLoadType {
            val types = WebViewLoadType.values()
            val length = types.size
            for (i in 0 until length) {
                val type = types[i]
                if (type.value == value) {
                    return type
                }
            }
            return RemoteUrl
        }
    }
}