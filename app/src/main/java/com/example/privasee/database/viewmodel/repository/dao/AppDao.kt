package com.example.privasee.database.viewmodel.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.privasee.database.model.App

@Dao
interface AppDao {

    @Query("SELECT * FROM app")
    fun getAllDataLive(): LiveData<List<App>>

    @Query("SELECT * FROM app")
    fun getAllData(): List<App>

    @Query("SELECT * FROM app " +
            "WHERE appName = :appName")
    fun getAppData(appName: String): App

    @Query("SELECT appName FROM app")
    fun getAllAppName(): List<String>

    @Query("SELECT packageName FROM app WHERE appName = :appName")
    fun getPackageName(appName: String): String

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addApp(app: App)

    @Delete
    suspend fun deleteApp(app: App)

}