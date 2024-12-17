package com.hyphenate.chatdemo.ui.me

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import com.hyphenate.chatdemo.DemoHelper
import com.hyphenate.chatdemo.R
import com.hyphenate.chatdemo.common.DemoConstant
import com.hyphenate.chatdemo.common.DeveloperModeHelper
import com.hyphenate.chatdemo.databinding.DemoActivityMeInformationBinding
import com.hyphenate.chatdemo.ui.me.controller.CameraAndCroppingController
import com.hyphenate.chatdemo.utils.CameraAndCropFileUtils
import com.hyphenate.chatdemo.viewmodel.ProfileInfoViewModel
import com.hyphenate.easeui.ChatUIKitClient
import com.hyphenate.easeui.base.ChatUIKitBaseActivity
import com.hyphenate.easeui.common.ChatImageUtils
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.bus.ChatUIKitFlowBus
import com.hyphenate.easeui.common.dialog.SimpleListSheetDialog
import com.hyphenate.easeui.common.extensions.catchChatException
import com.hyphenate.easeui.common.extensions.dpToPx
import com.hyphenate.easeui.common.extensions.mainScope
import com.hyphenate.easeui.common.extensions.showToast
import com.hyphenate.easeui.common.permission.PermissionCompat
import com.hyphenate.easeui.common.utils.ChatUIKitCompat
import com.hyphenate.easeui.common.utils.ChatUIKitFileUtils
import com.hyphenate.easeui.configs.setAvatarStyle
import com.hyphenate.easeui.interfaces.SimpleListSheetItemClickListener
import com.hyphenate.easeui.model.ChatUIKitEvent
import com.hyphenate.easeui.model.ChatUIKitMenuItem
import com.hyphenate.easeui.model.ChatUIKitProfile
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File


