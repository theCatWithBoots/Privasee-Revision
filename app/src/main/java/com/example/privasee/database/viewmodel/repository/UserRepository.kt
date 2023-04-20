package com.example.privasee.database.viewmodel.repository

import androidx.lifecycle.LiveData
import com.example.privasee.database.viewmodel.repository.dao.UserDao
import com.example.privasee.database.model.User

class UserRepository(private val userDao: UserDao) {

    val getAllDataLive: LiveData<List<User>> = userDao.getAllDataLive()
    val getAllNonOwner: LiveData<List<User>> = userDao.getAllNonOwner()

    fun getOwnerId(): Int {
        return userDao.getOwnerId()
    }

    fun getAllData(): List<User> {
        return userDao.getAllData()
    }

    suspend fun addUser(user: User) {
        userDao.addUser(user)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }

}