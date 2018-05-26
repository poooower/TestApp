package com.github.poooower.jetpack

import android.os.AsyncTask

open class Singleton<out T, in A>(private val creator: (A) -> T) {
    @Volatile
    private var instance: T? = null

    fun get(arg: A): T = instance ?: synchronized(this) {
        instance ?: creator(arg).also { instance = it }
    }
}

enum class State { OK, FAIL, LOADING }

data class StateData<D>(val data: D? = null, val state: State = State.OK, val msg: String = "") {
    fun isOK() = state == State.OK
    fun isFail() = state == State.FAIL
    fun isLoading() = state == State.LOADING
}


fun background(background: () -> Unit) {
    AsyncTask.THREAD_POOL_EXECUTOR.execute(background)
}

fun io(io: () -> Unit) {
    AsyncTask.SERIAL_EXECUTOR.execute(io)
}

