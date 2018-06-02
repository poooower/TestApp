package com.github.poooower.common

import android.app.Application
import android.arch.lifecycle.ViewModel

lateinit var app: Application

open class BaseViewModel : ViewModel() {
    @Volatile
    var cleared: Boolean = false

    override fun onCleared() {
        super.onCleared()
        cleared = true
    }

    fun ifActive(func: () -> Unit) {
        if (!cleared) {
            func()
        }
    }
}

