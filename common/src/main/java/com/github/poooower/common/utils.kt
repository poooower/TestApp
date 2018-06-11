package com.github.poooower.common

import android.annotation.SuppressLint
import android.arch.core.executor.ArchTaskExecutor
import android.os.AsyncTask
import android.view.View

open class Singleton<out T, in A>(private val creator: (A) -> T) {
    @Volatile
    private var instance: T? = null

    fun get(arg: A): T = instance ?: synchronized(this) {
        instance ?: creator(arg).also { instance = it }
    }
}

fun bg(bg: () -> Unit) {
    AsyncTask.THREAD_POOL_EXECUTOR.execute(bg)
}

@SuppressLint("RestrictedApi")
fun io(io: () -> Unit) {
    ArchTaskExecutor.getInstance().executeOnDiskIO(io)
}

@SuppressLint("RestrictedApi")
fun ui(ui: () -> Unit) {
    ArchTaskExecutor.getInstance().executeOnMainThread(ui)
}

fun oneShortPreDraw(view: View, task: () -> Unit) {
    android.support.v4.app.oneShortPreDraw(view, task)
}


