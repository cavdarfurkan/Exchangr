package com.cavdardevelopment.exchangr.model.database

import androidx.room.*
import com.cavdardevelopment.exchangr.model.database.entity.SupportedSymbolsEntity
import com.cavdardevelopment.exchangr.model.database.entity.WatchListEntity

@Dao
interface DatabaseDAO {

    // SupportedSymbols DAO
    @Insert
    fun insertSupportedSymbol(supportedSymbolsEntity: SupportedSymbolsEntity)

    @Delete
    fun deleteSupportedSymbol(supportedSymbolsEntity: SupportedSymbolsEntity)

    @Query("SELECT * FROM supportedSymbols")
    fun getAllSupportedSymbols(): List<SupportedSymbolsEntity>

    @Query("SELECT symbolCode FROM supportedSymbols")
    fun getAllSupportedCodes(): List<String>

    // WatchList DAO
    @Insert
    fun insertWatchList(watchListEntity: WatchListEntity)

    @Delete
    fun deleteWatchList(watchListEntity: WatchListEntity)

    @Update
    fun updateWatchList(watchListEntities: WatchListEntity)

    @Update
    fun updateAllWatchList(watchList: List<WatchListEntity>)

    @Query("SELECT * FROM watchList WHERE watchList.watchId = :watchId")
    fun getByWatchId(watchId: String): WatchListEntity

    @Query("SELECT * FROM watchList WHERE watchList.base = :base AND watchList.quote = :quote")
    fun getEntityByBaseAndQuote(base: String, quote: String): List<WatchListEntity>

    @Query("SELECT * FROM watchList ORDER BY rowIndex ASC")
    fun getAllWatchListOrderByRowIndex(): List<WatchListEntity>

    @Query("SELECT * FROM watchList")
    fun getAllWatchList(): List<WatchListEntity>
}