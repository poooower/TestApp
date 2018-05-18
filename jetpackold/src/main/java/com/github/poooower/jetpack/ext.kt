package com.github.poooower.jetpack

import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import java.util.*

val sHandlers = WeakHashMap<Any, Handler>()

val AppCompatActivity.mHandler: Handler
    get() {
        synchronized(sHandlers) {
            return sHandlers.getOrPut(this) {
                Handler(Looper.getMainLooper())
            }
        }
    }