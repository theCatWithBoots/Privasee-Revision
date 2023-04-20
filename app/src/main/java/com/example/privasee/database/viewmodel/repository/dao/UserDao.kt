package com.example.privasee.database.viewmodel.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.privasee.database.model.User

@Dao
interface UserDao {

    @Query("SELECT * FROM user ORDER BY id ASC")
    fun getAllDataLive(): LiveData<List<User>>

    @Query("SELECT * FROM user " +
            "WHERE isOwner = false " +
            "ORDER BY id ASC")
    fun getAllNonOwner(): LiveData<List<User>>

    @Query("SELECT * FROM user ORDER BY id ASC")
    fun getAllData(): List<User>

    @Query("SELECT id FROM user WHERE isOwner = true")
    fun getOwnerId(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Update
    suspend fun updateUser(user: User)

}