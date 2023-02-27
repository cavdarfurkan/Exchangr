package com.cavdardevelopment.exchangr.viewmodel


import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Transaction
import com.cavdardevelopment.exchangr.BuildConfig
import com.cavdardevelopment.exchangr.model.database.AppDatabase
import com.cavdardevelopment.exchangr.model.database.entity.SupportedSymbolsEntity
import com.cavdardevelopment.exchangr.model.database.entity.WatchListEntity
import com.cavdardevelopment.exchangr.model.response.FluctuationResponse
import com.cavdardevelopment.exchangr.model.response.LatestRateResponse
import com.cavdardevelopment.exchangr.model.response.SupportedSymbolsResponse
import com.cavdardevelopment.exchangr.util.callbacks.ResultCallback
import com.cavdardevelopment.exchangr.util.exception.CurrencyTypeException
import com.cavdardevelopment.exchangr.util.extensions.dayBefore
import com.cavdardevelopment.exchangr.util.extensions.format
import com.cavdardevelopment.exchangr.util.extensions.toCalendar
import com.cavdardevelopment.exchangr.util.network.RetrofitInstance
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.UnknownHostException
import java.util.concurrent.CountDownLatch


class MainActivityViewModel(private val appDatabase: AppDatabase) : ViewModel() {
    private val firebaseDB = Firebase.database(BuildConfig.FIREBASE_DATABASE_URL)
    private val userUid = Firebase.auth.currentUser?.uid

    var supportedSymbolsLiveData = MutableLiveData<SupportedSymbolsResponse>()

    private var watchList = ArrayList<WatchListEntity>()
    val watchListLiveData: MutableLiveData<ArrayList<WatchListEntity>> by lazy {
        MutableLiveData<ArrayList<WatchListEntity>>()
    }

    // API Functions
    fun getRateAndFluctuation(base: String, quote: String, sqlMethod: SQLMethod, callback: ResultCallback?) {
        if (supportedSymbolsLiveData.value?.codes?.contains(base) == false || supportedSymbolsLiveData.value?.codes?.contains(quote) == false) {
            Log.e("exception", "getRateAndFluctuation: if", CurrencyTypeException("Wrong currency type(s)"))
            failed(CurrencyTypeException("Wrong currency type(s)"), callback)
            return
        }

        RetrofitInstance.apiLatestRates.getLatestRates(base, quote).enqueue(object : Callback<LatestRateResponse> {
            override fun onResponse(call: Call<LatestRateResponse>, rateResponse: Response<LatestRateResponse>) {

                val today = rateResponse.body()?.date
                val yesterday = today?.toCalendar()?.dayBefore()?.format()

                if (today != null && yesterday != null) {
                    RetrofitInstance.apiFluctuation.getFluctuation(yesterday, today, base, quote).enqueue(object : Callback<FluctuationResponse> {
                        override fun onResponse(call: Call<FluctuationResponse>, fluctuationResponse: Response<FluctuationResponse>) {

                            val model = rateResponse.body()?.let {latestRateResponse ->
                                fluctuationResponse.body()?.let { fluctuationResponse -> WatchListEntity(
                                    base = latestRateResponse.base,
                                    quote = latestRateResponse.currencyNames[0],
                                    rate = latestRateResponse.currencyRates[0],
                                    date = latestRateResponse.date,
                                    changeRate = fluctuationResponse.changeRate,
                                    rowIndex = watchList.size
                                )}
                            }

                            model?.let {
                                when (sqlMethod) {
                                    SQLMethod.ADD -> addWatchListItem(model)
                                    SQLMethod.UPDATE -> updateWatchList(model)
                                    SQLMethod.DELETE -> {}
                                }
                            }

                            callback?.onResult(true)
                        }

                        override fun onFailure(call: Call<FluctuationResponse>, t: Throwable) {
                            Log.e("exception", "onFailure: apiFluctuation: $t", t)
                            failed(t, callback)
                        }
                    })
                }
            }

            override fun onFailure(call: Call<LatestRateResponse>, t: Throwable) {
                Log.e("exception", "onFailure: apiLatestRates: $t", t)
                failed(t, callback)
            }
        })
    }

