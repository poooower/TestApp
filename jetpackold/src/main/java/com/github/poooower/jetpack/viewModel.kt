package com.github.poooower.jetpack

import android.arch.paging.DataSource
import com.github.poooower.common.FetchWithPagedListViewModel
import com.github.poooower.common.io


class UserViewModel : FetchWithPagedListViewModel<User>() {
    override fun createDataSource(): DataSource.Factory<*, User> = userDao.findPageUsers()

    override fun fetch(lastItem: User?): List<User> {
        Thread.sleep(3000)
        val list = mutableListOf<User>()
        for (i in 1..10) {
            list.add(User(firstName = "first", lastName = "last"))
        }
        lastItem?.let { throw RuntimeException("fetch error") }
        return list
    }

    override fun afterFetch(loadingMore: Boolean, list: List<User>) {
        if (!loadingMore) {
            userDao.deleteAll()
        }
        userDao.insert(list)
    }


    fun addUser() = io {
        userDao.insert(User(firstName = "first", lastName = "last"))
    }

    fun deleteUser(pos: Int) = list.value?.get(pos)?.let {
        io {
            userDao.deleteUser(it.id!!)
        }
    }
}