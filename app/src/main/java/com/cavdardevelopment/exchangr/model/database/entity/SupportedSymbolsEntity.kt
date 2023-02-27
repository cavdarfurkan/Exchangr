package com.cavdardevelopment.exchangr.model.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supportedSymbols")
data class SupportedSymbolsEntity(

    @PrimaryKey(autoGenerate = true) val symbolId: Int,
    @ColumnInfo(name = "symbolCode") var symbolCode: String,
    @ColumnInfo(name = "symbolDescription") var symbolDescription: String
    )