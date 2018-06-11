package com.github.poooower.common

import android.content.Context
import android.os.Parcelable
import android.support.v4.app.FragmentTabHost
import android.util.AttributeSet

class FragmentTabHostForDataBinding : FragmentTabHost {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    var restoring = false
    var restoreTag: String? = null

    override fun onRestoreInstanceState(state: Parcelable?) {
        restoring = true
        super.onRestoreInstanceState(state)
        restoring = false
    }

    override fun setCurrentTabByTag(tag: String?) {
        if (restoring && tabWidget == null) {
            restoreTag = tag
        }
        super.setCurrentTabByTag(tag)
    }

    override fun setCurrentTab(index: Int) {
        restoreTag?.let { return }
        super.setCurrentTab(index)
    }

    fun finishIfNeedRestore() = restoreTag?.let {
        restoreTag = null
        setCurrentTabByTag(it)
    }
}