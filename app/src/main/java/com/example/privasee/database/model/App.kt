package com.example.privasee.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity (tableName = "app")
data class App(
    @PrimaryKey (autoGenerate = true) val id : Int = 0,
    val packageName : String,
    val appName : String
)
