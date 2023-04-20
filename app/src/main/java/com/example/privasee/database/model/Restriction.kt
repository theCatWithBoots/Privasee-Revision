package com.example.privasee.database.model

import androidx.room.*

@Entity( tableName = "restriction",
        foreignKeys = [
            ForeignKey(
                entity = User::class,
                parentColumns = ["id"],
                childColumns = ["userId"],
                onDelete = ForeignKey.CASCADE
            )],
        indices = [
            Index(value = ["userId"]),
        ]
)
data class Restriction(
    @PrimaryKey (autoGenerate = true) val id : Int = 0,
    val appName: String,
    val monitored: Boolean = false,
    val controlled: Boolean = false,
    val userId: Int
)