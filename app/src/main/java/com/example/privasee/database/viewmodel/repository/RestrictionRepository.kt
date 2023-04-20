package com.example.privasee.database.viewmodel.repository

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Query
import com.example.privasee.database.model.Restriction
import com.example.privasee.database.viewmodel.repository.dao.RestrictionDao

class RestrictionRepository(private val restrictionDao: RestrictionDao) {

    suspend fun addRestriction(restriction: Restriction) {
        restrictionDao.addRestriction(restriction)
    }

    fun getRestrictionUsingAppName(appName: String): Restriction {
        return restrictionDao.getRestrictionUsingAppName(appName)
    }

    fun getAppName(id: Int): String {
        return restrictionDao.getAppName(id)
    }

    suspend fun deleteRestriction(restriction: Restriction) {
        restrictionDao.deleteRestriction(restriction)
    }

    // Monitoring app access repository
    fun getAllMonitoredApps(userId: Int): LiveData<List<Restriction>> {
        return restrictionDao.getAllMonitoredApps(userId)
    }

    fun getAllUnmonitoredApps(userId: Int): LiveData<List<Restriction>> {
        return restrictionDao.getAllUnmonitoredApps(userId)
    }

    fun updateMonitoredApps(restrictionId: Int, isMonitored: Boolean) {
        return restrictionDao.updateMonitoredApps(restrictionId, isMonitored)
    }

    // Controlling app access repository
    fun getAllControlledApps(userId: Int): LiveData<List<Restriction>> {
        return restrictionDao.getAllControlledApps(userId)
    }

    fun getAllUncontrolledApps(userId: Int): LiveData<List<Restriction>> {
        return restrictionDao.getAllUncontrolledApps(userId)
    }

    fun updateControlledApps(restrictionId: Int, isControlled: Boolean) {
        return restrictionDao.updateControlledApps(restrictionId, isControlled)
    }

    fun getUserRestrictionCount(userId: Int): Int {
        return restrictionDao.getUserRestrictionCount(userId)
    }

    fun getAllMonitoredAppList(userId: Int): List<Restriction> {
        return restrictionDao.getAllMonitoredAppList(userId)
    }

}