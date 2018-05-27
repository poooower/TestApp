package com.github.poooower.jetpack

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList


val AndroidViewModel.appDatabase
    get() = AppDatabase.get(getApplication())
val AndroidViewModel.userDao
    get() = appDatabase.userDao()

class UserViewModel(app: Application) : AndroidViewModel(app) {

    val users: LiveData<PagedList<User>> by lazy {
        val dataSource: DataSource.Factory<Int, User> =
                userDao.findPageUsers()

        //        val myPagingConfig = PagedList.Config.Builder()
//                .setPageSize(50)
//                .setPrefetchDistance(150)
//                .setEnablePlaceholders(true)
//                .build()

        LivePagedListBuilder(dataSource, 5)
                .build()
    }

    fun addUser() = io {
        userDao.insert(User(firstName = "first", lastName = "last"))
    }

    fun deleteUser(pos: Int) {
        users.value?.get(pos)?.let {
            io {
                userDao.deleteUser(it.id!!)
            }
        }
    }
}