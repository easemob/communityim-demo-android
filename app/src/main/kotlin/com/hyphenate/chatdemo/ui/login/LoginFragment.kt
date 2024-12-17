package com.hyphenate.chatdemo.ui.login

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.core.content.ContextCompat
import androidx.core.text.clearSpans
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.hyphenate.chatdemo.DemoHelper
import com.hyphenate.chatdemo.MainActivity
import com.hyphenate.chatdemo.R
import com.hyphenate.chatdemo.common.CustomCountDownTimer
import com.hyphenate.chatdemo.common.DemoConstant
import com.hyphenate.chatdemo.common.PreferenceManager
import com.hyphenate.chatdemo.common.dialog.SimpleDialog
import com.hyphenate.chatdemo.common.extensions.internal.changePwdDrawable
import com.hyphenate.chatdemo.common.extensions.internal.clearEditTextListener
import com.hyphenate.chatdemo.common.extensions.internal.showRightDrawable
import com.hyphenate.chatdemo.databinding.DemoFragmentLoginBinding
import com.hyphenate.chatdemo.utils.PhoneNumberUtils
import com.hyphenate.chatdemo.utils.ToastUtils.showToast
import com.hyphenate.chatdemo.viewmodel.LoginFragmentViewModel
import com.hyphenate.easeui.base.ChatUIKitBaseFragment
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatError
import com.hyphenate.easeui.common.bus.ChatUIKitFlowBus
import com.hyphenate.easeui.common.extensions.catchChatException
import com.hyphenate.easeui.common.extensions.hideSoftKeyboard
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class LoginFragment : ChatUIKitBaseFragment<DemoFragmentLoginBinding>(), View.OnClickListener, TextWatcher,
    CompoundButton.OnCheckedChangeListener, OnEditorActionListener {
    private var mUserPhone: String? = null
    private var mCode: String? = null
    private lateinit var mFragmentViewModel: LoginFragmentViewModel
    private var clear: Drawable? = null
    private var eyeOpen: Drawable? = null
    private var eyeClose: Drawable? = null
    private val mHits = LongArray(COUNT)
    private var isDeveloperMode = false
    private var isShowingDialog = false
    private var countDownTimer: CustomCountDownTimer? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DemoFragmentLoginBinding? {
        return DemoFragmentLoginBinding.inflate(inflater)
    }

    override fun initListener() {
        super.initListener()
        binding?.run {
            etLoginPhone.addTextChangedListener(this@LoginFragment)
            etLoginCode.addTextChangedListener(this@LoginFragment)
            tvVersion.setOnClickListener(this@LoginFragment)
            btnLogin.setOnClickListener(this@LoginFragment)
            tvGetCode.setOnClickListener(this@LoginFragment)
            tvLoginDeveloper.setOnClickListener(this@LoginFragment)
            cbSelect.setOnCheckedChangeListener(this@LoginFragment)
            etLoginCode.setOnEditorActionListener(this@LoginFragment)
            etLoginPhone.clearEditTextListener()
            root.setOnClickListener {
                mContext.hideSoftKeyboard()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        mFragmentViewModel = ViewModelProvider(this)[LoginFragmentViewModel::class.java]
    }

    override fun initData() {
        super.initData()
        binding?.run {
            etLoginPhone.setText(ChatClient.getInstance().currentUser)
            tvVersion.text = "V${ChatClient.VERSION}"
            tvAgreement.text = spannable
            tvAgreement.movementMethod = LinkMovementMethod.getInstance()
            tvAgreement.setHintTextColor(Color.TRANSPARENT)
        }
        eyeClose = ContextCompat.getDrawable(mContext, R.drawable.d_pwd_hide)
        eyeOpen = ContextCompat.getDrawable(mContext, R.drawable.d_pwd_show)
        clear = ContextCompat.getDrawable(mContext, R.drawable.d_clear)
        binding?.etLoginPhone?.showRightDrawable(clear)
        isDeveloperMode = DemoHelper.getInstance().getDataModel().isDeveloperMode()
        resetView(isDeveloperMode)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_version -> {
                System.arraycopy(mHits, 1, mHits, 0, mHits.size - 1)
                mHits[mHits.size - 1] = SystemClock.uptimeMillis()
                if (mHits[0] >= SystemClock.uptimeMillis() - DURATION && !isShowingDialog) {
                    isShowingDialog = true
                    showOpenDeveloperDialog()
                }
            }

            R.id.btn_login -> {
                mContext?.hideSoftKeyboard()
                loginToServer()
            }

            R.id.tv_get_code -> {
                getVerificationCode()
            }

            R.id.tv_login_developer -> {
                ChatUIKitFlowBus.with<String>(DemoConstant.SKIP_DEVELOPER_CONFIG).post(lifecycleScope, LoginFragment::class.java.simpleName)
            }
        }
    }

    private fun loginToServer() {
        if (isDeveloperMode) {
            if (TextUtils.isEmpty(mUserPhone) || TextUtils.isEmpty(mCode)) {
                showToast(mContext!!.getString(R.string.em_login_btn_info_incomplete))
                return
            }
            if (binding?.cbSelect?.isChecked == false) {
                showToast(mContext!!.getString(R.string.em_login_not_select_agreement))
                return
            }
            lifecycleScope.launch {
                mFragmentViewModel.login(mUserPhone!!, mCode!!)
                    .onStart { showLoading(true) }
                    .onCompletion { dismissLoading() }
                    .catchChatException { e ->
                        if (e.errorCode == ChatError.USER_AUTHENTICATION_FAILED) {
                            showToast(R.string.demo_error_user_authentication_failed)
                        } else {
                            showToast(e.description)
                        }
                    }
                    .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
                    .collect {
                        if (it != null) {
                            DemoHelper.getInstance().getDataModel().initDb()
                            startActivity(Intent(mContext, MainActivity::class.java))
                            mContext.finish()
                        }
                    }
            }
        } else {
            if (mUserPhone.isNullOrEmpty()) {
                showToast(mContext.getString(R.string.em_login_phone_empty))
                return
            }
            if (!PhoneNumberUtils.isPhoneNumber(mUserPhone)) {
                showToast(mContext!!.getString(R.string.em_login_phone_illegal))
                return
            }
            if (mCode.isNullOrEmpty()) {
                showToast(R.string.em_login_code_empty)
                return
            }
            if (!PhoneNumberUtils.isNumber(mCode)) {
                showToast(mContext.getString(R.string.em_login_illegal_code))
                return
            }
            if (binding?.cbSelect?.isChecked == false) {
                showToast(mContext.getString(R.string.em_login_not_select_agreement))
                return
            }
            lifecycleScope.launch {
                mFragmentViewModel.loginFromAppServer(mUserPhone!!, mCode!!)
                    .onStart {
                        showLoading(true)
                    }
                    .onCompletion {
                        dismissLoading()
                    }
                    .catchChatException { e ->
                        if (e.errorCode == ChatError.USER_AUTHENTICATION_FAILED) {
                            showToast(R.string.demo_error_user_authentication_failed)
                        } else {
                            showToast(e.description)
                        }
                    }
                    .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
                    .collect {
                        if (it != null) {
                            DemoHelper.getInstance().getDataModel().initDb()
                            startActivity(Intent(mContext, MainActivity::class.java))
                            mContext.finish()
                        }
                    }
            }

        }
    }

    private fun getVerificationCode() {
        if (mUserPhone.isNullOrEmpty()) {
            showToast(mContext!!.getString(R.string.em_login_phone_empty))
            return
        }
        if (!PhoneNumberUtils.isPhoneNumber(mUserPhone)) {
            showToast(mContext!!.getString(R.string.em_login_phone_illegal))
            return
        }
        binding?.tvGetCode?.let {
            countDownTimer = CustomCountDownTimer(
                it,
                60000,
                1000
            )
        }
        lifecycleScope.launch {
            mFragmentViewModel.getVerificationCode(mUserPhone!!)
                .catchChatException { e ->
                    showToast(e.description)
                }
                .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
                .collect {
                    if (it != null) {
                        countDownTimer?.start()
                        showToast(R.string.em_login_post_code)
                    }
                }
        }
    }

    private fun showOpenDeveloperDialog() {
        SimpleDialog.Builder(mContext)
            .setTitle(
                if (isDeveloperMode) getString(R.string.server_close_develop_mode) else getString(
                    R.string.server_open_develop_mode
                )
            )
            .setPositiveButton{
                isDeveloperMode = !isDeveloperMode
                DemoHelper.getInstance().getDataModel()?.setDeveloperMode(isDeveloperMode)
                binding?.etLoginPhone?.setText("")
                resetView(isDeveloperMode)
            }
            .setOnDismissListener {
                isShowingDialog = false
            }
            .setCanceledOnTouchOutside(false)
            .build()
            .show()
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: Editable) {
        binding?.run {
            mUserPhone = etLoginPhone.text.toString().trim { it <= ' ' }
            mCode = etLoginCode.text.toString().trim { it <= ' ' }
            etLoginPhone.showRightDrawable(clear)
            if (isDeveloperMode) {
                etLoginCode.showRightDrawable(eyeClose)
            }
            setButtonEnable(!TextUtils.isEmpty(mUserPhone) && !TextUtils.isEmpty(mCode))
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.cb_select -> setButtonEnable(
                !TextUtils.isEmpty(mUserPhone) && !TextUtils.isEmpty(
                    mCode
                ) && isChecked
            )
        }
    }

    private fun setButtonEnable(enable: Boolean) {
        binding?.run {
            btnLogin.isEnabled = enable
            if (etLoginCode.hasFocus()) {
                etLoginCode.imeOptions =
                    if (enable) EditorInfo.IME_ACTION_DONE else EditorInfo.IME_ACTION_PREVIOUS
            } else if (etLoginPhone.hasFocus()) {
                etLoginCode.imeOptions =
                    if (enable) EditorInfo.IME_ACTION_DONE else EditorInfo.IME_ACTION_NEXT
            }
        }
    }

    private var spannable: SpannableString? = null
        private get() {
            if (field == null) {
                field = createSpannable()
            }
            return field
        }

    private fun createSpannable(): SpannableString {
        val language = PreferenceManager.getValue(DemoConstant.APP_LANGUAGE,Locale.getDefault().language)
        val isZh = language.startsWith("zh")
        val spanStr = SpannableString(getString(R.string.em_login_agreement))
        var start1 = 29
        var end1 = 45
        var start2 = 50
        var end2 = spanStr.length
        if (isZh) {
            start1 = 5
            end1 = 13
            start2 = 14
            end2 = spanStr.length
        }
        //设置下划线
        //spanStr.setSpan(new UnderlineSpan(), 3, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanStr.setSpan(object : MyClickableSpan() {
            override fun onClick(widget: View) {
                jumpToAgreement()
            }
        }, start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanStr.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(mContext, com.hyphenate.easeui.R.color.ease_color_primary)),
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
            ForegroundColorSpan(ContextCompat.getColor(mContext, com.hyphenate.easeui.R.color.ease_color_primary)),
            start2,
            end2,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spanStr
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (!TextUtils.isEmpty(mUserPhone) && !TextUtils.isEmpty(mCode)) {
                mContext.hideSoftKeyboard()
                loginToServer()
                return true
            }
        }
        return false
    }

    private fun resetView(isDeveloperMode: Boolean) {
        binding?.run {
            etLoginCode.setText("")
            if (isDeveloperMode) {
                etLoginPhone.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                etLoginCode.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                etLoginCode.setHint(R.string.em_login_password_hint)
                etLoginPhone.setHint(R.string.em_login_name_hint)
                tvGetCode.visibility = View.GONE
                tvLoginDeveloper.visibility = View.VISIBLE
                etLoginCode.changePwdDrawable(
                    eyeOpen,
                    eyeClose,
                    null,
                    null,
                    null
                )
            } else {
                etLoginPhone.inputType = InputType.TYPE_CLASS_PHONE
                etLoginCode.inputType = InputType.TYPE_CLASS_NUMBER
                etLoginCode.setHint(R.string.em_login_input_verification_code)
                etLoginPhone.setHint(R.string.register_phone_number)
                tvGetCode.visibility = View.VISIBLE
                tvLoginDeveloper.visibility = View.GONE
                etLoginCode.showRightDrawable(null)
                etLoginCode.clearEditTextListener()
                DemoHelper.getInstance().getDataModel().enableCustomSet(false)
                DemoHelper.getInstance().getDataModel().setDeveloperMode(false)
                ChatClient.getInstance().options.enableDNSConfig(true)
            }
        }
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
            ds.color = ContextCompat.getColor(mContext, R.color.transparent)
            ds.clearShadowLayer()
        }
    }

    override fun onDestroyView() {
        spannable?.clearSpans()
        spannable = null
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "LoginFragment"
        private const val COUNT: Int = 5
        private const val DURATION = (3 * 1000).toLong()
        private const val stopTimeoutMillis: Long = 5000
    }
}