package com.github.poooower.jetpack

import android.arch.paging.DataSource
import com.github.poooower.common.FetchWithPagedListViewModel
import org.jetbrains.anko.coroutines.experimental.bg


class UserViewModel : FetchWithPagedListViewModel<User>() {
    override fun createDataSource(): DataSource.Factory<*, User> = userDao.findPageUsers()

    override fun fetch(lastItem: User?): List<User> {
        Thread.sleep(3000)
        val list = mutableListOf<User>()
        for (i in 1..10) {
            list.add(User(firstName = "first", lastName = "last"))
        }
//        lastItem?.let { throw RuntimeException("fetch error") }
        return list
    }

    override fun afterFetch(loadingMore: Boolean, list: List<User>) {
        if (loadingMore) {
            userDao.insert(list)
        } else {
            userDao.refresh(list)
        }
    }


    fun addUser() = bg {
        userDao.insert(User(firstName = "first", lastName = "last"))
    }

    fun deleteUser(pos: Int) = list.value?.get(pos)?.let {
        bg {
            userDao.deleteUser(it.id!!)
        }
    }
}