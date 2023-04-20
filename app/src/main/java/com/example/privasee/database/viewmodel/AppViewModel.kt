package com.example.privasee.database.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.privasee.database.PrivaSeeDatabase
import com.example.privasee.database.model.App
import com.example.privasee.database.model.User
import com.example.privasee.database.viewmodel.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    init {
        val appDao = PrivaSeeDatabase.getDatabase(application).appDao()
        repository = AppRepository(appDao)
    }

    val getAllDataLive: LiveData<List<App>> = repository.getAllDataLive

    fun getAllData(): List<App> {
        return repository.getAllData()
    }

    fun getAllAppName(): List<String> {
        return repository.getAllAppName()
    }

    fun getAppData(appName: String): App {
        return repository.getAppData(appName)
    }

    fun getPackageName(appName: String): String {
        return repository.getPackageName(appName)
    }

    fun addApp(app: App) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addApp(app)
        }
    }

    fun deleteApp(app: App) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteApp(app)
        }
    }

}