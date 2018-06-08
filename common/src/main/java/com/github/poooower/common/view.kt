package com.github.poooower.common

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.AbsSavedState
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

class FragmentTabs : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var bottomNavigationViewId: Int = 0
        set(value) {
            field = value
            val bottomNavigationView = if (value == 0) null else parent?.let { (it as View).findViewById(value) as BottomNavigationView }
            bottomNavigationView?.let {
                val menu = it.menu
                it.setOnNavigationItemSelectedListener {
                    for (i in 0 until (Math.min(menu.size(), fragments?.size ?: 0))) {
                        if (it === menu.getItem(i)) {
                            switchTo(i)
                            break
                        }
                    }
                    true
                }
                if (firstInit) {
                    it.selectedItemId = menu.getItem(0).itemId
                }
            }
        }
    var fragmentManager: FragmentManager? = null
    var fragments: Array<Class<out Fragment>>? = null
        set(value) {
            field = value
            if (firstInit) {
                fragmentStates = value?.let { arrayOfNulls<Fragment.SavedState?>(it.size) } ?: null
            }
        }
    private var fragmentStates: Array<Fragment.SavedState?>? = null
    private var currIndex = -1
    private val firstInit
        get() = currIndex == -1

    fun switchTo(pos: Int) {
        if (currIndex == pos) {
            return
        }
        val lastIndex = currIndex
        currIndex = pos

        val newFragment = fragments?.getOrNull(pos)?.newInstance() ?: return
        val transaction = fragmentManager?.beginTransaction() ?: return
        val lastFragment = fragmentManager?.findFragmentById(id)
        lastFragment?.let {
            fragmentStates?.set(lastIndex, fragmentManager?.saveFragmentInstanceState(it) ?: null)
            transaction.remove(it)
        }
        newFragment.setInitialSavedState(fragmentStates?.getOrNull(currIndex))
        transaction.add(id, newFragment)
        transaction.commitNowAllowingStateLoss()
    }

    override fun onSaveInstanceState(): Parcelable {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.currIndex = currIndex
        savedState.fragmentStates = fragmentStates
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        currIndex = state.currIndex
        state.fragmentStates?.let {
            if (it.isNotEmpty()) {
                fragmentStates = it
            }
        }

    }

    class SavedState : AbsSavedState {
        var currIndex: Int = -1
        var fragmentStates: Array<Fragment.SavedState?>? = null

        constructor(superState: Parcelable) : super(superState)

        constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
            currIndex = source.readInt()
            val size = source.readInt()
            if (size > 0) {
                source.readParcelableArray(loader)?.let { parcelables ->
                    fragmentStates = Array(parcelables.size) {
                        parcelables[it] as Fragment.SavedState?
                    }
                }
            }
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeInt(currIndex)
            val size = fragmentStates?.size ?: 0
            parcel.writeInt(size)
            if (size > 0) {
                parcel.writeParcelableArray(fragmentStates, 0)
            }
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.ClassLoaderCreator<SavedState> {
            override fun createFromParcel(source: Parcel, loader: ClassLoader): SavedState {
                return SavedState(source, loader)
            }

            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel, null)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }


    }
}