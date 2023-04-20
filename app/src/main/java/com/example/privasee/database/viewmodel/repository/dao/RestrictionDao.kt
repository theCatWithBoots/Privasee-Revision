package com.example.privasee.database.viewmodel.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.privasee.database.model.Restriction

@Dao
interface RestrictionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addRestriction(restriction: Restriction)

    @Query("SELECT * FROM restriction " +
            "WHERE appName = :appName")
    fun getRestrictionUsingAppName(appName: String): Restriction

    @Query("SELECT appName FROM restriction " +
            "WHERE id = :id")
    fun getAppName(id: Int): String

    @Delete
    suspend fun deleteRestriction(restriction: Restriction)

    // App Monitoring Access queries
    @Query("SELECT * FROM restriction " +
            "WHERE monitored = true " +
            "AND userId = :userId " +
            "ORDER BY appName ASC")
    fun getAllMonitoredApps(userId: Int): LiveData<List<Restriction>>

    @Query("SELECT * FROM restriction " +
            "WHERE monitored = false " +
            "AND userId = :userId " +
            "ORDER BY appName ASC")
    fun getAllUnmonitoredApps(userId: Int): LiveData<List<Restriction>>

    @Query("UPDATE restriction SET monitored = :isMonitored " +
            "WHERE id = :restrictionId")
    fun updateMonitoredApps(restrictionId: Int, isMonitored: Boolean)

    // App Controlling Access queries
    @Query("SELECT * FROM restriction " +
            "WHERE controlled = true " +
            "AND userId = :userId " +
            "ORDER BY appName ASC")
    fun getAllControlledApps(userId: Int): LiveData<List<Restriction>>

    @Query("SELECT * FROM restriction " +
            "WHERE controlled = false " +
            "AND userId = :userId " +
            "ORDER BY appName ASC")
    fun getAllUncontrolledApps(userId: Int): LiveData<List<Restriction>>

    @Query("UPDATE restriction SET controlled = :isControlled " +
            "WHERE id = :restrictionId")
    fun updateControlledApps(restrictionId: Int, isControlled: Boolean)

    @Query("SELECT COUNT(*) FROM restriction " +
            "WHERE userId = :userId")
    fun getUserRestrictionCount(userId: Int): Int

    // Non live data for monitored apps
    @Query("SELECT * FROM restriction " +
            "WHERE monitored = true " +
            "AND userId = :userId")
    fun getAllMonitoredAppList(userId: Int): List<Restriction>

}