    fun getSupportedSymbols() {
        RetrofitInstance.apiSupportedSymbols.getSupportedSymbols().enqueue(
            object : Callback<SupportedSymbolsResponse> {
                override fun onResponse(
                    call: Call<SupportedSymbolsResponse>,
                    response: Response<SupportedSymbolsResponse>
                ) {
                    response.body()?.let {
                        supportedSymbolsLiveData.value = it
                        saveSupportedSymbols(it)
                    }
                }

                override fun onFailure(call: Call<SupportedSymbolsResponse>, t: Throwable) {
                    Log.e("exception", "onFailure: apiSupportedSymbols: $t", t)
                    failed(t, null)
                    loadSupportedSymbols()
                }
            }
        )
    }

    fun failed(t: Throwable, callback: ResultCallback?) {
        val resultMessage = when (t) {
            is UnknownHostException -> "No internet connection"
            is CurrencyTypeException -> "The currency type is not supported"
            else -> "An error occurred"
        }

        callback?.onResult(false, resultMessage)
    }

    // Livedata functions
    fun addWatchListItem(entity: WatchListEntity) = viewModelScope.launch(Dispatchers.IO) {
        watchList.add(entity)
        watchListLiveData.postValue(watchList)
        insertWatchList(entity)
    }

    fun removeWatchListItem(item: WatchListEntity) = viewModelScope.launch(Dispatchers.IO) {
        watchList.remove(item)
        watchListLiveData.postValue(watchList)
        deleteWatchList(item)
        updateWatchListOrder(watchList)
    }

    fun filterWatchList(newText: String?) {
        val tempWatchList = watchList
        val filteredWatchList = tempWatchList.filter {
            it.base.contains(newText.toString(), ignoreCase = true)
                .or(it.quote.contains(newText.toString(), ignoreCase = true))
        }

        watchListLiveData.value = filteredWatchList as ArrayList<WatchListEntity>
    }

    // Database functions
    private fun insertWatchList(watchListEntity: WatchListEntity) = viewModelScope.launch(Dispatchers.IO) {
        appDatabase.databaseDAO().insertWatchList(watchListEntity)
        firebaseWrite(watchListEntity)
    }

    fun loadWatchList(isLoggedOut: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        watchList.clear()

        if (isLoggedOut) {
            Log.i("info", "loadWatchList: if")
            runBlocking {
                firebaseRead()?.let { entities ->
                    entities.forEach { entity ->
                        watchList.add(entity)
                        appDatabase.databaseDAO().insertWatchList(entity)
                    }
                }
            }
        }
        else {
            Log.i("info", "loadWatchList: else")
            appDatabase.databaseDAO().getAllWatchListOrderByRowIndex().forEach {
                watchList.add(WatchListEntity(it.watchId, it.base, it.quote, it.rate, it.date, it.changeRate, it.rowIndex))
            }
        }

        watchListLiveData.postValue(watchList)
    }

    private fun deleteWatchList(watchListEntity: WatchListEntity) = viewModelScope.launch(Dispatchers.IO) {
        appDatabase.databaseDAO().deleteWatchList(watchListEntity)
        firebaseDelete(watchListEntity)
    }

    fun updateWatchList(entity: WatchListEntity) = viewModelScope.launch(Dispatchers.IO) {
        // For some reason, there may be multiple same rows with the same base and quote
        // To update the rate of each one of them, a list is returned with IDs
        val entities = appDatabase.databaseDAO().getEntityByBaseAndQuote(entity.base, entity.quote)
        entities.forEach {
            val updatedEntity = WatchListEntity(it.watchId, entity.base, entity.quote, entity.rate, entity.date, entity.changeRate, it.rowIndex)
            appDatabase.databaseDAO().updateWatchList(updatedEntity)
            firebaseUpdateItem(updatedEntity)
        }
    }

    @Transaction
    fun updateWatchListOrder(reorderedWatchList: ArrayList<WatchListEntity>) = viewModelScope.launch(Dispatchers.IO) {
        val dbWatchList = appDatabase.databaseDAO().getAllWatchList()
        val updatedWatchList = mutableListOf<WatchListEntity>()

        for (i in reorderedWatchList.indices) {
            for (j in dbWatchList.indices) {
                if (reorderedWatchList[i].watchId == dbWatchList[j].watchId) {
                    val updatedEntity = dbWatchList[j].copy(rowIndex = i)
                    updatedWatchList.add(updatedEntity)
                    firebaseUpdateItem(updatedEntity)
                    break
                }
            }
        }

        appDatabase.databaseDAO().updateAllWatchList(updatedWatchList)
    }

