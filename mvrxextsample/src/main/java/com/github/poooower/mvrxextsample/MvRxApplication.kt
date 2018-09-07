package com.github.poooower.mvrxext.sample

import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import com.airbnb.epoxy.EpoxyController
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.koin.android.ext.android.startKoin
import org.koin.dsl.module.Module
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory


class MvRxApplication : Application() {

    private val dadJokeServiceModule: Module = module {
        single {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            Retrofit.Builder()
                    .baseUrl("https://icanhazdadjoke.com/")
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
        }
        single {
            get<Retrofit>().create(DadJokeService::class.java)
        }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(dadJokeServiceModule))
    }
}