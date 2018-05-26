package com.github.poooower.jetpack

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull

@Entity(tableName = "user")
data class  User(
        @PrimaryKey(autoGenerate = true)
        @NonNull
        @ColumnInfo(name = "id")
        var id: Int? = null,
        @ColumnInfo(name = "first_name")
        var firstName: String? = null,
        @ColumnInfo(name = "last_name")
        var lastName: String? = null
)