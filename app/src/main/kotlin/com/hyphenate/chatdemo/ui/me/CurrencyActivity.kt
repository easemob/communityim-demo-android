package com.hyphenate.chatdemo.ui.me

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import com.hyphenate.chatdemo.DemoHelper
import com.hyphenate.chatdemo.R
import com.hyphenate.chatdemo.bean.LanguageType
import com.hyphenate.chatdemo.common.DemoConstant
import com.hyphenate.chatdemo.common.LanguageUtil
import com.hyphenate.chatdemo.common.PreferenceManager
import com.hyphenate.chatdemo.common.extensions.internal.setSwitchDefaultStyle
import com.hyphenate.chatdemo.databinding.DemoActivityCurrencyBinding
import com.hyphenate.easeui.ChatUIKitClient
import com.hyphenate.easeui.base.ChatUIKitBaseActivity
import java.util.Locale

class CurrencyActivity:ChatUIKitBaseActivity<DemoActivityCurrencyBinding>(),View.OnClickListener {
    private var targetLanguage:String? = ""
    private var appLanguage:String? = ""
    private var messageStyle:String? = ""
    private var extendStyle:String? = ""

    override fun getViewBinding(inflater: LayoutInflater): DemoActivityCurrencyBinding {
        return DemoActivityCurrencyBinding.inflate(inflater)
    }
    companion object {
        private const val LANGUAGE_TYPE_APPLICATION = 0
        private const val LANGUAGE_TYPE_TARGET = 1
        private const val MESSAGE_STYLE = 0
        private const val EXTEND_STYLE = 1
        private const val RESULT_CHOICE_APP_LANGUAGE = 100
        private const val RESULT_CHOICE_TARGET_LANGUAGE = 101
        private const val RESULT_STYLE_SETTING = 102
        private const val RESULT_LANGUAGE_TAG = "language_tag"
        private const val RESULT_LANGUAGE_CODE = "language_code"
        private const val LANGUAGE_TYPE = "language_type"
        private const val STYLE_TYPE = "style_type"
        private const val STYLE_NUMBER = "style_number"
    }

