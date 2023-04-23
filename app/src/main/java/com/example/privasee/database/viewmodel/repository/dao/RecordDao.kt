package com.example.privasee.database.viewmodel.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.privasee.database.model.Record
import com.example.privasee.database.model.User

@Dao
interface RecordDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addRecord(record: Record)

    @Query("SELECT * FROM record WHERE " +
            "day = :day AND " +
            "month = :month AND " +
            "year = :year")
    fun getRecord(day: Int, month: Int, year: Int): LiveData<List<Record>>

    @Delete
    suspend fun deleteRecord(record: Record)

}