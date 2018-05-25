package com.github.poooower.jetpack

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MediatorLiveData


val AndroidViewModel.appDatabase
    get() = AppDatabase.get(getApplication())
val AndroidViewModel.userDao
    get() = appDatabase.userDao()

class UserViewModel(app: Application) : AndroidViewModel(app) {
    private val taskHolder: TaskHolder by lazy { TaskHolder() }

    val users: MediatorLiveData<StateData<List<User>>> by lazy {
        val u = MediatorLiveData<StateData<List<User>>>()
        u.value = StateData()
        return@lazy u
    }

    fun init() {
        loadUsers()
    }

    override fun onCleared() {
        super.onCleared()
        taskHolder.cancelAll()
    }

    private fun loadUsers() {
        users.update(state = State.LOADING)
        background {
            Thread.sleep(3000)
            return@background userDao.findUsers()
        }.ui {
            users.addSource(it, state = State.OK)
        }.start(taskHolder, "loadUsers")
    }

    fun addUser() {
        background {
            userDao.insert(User(firstName = "first", lastName = "last"))
        }.start()
    }

    fun deleteUser() {
        users.value?.data?.firstOrNull()?.id?.let {
            background {
                userDao.deleteUser(it)
            }.start()
        }
    }
}