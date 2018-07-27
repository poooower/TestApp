package com.github.poooower.common

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.Thread.sleep

class UnitTest {
    suspend fun doSth() {
        sleep(3000)
        println("doSth~~~${Thread.currentThread()}")
    }

    @Test
    fun test() {

//        runBlocking {
//            launch(CommonPool) {
//                for (i in 1..10) {
//                    doSth()
//                }
//
//                println("over~~~")
//            }
//        }
    }
}