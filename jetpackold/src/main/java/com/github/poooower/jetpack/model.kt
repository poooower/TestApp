package com.github.poooower.jetpack

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

data class User(val firstName: String, val lastName: String)

class UserViewModel : ViewModel() {
    private val mUser: MutableLiveData<User> by lazy {
        var u = MutableLiveData<User>()
        loadUser()
        return@lazy u
    }

    fun getUser(): LiveData<User> {
        return mUser
    }

    private fun loadUser() {
        sExecutor.execute {
            mUser.postValue(User("aaa", "bbbb"))
        }
    }
}