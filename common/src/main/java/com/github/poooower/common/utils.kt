package com.github.poooower.common

import android.view.View
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import org.jetbrains.anko.coroutines.experimental.bg as sbg

open class Singleton<out T, in A>(private val creator: (A) -> T) {
    @Volatile
    private var instance: T? = null

    fun get(arg: A): T = instance ?: synchronized(this) {
        instance ?: creator(arg).also { instance = it }
    }
}

fun oneShotPreDraw(view: View, task: () -> Unit) {
    android.support.v4.app.oneShotPreDraw(view, task)
}

inline fun <T> ui(crossinline block: suspend () -> T): Deferred<T> = async(UI) {
    block()
}


suspend inline fun <T> bg(crossinline block: () -> T): T = sbg {
    block()
}.await()
