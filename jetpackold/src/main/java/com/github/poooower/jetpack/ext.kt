package com.github.poooower.jetpack

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData

fun <T> MediatorLiveData<T>.addSource(source: LiveData<T>) {
    addSource(source) {
        value = it
    }
}

fun <T> MediatorLiveData<StateData<T>>.addSource(source: LiveData<T>, state: State? = null, msg: String? = null) {
    addSource(source) {
        update(it, state, msg)
    }
}

fun <T> MediatorLiveData<StateData<T>>.update(data: T? = null, state: State? = null, msg: String? = null) {
    if (data == null && state == null && msg == null) {
        return
    }
    value = StateData(data ?: value?.data, (state ?: value?.state) ?: State.OK, (msg ?: value?.msg)
            ?: "")
}