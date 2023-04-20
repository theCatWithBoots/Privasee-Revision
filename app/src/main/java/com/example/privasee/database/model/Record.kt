package com.example.privasee.database.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity (tableName = "record")
@Parcelize
data class Record(
    @PrimaryKey (autoGenerate = true) val id : Int = 0,
    val day : Int,
    val month: Int,
    val year: Int,
    val time : Long,
    val image: String,
    val packageName : String
): Parcelable
