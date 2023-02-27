package com.cavdardevelopment.exchangr.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cavdardevelopment.exchangr.model.database.entity.SupportedSymbolsEntity
import com.cavdardevelopment.exchangr.model.database.entity.WatchListEntity

@Database(entities = [SupportedSymbolsEntity::class, WatchListEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun databaseDAO(): DatabaseDAO

    companion object {
//        private var instance: SupportedSymbolsDatabase? = null
//
//        fun getSupportedSymbolsDatabase(context: Context): SupportedSymbolsDatabase? {
//            if (instance == null) {
//                instance = Room.databaseBuilder(context.applicationContext, SupportedSymbolsDatabase::class.java, "supportedSymbols.db").build()
//            }
//            return instance
//        }

        val databaseInstance = fun(context: Context): AppDatabase {
            return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "app.db").build()
        }
    }
}