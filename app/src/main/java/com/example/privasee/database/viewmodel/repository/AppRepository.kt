package com.example.privasee.database.viewmodel.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Query
import com.example.privasee.database.model.App
import com.example.privasee.database.model.User
import com.example.privasee.database.viewmodel.repository.dao.AppDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppRepository(private val appDao: AppDao) {

    val getAllDataLive: LiveData<List<App>> = appDao.getAllDataLive()

    fun getAllData(): List<App> {
        return appDao.getAllData()
    }

    fun getAllAppName(): List<String> {
        return appDao.getAllAppName()
    }


    fun getAppData(appName: String): App {
        return appDao.getAppData(appName)
    }

    fun getPackageName(appName: String): String {
        return appDao.getPackageName(appName)
    }

    suspend fun addApp(app: App) {
        appDao.addApp(app)
    }

    suspend fun deleteApp(app: App) {
        appDao.deleteApp(app)
    }

}