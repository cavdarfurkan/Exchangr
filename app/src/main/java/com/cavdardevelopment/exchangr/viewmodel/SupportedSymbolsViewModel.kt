package com.cavdardevelopment.exchangr.viewmodel

import androidx.lifecycle.*
import com.cavdardevelopment.exchangr.model.database.AppDatabase
import com.cavdardevelopment.exchangr.model.response.SupportedSymbolsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SupportedSymbolsViewModel(private val appDatabase: AppDatabase) : ViewModel() {
    private var supportedSymbolsLiveData = MutableLiveData<SupportedSymbolsResponse>()

    fun loadSupportedSymbols() {
        val arrayListDescriptions = ArrayList<String>()
        val arrayListCodes = ArrayList<String>()

        viewModelScope.launch(Dispatchers.IO) {
            appDatabase.databaseDAO().getAllSupportedSymbols().forEach {
                arrayListDescriptions.add(it.symbolDescription)
                arrayListCodes.add(it.symbolCode)
            }

            supportedSymbolsLiveData.postValue(SupportedSymbolsResponse(true, arrayListDescriptions, arrayListCodes))
        }
    }

    fun observeSupportedSymbolsLiveData() : LiveData<SupportedSymbolsResponse> = supportedSymbolsLiveData
}

class SupportedSymbolsViewModelFactory(private val appDatabase: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SupportedSymbolsViewModel(appDatabase) as T
    }
}