class UserInformationActivity:ChatUIKitBaseActivity<DemoActivityMeInformationBinding>(),
    View.OnClickListener {

    private val cameraAndCroppingController: CameraAndCroppingController by lazy {
        CameraAndCroppingController(mContext)
    }
    private var selfProfile:ChatUIKitProfile? = null
    private var showSelectDialog:SimpleListSheetDialog? = null
    private var imageUri:Uri?= null
    private lateinit var model: ProfileInfoViewModel

    override fun getViewBinding(inflater: LayoutInflater): DemoActivityMeInformationBinding? {
        return DemoActivityMeInformationBinding.inflate(inflater)
    }
    companion object {
        private const val REQUEST_CODE_STORAGE_PICTURE = 111
        private const val REQUEST_CODE_CAMERA = 112
        private const val REQUEST_CODE_LOCAL_EDIT = 113
        private const val RESULT_CODE_CAMERA = 114
        private const val RESULT_CODE_LOCAL = 115
        private const val RESULT_CODE_UPDATE_NAME = 116
        private const val RESULT_REFRESH = "isRefresh"
    }

    private val requestCameraPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            onRequestResult(
                result,
                REQUEST_CODE_CAMERA
            )
        }

    private val requestImagePermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            onRequestResult(
                result,
                REQUEST_CODE_STORAGE_PICTURE
            )
        }

    private val launcherToCamera: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> onActivityResult(result, RESULT_CODE_CAMERA)
    }
    private val launcherToAlbum: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> onActivityResult(result, RESULT_CODE_LOCAL) }

    private val launcherToUpdateName: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> onActivityResult(result, RESULT_CODE_UPDATE_NAME) }


    private val launcherToMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        if (uri != null) {
            ChatLog.d("launcherToMedia", "Selected URI: $uri")
            cameraAndCroppingController.gotoCrop(uri)
        } else {
            ChatLog.d("launcherToMedia", "No media selected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListener()
        initData()
    }

    private fun initView(){
        ChatUIKitClient.getConfig()?.avatarConfig?.setAvatarStyle(binding.ivAvatar)
        binding.run {
            ivAvatar.setRadius(8.dpToPx(this@UserInformationActivity))
            ivAvatar.scaleType = ImageView.ScaleType.CENTER_CROP
        }
        updateLocalData()
    }

    private fun initListener(){
        binding.run {
            titleBar.setNavigationOnClickListener{ finish() }
            avatarLayout.setOnClickListener(this@UserInformationActivity)
            nickNameLayout.setOnClickListener(this@UserInformationActivity)
        }
    }

    private fun initData(){
        model = ViewModelProvider(this)[ProfileInfoViewModel::class.java]
    }

    private fun updateLocalData(){
        binding.run {
            selfProfile = ChatUIKitClient.getCurrentUser()
            selfProfile?.let { profile->
                val ph = AppCompatResources.getDrawable(this@UserInformationActivity, com.hyphenate.easeui.R.drawable.uikit_default_avatar)
                val ep = AppCompatResources.getDrawable(this@UserInformationActivity, com.hyphenate.easeui.R.drawable.uikit_default_avatar)
                ivAvatar.load(profile.avatar ?: ph) {
                    placeholder(ph)
                    error(ep)
                }
                tvNickName.text = profile.getNotEmptyName()
            }
        }
    }

    private fun showSelectDialog(){
        val context = this@UserInformationActivity
        showSelectDialog = SimpleListSheetDialog(
            context = context,
            itemList = mutableListOf(
                ChatUIKitMenuItem(
                    menuId = R.id.about_information_camera,
                    title = getString(R.string.main_about_me_information_camera),
                    titleColor = ContextCompat.getColor(context, com.hyphenate.easeui.R.color.ease_color_primary)
                ),
                ChatUIKitMenuItem(
                    menuId = R.id.about_information_picture,
                    title = getString(R.string.main_about_me_information_picture),
                    titleColor = ContextCompat.getColor(context, com.hyphenate.easeui.R.color.ease_color_primary)
                )
            ),
            itemListener = object : SimpleListSheetItemClickListener {
                override fun onItemClickListener(position: Int, menu: ChatUIKitMenuItem) {
                    simpleMenuItemClickListener(menu)
                    showSelectDialog?.dismiss()
                }
            })
        supportFragmentManager.let { showSelectDialog?.show(it,"image_select_dialog") }
    }

    fun simpleMenuItemClickListener(menu: ChatUIKitMenuItem){
        when(menu.menuId){
            R.id.about_information_camera -> {
                if (PermissionCompat.checkPermission(
                        mContext,
                        requestCameraPermission,
                        Manifest.permission.CAMERA,
                    )
                ) {
                    cameraAndCroppingController.selectPicFromCamera(launcherToCamera)
                }
            }
            R.id.about_information_picture -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2){
                    val mimeType = "image/*"
                    launcherToMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.SingleMimeType(mimeType)))
                }else{
                    if (PermissionCompat.checkMediaPermission(
                            mContext,
                            requestImagePermission,
                            Manifest.permission.READ_MEDIA_IMAGES
                        )
                    ) {
                        selectPicFromLocal(launcherToAlbum)
                    }
                }
            }
            else -> {}
        }
    }


    private fun onRequestResult(result: Map<String, Boolean>?, requestCode: Int) {
        if (!result.isNullOrEmpty()) {
            for ((key, value) in result) {
                ChatLog.e("UserInformationActivity", "onRequestResult: $key  $value")
            }
            if (PermissionCompat.getMediaAccess(mContext) !== PermissionCompat.StorageAccess.Denied) {
                if (requestCode == REQUEST_CODE_STORAGE_PICTURE) {
                    selectPicFromLocal(launcherToAlbum)
                }else if (requestCode == REQUEST_CODE_CAMERA){
                    cameraAndCroppingController.selectPicFromCamera(launcherToCamera)
                }else if (requestCode == REQUEST_CODE_LOCAL_EDIT){
                    imageUri?.let { cameraAndCroppingController.gotoCrop(it) }
                }
            }
        }
    }

    /**
     * It's the result from ActivityResultLauncher.
     * @param result
     * @param requestCode
     */
    private fun onActivityResult(result: ActivityResult, requestCode: Int) {
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            when (requestCode) {
                RESULT_CODE_CAMERA -> { // capture new image
                    onActivityResultForCamera(data)
                }
                RESULT_CODE_LOCAL -> {
                    onActivityResultForLocalPhotos(data)
                }
                RESULT_CODE_UPDATE_NAME -> {
                    data?.let {
                        if (it.hasExtra(RESULT_REFRESH) && it.getBooleanExtra(RESULT_REFRESH,false)){
                            it.getStringExtra("nickname")?.let { name ->
                                updateUsername(name)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
            if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
                val resultUri = UCrop.getOutput(data)
                resultUri?.let { it1 ->
                    val result = ChatImageUtils.checkDegreeAndRestoreImage(mContext,it1)
                    imageUri = result
                    val path = CameraAndCropFileUtils.getAbsolutePathFromUri(mContext, result)
                    ChatLog.e("UserInformationActivity","onActivityResult corp path $path")
                    path?.let {
                        uploadFile(it)
                    }
                }
            } else if (resultCode == UCrop.RESULT_ERROR) {
                val cropError = UCrop.getError(data)
                ChatLog.e("UserInformationActivity","onActivityResult corp error ${cropError?.message}")
            } else {

            }
        }
    }

    private fun onActivityResultForCamera(data: Intent?) {
        val imageUri = cameraAndCroppingController.resultForCamera(data)
        val result = ChatImageUtils.checkDegreeAndRestoreImage(mContext,imageUri)
        this.imageUri = result
        imageUri?.let {
            cameraAndCroppingController.gotoCrop(it)
        }
    }

    private fun onActivityResultForLocalPhotos(data: Intent?) {
        if (data != null) {
            val selectedImage = data.data
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2){
                selectedImage?.let { cameraAndCroppingController.gotoCrop(it) }
            }else{
                if (selectedImage != null) {
                    var filePath: String = ChatUIKitFileUtils.getFilePath(mContext, selectedImage)
                    if (!TextUtils.isEmpty(filePath) && File(filePath).exists()) {
                        imageUri = Uri.parse(filePath)
                    } else {
                        imageUri = selectedImage
                        selectedImage.path?.let {
                            filePath = it
                        }
                    }
                    imageUri?.let { cameraAndCroppingController.gotoCrop(it) }
                }
            }
        }
    }

    /**
     * select local image
     */
    private fun selectPicFromLocal(launcher: ActivityResultLauncher<Intent>?) {
        ChatUIKitCompat.openImageByLauncher(launcher, mContext)
    }

    private fun updateUserAvatar(){
        selfProfile = ChatUIKitClient.getCurrentUser()
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE + ChatUIKitEvent.TYPE.CONTACT)
            .post(lifecycleScope, ChatUIKitEvent(DemoConstant.EVENT_UPDATE_SELF, ChatUIKitEvent.TYPE.CONTACT))
    }

    private fun updateUsername(nickname: String){
        lifecycleScope.launch {
            model.updateUserNickName(nickname)
                .onStart {
                    showLoading(true)
                }
                .onCompletion { dismissLoading() }
                .catchChatException { e ->
                    ChatLog.e("TAG", "updateUserNickName fail error message = " + e.description)
                    mainScope().launch {
                        mContext.showToast("updateUsername error ${e.errorCode} ${e.description}")
                    }
                }
                .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(5000), null)
                .collect {

                    ChatUIKitClient.getCurrentUser()?.let {profile ->
                        profile.name = nickname
                        DemoHelper.getInstance().getDataModel().insertUser(profile)
                        ChatUIKitClient.updateCurrentUser(profile)
                    }
                    binding.tvNickName.text = nickname
                    ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE + ChatUIKitEvent.TYPE.CONTACT)
                        .post(lifecycleScope, ChatUIKitEvent(DemoConstant.EVENT_UPDATE_SELF, ChatUIKitEvent.TYPE.CONTACT))
                }
        }
    }

    private fun uploadFile(filePath:String?){
        ChatLog.e("UserInformationActivity","uploadFile filePath $filePath")
        val scaledImage = ChatImageUtils.getScaledImageByUri(this,filePath)
        lifecycleScope.launch {
            model.uploadAvatar(scaledImage)
                .onStart {
                    showLoading(true)
                }
                .onCompletion { dismissLoading() }
                .catchChatException { e ->
                    ChatLog.e("UserInformationActivity", "uploadAvatar fail error message = " + e.description)
                    mainScope().launch {
                        mContext.showToast("uploadFile error ${e.errorCode} ${e.description}")
                    }
                }
                .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(5000), null)
                .collect {
                    it?.let {
                        binding.ivAvatar.setImageURI(imageUri)
                        updateUserAvatar()
                    }
                }
        }
    }

    override fun onClick(v: View?) {
       when(v?.id){
           R.id.avatar_layout -> {
               if (DeveloperModeHelper.isRequestToAppServer()){
                   showSelectDialog()
               }else{
                   mainScope().launch {
                       mContext.showToast(mContext.getString(R.string.main_information_checked_model))
                   }
               }
           }
           R.id.nick_name_layout -> {
               launcherToUpdateName.launch(Intent(
                   this@UserInformationActivity,
                   EditUserNicknameActivity::class.java))
           }
           else -> {}
       }
    }

}