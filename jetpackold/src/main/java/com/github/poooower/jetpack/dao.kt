package com.github.poooower.jetpack

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.persistence.room.*
import android.content.Context

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    companion object : Singleton<AppDatabase, Context>({
        Room.databaseBuilder(it.applicationContext, AppDatabase::class.java, "database.db").build()
    })

    abstract fun userDao(): UserDao
}

@Dao
interface UserDao {
    @Insert
    fun insert(user: User)

    @Query("DELETE FROM user WHERE id = :id")
    fun deleteUser(id: Int)

    @Query("SELECT * FROM user")
    fun findUsers(): LiveData<List<User>>

    @Query("SELECT * FROM user WHERE id = :id")
    fun findUser(id: Int): LiveData<User>

    @Query("SELECT * FROM user")
    fun findPageUsers(): DataSource.Factory<Int, User>
}