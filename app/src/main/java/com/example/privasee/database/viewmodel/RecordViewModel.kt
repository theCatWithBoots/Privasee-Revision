package com.example.privasee.database.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.privasee.database.PrivaSeeDatabase
import com.example.privasee.database.model.Record
import com.example.privasee.database.viewmodel.repository.RecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecordViewModel(application: Application): AndroidViewModel(application) {

    private val repository: RecordRepository

    init {
        val recordRepository = PrivaSeeDatabase.getDatabase(application).recordDao()
        repository = RecordRepository(recordRepository)
    }

    fun addRecord(record: Record) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addRecord(record)
        }
    }

    fun getRecord(day: Int, month: Int, year: Int): LiveData<List<Record>> {
        return repository.getRecord(day, month, year)
    }

}