    private val launcherToAppLanguage: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> onActivityResult(result, RESULT_CHOICE_APP_LANGUAGE) }

    private val launcherToTargetLanguage: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> onActivityResult(result, RESULT_CHOICE_TARGET_LANGUAGE) }

    private val launcherToStyleSetting: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> onActivityResult(result, RESULT_STYLE_SETTING) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListener()
    }

    private fun initView(){
        initSwitch()
        initLanguage()
        initStyle()
    }

    private fun initListener(){
        binding.let {
            it.switchItemDark.setOnClickListener(this)
            it.arrowItemFeature.setOnClickListener(this)
            it.arrowItemLanguage.setOnClickListener(this)
            it.arrowItemTargetLanguage.setOnClickListener(this)
            it.arrowItemMessageStyle.setOnClickListener(this)
            it.arrowItemExtendStyle.setOnClickListener(this)
            it.titleBar.setNavigationOnClickListener{
                mContext.onBackPressed()
            }
        }
    }

    private fun initSwitch(){
        val isBlack = DemoHelper.getInstance().getDataModel().getBoolean(DemoConstant.IS_BLACK_THEME)

        val handler = Looper.myLooper()?.let { Handler(it) }

        handler?.postDelayed({
            binding.switchItemDark.setChecked(isBlack)
            binding.switchItemDark.setSwitchDefaultStyle()
        }, 200)

    }

    private fun initLanguage(){
        val spTagLanguage = PreferenceManager.getValue(DemoConstant.TARGET_LANGUAGE, LanguageType.EN.value)
        val spAppLanguage = PreferenceManager.getValue(DemoConstant.APP_LANGUAGE, Locale.getDefault().language)

        if (spTagLanguage == LanguageType.ZH.value){
            targetLanguage = getString(R.string.currency_language_zh_cn)
        }else if (spTagLanguage == LanguageType.EN.value){
            targetLanguage = getString(R.string.currency_language_en)
        }

        if (spAppLanguage == LanguageType.ZH.value){
            appLanguage = getString(R.string.currency_language_zh_cn)
        }else if (spAppLanguage == LanguageType.EN.value){
            appLanguage = getString(R.string.currency_language_en)
        }
        updateLanguage()
    }

    private fun initStyle(){
        PreferenceManager.getValue(DemoConstant.MSG_STYLE,true).let {
            messageStyle = if (it){
                getString(R.string.currency_feature_style_num1)
            } else{
                getString(R.string.currency_feature_style_num2)
            }
        }
        PreferenceManager.getValue(DemoConstant.EXTEND_STYLE,true).let {
            extendStyle = if (it){
                getString(R.string.currency_feature_style_num1)
            } else {
                getString(R.string.currency_feature_style_num2)
            }
        }
        updateStyle()
    }

    private fun onActivityResult(result: ActivityResult, requestCode: Int) {
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                if (it.hasExtra(RESULT_LANGUAGE_TAG) && it.hasExtra(RESULT_LANGUAGE_CODE)){
                    val tag = it.getStringExtra(RESULT_LANGUAGE_TAG)
                    val code = it.getStringExtra(RESULT_LANGUAGE_CODE)
                    when (requestCode) {
                        RESULT_CHOICE_APP_LANGUAGE -> {
                            appLanguage = tag
                            code?.let { languageCode->
                                PreferenceManager.putValue(DemoConstant.APP_LANGUAGE, languageCode)
                                LanguageUtil.changeLanguage(languageCode)
                            }
                        }
                        RESULT_CHOICE_TARGET_LANGUAGE -> {
                            targetLanguage = tag
                            code?.let { languageCode->
                                PreferenceManager.putValue(DemoConstant.TARGET_LANGUAGE, languageCode)
                                ChatUIKitClient.getConfig()?.chatConfig?.targetTranslationLanguage = languageCode
                            }
                        }
                        else -> {}
                    }
                    updateLanguage()
                }else if (it.hasExtra(STYLE_TYPE) && it.hasExtra(STYLE_NUMBER)){
                    val type = it.getIntExtra(STYLE_TYPE,0)
                    val tag = it.getStringExtra(STYLE_NUMBER)
                    when (requestCode) {
                        RESULT_STYLE_SETTING -> {
                            if (type == MESSAGE_STYLE){
                                messageStyle = tag
                            }else{
                                extendStyle = tag
                            }
                        }
                        else -> {}
                    }
                    updateStyle()
                }
            }
        }
    }

    private fun changeTheme(isChecked: Boolean){
        DemoHelper.getInstance().getDataModel().putBoolean(DemoConstant.IS_BLACK_THEME, isChecked)
        AppCompatDelegate.setDefaultNightMode(if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun updateLanguage(){
        binding.let {
            it.arrowItemLanguage.setContent(appLanguage?:"")
            it.arrowItemTargetLanguage.setContent(targetLanguage?:"")
        }
    }

    private fun updateStyle(){
        binding.let {
            it.arrowItemMessageStyle.setContent(messageStyle?:"")
            it.arrowItemExtendStyle.setContent(extendStyle?:"")
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.arrow_item_feature -> {
                startActivity(Intent(this@CurrencyActivity,FeaturesActivity::class.java))
            }
            R.id.arrow_item_language -> {
                val intent = Intent(this@CurrencyActivity, LanguageSettingActivity::class.java)
                intent.putExtra(LANGUAGE_TYPE, LANGUAGE_TYPE_APPLICATION)
                launcherToAppLanguage.launch(intent)
            }
            R.id.arrow_item_target_language -> {
                val intent = Intent(this@CurrencyActivity, LanguageSettingActivity::class.java)
                intent.putExtra(LANGUAGE_TYPE, LANGUAGE_TYPE_TARGET)
                launcherToTargetLanguage.launch(intent)
            }
            R.id.switch_item_dark -> {
                binding.switchItemDark.switch?.let { switch ->
                    val isChecked = switch.isChecked.not()
                    binding.switchItemDark.setChecked(isChecked)
                    changeTheme(isChecked)
                }
            }
            R.id.arrow_item_message_style -> {
                val intent = Intent(this@CurrencyActivity, StyleSettingActivity::class.java)
                intent.putExtra(STYLE_TYPE, MESSAGE_STYLE)
                launcherToStyleSetting.launch(intent)
            }
            R.id.arrow_item_extend_style -> {
                val intent = Intent(this@CurrencyActivity, StyleSettingActivity::class.java)
                intent.putExtra(STYLE_TYPE, EXTEND_STYLE)
                launcherToStyleSetting.launch(intent)
            }
            else -> {}
        }
    }


}