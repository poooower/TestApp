package com.github.poooower.jetpack

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList


val AndroidViewModel.appDatabase
    get() = AppDatabase.get(getApplication())
val AndroidViewModel.userDao
    get() = appDatabase.userDao()

class UserViewModel(app: Application) : AndroidViewModel(app) {

    val users: LiveData<StateData<List<User>>> by lazy {
        Transformations.map(userDao.findUsers()) {
            StateData(it, State.OK, "")
        }
    }

    fun addUser() = io {
        userDao.insert(User(firstName = "first", lastName = "last"))
    }

    fun deleteUser() {
        users.value?.data?.firstOrNull()?.id?.let {
            io {
                userDao.deleteUser(it)
            }
        }
    }

    fun findUsersByPage() {
        val dataSource: DataSource.Factory<Int, User> =
                userDao.findPageUsers()

//        val myPagingConfig = PagedList.Config.Builder()
//                .setPageSize(50)
//                .setPrefetchDistance(150)
//                .setEnablePlaceholders(true)
//                .build()

        val pagedList = LivePagedListBuilder(dataSource, 20)
                .build()
    }
}