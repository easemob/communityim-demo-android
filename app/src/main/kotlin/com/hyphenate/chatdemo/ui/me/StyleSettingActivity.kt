package com.hyphenate.chatdemo.ui.me

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hyphenate.chatdemo.R
import com.hyphenate.chatdemo.common.DemoConstant
import com.hyphenate.chatdemo.common.PreferenceManager
import com.hyphenate.chatdemo.databinding.DemoStyleSettingLayoutBinding
import com.hyphenate.easeui.ChatUIKitClient
import com.hyphenate.easeui.base.ChatUIKitBaseActivity

class StyleSettingActivity: ChatUIKitBaseActivity<DemoStyleSettingLayoutBinding>() {
    companion object {
        private const val STYLE_TYPE = "style_type"
        private const val STYLE_NUMBER = "style_number"
        private const val MESSAGE_STYLE = 0
        private const val EXTEND_STYLE = 1
    }

    private var styleType:Int = 0
    private var styleTag: String = ""
    private var selectedPosition: Int = -1
    private var tagList:MutableList<String> = mutableListOf()
    private var adapter:StyleAdapter? = null

    override fun getViewBinding(inflater: LayoutInflater): DemoStyleSettingLayoutBinding {
        return DemoStyleSettingLayoutBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.hasExtra(STYLE_TYPE).apply {
            styleType = intent.getIntExtra(STYLE_TYPE,0)
        }
        defaultStyle()
        initView()
        initListener()
    }

    fun initView(){
        binding.run {
            adapter = StyleAdapter(tagList)
            val layoutManager = LinearLayoutManager(mContext)
            rlSheetList.layoutManager = layoutManager
            rlSheetList.adapter = adapter
            when(styleType){
                MESSAGE_STYLE -> {
                    titleBar.setTitle(getString(R.string.currency_feature_message_style))
                    PreferenceManager.getValue(DemoConstant.MSG_STYLE, true).let {
                        selectedPosition = if (it) { 0 } else { 1 }
                        adapter?.setSelectPosition(selectedPosition)
                    }
                }
                EXTEND_STYLE -> {
                    titleBar.setTitle(getString(R.string.currency_feature_extend_style))
                    PreferenceManager.getValue(DemoConstant.EXTEND_STYLE, true).let {
                        selectedPosition = if (it) { 0 } else { 1 }
                        adapter?.setSelectPosition(selectedPosition)
                    }
                }
                else -> {}
            }
        }

    }

    fun initListener(){
        adapter?.setStyleListItemClickListener(object : StyleListItemSelectListener {
            override fun onSelectListener(position: Int,tag: String) {
                styleTag = tag
                selectedPosition = position
                updateConfirm()
            }
        })
        binding.titleBar.setOnMenuItemClickListener { item->
            when (item.itemId){
                R.id.action_language_confirm -> {
                    when(styleType){
                        MESSAGE_STYLE -> {
                            if (selectedPosition == 1){
                                PreferenceManager.putValue(DemoConstant.MSG_STYLE,false)
                                ChatUIKitClient.getConfig()?.chatConfig?.enableWxMessageStyle = false
                            }else{
                                PreferenceManager.putValue(DemoConstant.MSG_STYLE,true)
                                ChatUIKitClient.getConfig()?.chatConfig?.enableWxMessageStyle = true
                            }
                        }
                        EXTEND_STYLE -> {
                            if (selectedPosition == 1){
                                PreferenceManager.putValue(DemoConstant.EXTEND_STYLE,false)
                                ChatUIKitClient.getConfig()?.chatConfig?.enableWxExtendStyle = false
                            }else{
                                PreferenceManager.putValue(DemoConstant.EXTEND_STYLE,true)
                                ChatUIKitClient.getConfig()?.chatConfig?.enableWxExtendStyle = true
                            }
                        }
                        else -> {}
                    }
                    chengStyle()
                }
                else -> {}
            }
            true
        }
        binding.titleBar.setNavigationOnClickListener{
            mContext.onBackPressed()
        }
    }

    private fun defaultStyle(){
        tagList = mutableListOf(
            getString(R.string.currency_feature_style_num1),
            getString(R.string.currency_feature_style_num2)
        )
    }

    fun updateConfirm(){
        binding.let {
            it.titleBar.getToolBar().let { tb ->
                tb.menu.findItem(R.id.action_language_confirm)?.let { menuItem->
                    menuItem.isVisible = true
                    menuItem.title?.let { tl ->
                        val spannable = SpannableString(menuItem.title)
                        spannable.setSpan(
                            ForegroundColorSpan(
                                ContextCompat.getColor(mContext,
                                if (selectedPosition != -1){
                                    com.hyphenate.easeui.R.color.ease_color_primary
                                }else{
                                    com.hyphenate.easeui.R.color.ease_color_on_background_high
                                })
                            )
                            , 0, tl.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        menuItem.title = spannable
                    }
                }
            }
        }
    }

    private fun chengStyle(){
        val resultIntent = Intent()
        resultIntent.putExtra(STYLE_TYPE,styleType)
        resultIntent.putExtra(STYLE_NUMBER,styleTag)
        setResult(RESULT_OK,resultIntent)
        finish()
    }

    class StyleAdapter(
        private val tagList: MutableList<String>?,
    ) : RecyclerView.Adapter<StyleAdapter.ViewHolder>(){
        private lateinit var listener: StyleListItemSelectListener
        private var selectPosition:Int = -1

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.style_tag)
            val tagCb: CheckBox = itemView.findViewById(R.id.style_cb)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.ease_layout_style_item, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return tagList?.size ?: 0
        }

        override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
            tagList?.let {
                holder.textView.text = it[position]
                holder.tagCb.isChecked = selectPosition == position

                holder.tagCb.isClickable = false

                holder.itemView.setOnClickListener {
                    if (position == selectPosition) {
                        holder.tagCb.isChecked = false
                        selectPosition = -1
                    }else{
                        holder.tagCb.isChecked = true
                        selectPosition = position
                    }
                    notifyDataSetChanged()

                    if (holder.tagCb.isChecked){
                        listener.onSelectListener(position,tagList[selectPosition])
                    }
                }
            }
        }

        fun setSelectPosition(selectPosition:Int){
            this.selectPosition = selectPosition
            notifyDataSetChanged()
        }

        fun setStyleListItemClickListener(listener: StyleListItemSelectListener){
            this.listener = listener
        }

    }

    interface StyleListItemSelectListener{
        fun onSelectListener(position:Int,tag:String)
    }

}