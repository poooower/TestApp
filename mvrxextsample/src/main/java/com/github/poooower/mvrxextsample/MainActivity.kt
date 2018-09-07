package com.github.poooower.mvrxext.sample

import android.os.Bundle
import com.airbnb.mvrx.BaseMvRxActivity
import com.github.poooower.mvrxextsample.R

class MainActivity : BaseMvRxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
