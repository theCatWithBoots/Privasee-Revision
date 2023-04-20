package com.example.privasee.database.viewmodel.repository

import androidx.lifecycle.LiveData
import com.example.privasee.database.model.Record
import com.example.privasee.database.viewmodel.repository.dao.RecordDao

class RecordRepository(private val recordDao: RecordDao) {

    suspend fun addRecord(record: Record) {
        recordDao.addRecord(record)
    }

    fun getRecord(day: Int, month: Int, year: Int): LiveData<List<Record>> {
        return recordDao.getRecord(day, month, year)
    }
}