package com.cavdardevelopment.exchangr.model.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude
import com.google.gson.annotations.SerializedName
import java.util.UUID

@Entity(tableName = "watchList")
data class WatchListEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "watchId")
    @SerializedName("watchId")
    val watchId: String = UUID.randomUUID().toString(),

    @SerializedName("base")
    @ColumnInfo(name = "base")
    val base: String,

    @SerializedName("quote")
    @ColumnInfo(name = "quote")
    val quote: String,

    @SerializedName("rate")
    @ColumnInfo(name = "rate")
    val rate: Double,

    @SerializedName("date")
    @ColumnInfo(name = "date")
    val date: String,

    @SerializedName("changeRate")
    @ColumnInfo(name = "changeRate")
    val changeRate: Double,

    @SerializedName("rowIndex")
    @ColumnInfo(name = "rowIndex")
    val rowIndex: Int
    ) {

    @Exclude
    fun toMap(): Map<String, Any> = mapOf(
        "watchId" to watchId,
        "base" to base,
        "quote" to quote,
        "rate" to rate,
        "date" to date,
        "changeRate" to changeRate,
        "rowIndex" to rowIndex
    )
}