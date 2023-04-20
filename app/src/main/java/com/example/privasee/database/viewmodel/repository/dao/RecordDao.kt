package com.example.privasee.database.viewmodel.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.privasee.database.model.Record

@Dao
interface RecordDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addRecord(record: Record)

    @Query("SELECT * FROM record WHERE " +
            "day = :day AND " +
            "month = :month AND " +
            "year = :year")
    fun getRecord(day: Int, month: Int, year: Int): LiveData<List<Record>>

}