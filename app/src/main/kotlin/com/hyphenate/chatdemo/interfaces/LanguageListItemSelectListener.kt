package com.hyphenate.chatdemo.interfaces

import com.hyphenate.chatdemo.bean.Language

interface LanguageListItemSelectListener {
    fun onSelectListener(position:Int,language:Language)
}