    private fun saveSupportedSymbols(supportedSymbolsResponse: SupportedSymbolsResponse) = viewModelScope.launch(Dispatchers.IO) {
        val allCodes = appDatabase.databaseDAO().getAllSupportedCodes()

        for (i in 0 until supportedSymbolsResponse.codes.size) {
            if (!allCodes.contains(supportedSymbolsResponse.codes[i])) {
                val entity = SupportedSymbolsEntity(0, supportedSymbolsResponse.codes[i], supportedSymbolsResponse.descriptions[i])
                appDatabase.databaseDAO().insertSupportedSymbol(entity)
            }
        }
    }

    private fun loadSupportedSymbols() {
        val codes = ArrayList<String>()
        val descriptions = ArrayList<String>()

        viewModelScope.launch(Dispatchers.IO) {
            appDatabase.databaseDAO().getAllSupportedSymbols().forEach {
                codes.add(it.symbolCode)
                descriptions.add(it.symbolDescription)
            }
            supportedSymbolsLiveData.postValue(SupportedSymbolsResponse(true, descriptions, codes))
        }
    }

    fun clearDatabase() = viewModelScope.launch(Dispatchers.IO) {
        appDatabase.clearAllTables()
    }

    // Firebase Realtime Database functions
    private suspend fun firebaseRead(): List<WatchListEntity>? {
        if (userUid == null) return null

        val entitiesList = ArrayList<WatchListEntity>()
        val dataSnapshot = firebaseDB.reference.child("users").child(userUid).get().await()

        dataSnapshot.children.forEach { data ->
            val base = data.child("base").getValue(String::class.java)
            val changeRate = data.child("changeRate").getValue(Double::class.java)
            val date = data.child("date").getValue(String::class.java)
            val quote = data.child("quote").getValue(String::class.java)
            val rate = data.child("rate").getValue(Double::class.java)
            val rowIndex = data.child("rowIndex").getValue(Int::class.java)
            val watchId = data.child("watchId").getValue(String::class.java)

            if (base != null && changeRate != null && date != null && quote != null && rate != null && rowIndex != null && watchId != null) {
                entitiesList.add(WatchListEntity(watchId, base, quote, rate, date, changeRate, rowIndex))
            }
        }

        return entitiesList
    }


    private fun firebaseWrite(watchListEntity: WatchListEntity) {
        if (userUid == null) return
        firebaseDB.reference.child("users").child(userUid).child(watchListEntity.watchId).setValue(watchListEntity).addOnSuccessListener {
            Log.i("firebase", "firebaseWrite: ${watchListEntity.watchId} is added")
        }.addOnFailureListener {
            Log.i("firebase", "firebaseWrite: ${watchListEntity.watchId} is failed to add")
        }
    }

    private fun firebaseUpdateItem(item: WatchListEntity) {
        if (userUid == null) return
        firebaseDB.reference.child("users/$userUid").child(item.watchId).updateChildren(item.toMap())
    }

    private fun firebaseDelete(watchListEntity: WatchListEntity) {
        if (userUid == null) return
        firebaseDB.reference.child("users").child(userUid).child(watchListEntity.watchId).removeValue().addOnSuccessListener {
            Log.i("firebase", "firebaseDelete: ${watchListEntity.watchId} is removed")
        }.addOnFailureListener {
            Log.i("firebase", "firebaseDelete: ${watchListEntity.watchId} is failed to remove")
        }
    }

    // SwipeRefresh update function, callback to callback filtering, disgusting function
    fun updateData(isLoggedOut: Boolean, callback: ResultCallback?) {
        val latch = CountDownLatch(watchList.size)

        getSupportedSymbols()

        if (watchList.isEmpty()) {
            callback?.onResult(false, "List is empty")
            return
        }
        watchList.forEach {
            getRateAndFluctuation(it.base, it.quote, SQLMethod.UPDATE, object : ResultCallback {
                override fun onResult(success: Boolean, result: String?) {
                    latch.countDown()
                    if (latch.count == 0L) {
                        if (success) callback?.onResult(true, null)
                        else callback?.onResult(false, result)
                    }
                }
            })
        }

        loadWatchList(isLoggedOut)
    }
}

class MainActivityViewModelFactory(private val appDatabase: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainActivityViewModel(appDatabase) as T
    }
}

enum class SQLMethod {
    ADD,
    UPDATE,
    DELETE
}
