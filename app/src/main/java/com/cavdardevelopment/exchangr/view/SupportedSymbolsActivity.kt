package com.cavdardevelopment.exchangr.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.cavdardevelopment.exchangr.databinding.ActivitySupportedSymbolsBinding
import com.cavdardevelopment.exchangr.model.database.AppDatabase
import com.cavdardevelopment.exchangr.view.adapter.SupportedSymbolsAdapter
import com.cavdardevelopment.exchangr.viewmodel.SupportedSymbolsViewModel
import com.cavdardevelopment.exchangr.viewmodel.SupportedSymbolsViewModelFactory

class SupportedSymbolsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySupportedSymbolsBinding
    private lateinit var supportedSymbolsViewModel: SupportedSymbolsViewModel
    private lateinit var supportedSymbolsAdapter: SupportedSymbolsAdapter
    private lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportedSymbolsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar configuration
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Inits
        appDatabase = AppDatabase.databaseInstance(this.applicationContext)
        supportedSymbolsViewModel = ViewModelProvider(this, SupportedSymbolsViewModelFactory(appDatabase))[SupportedSymbolsViewModel::class.java]

        // API Call & Observe Live Data
        supportedSymbolsViewModel.loadSupportedSymbols()
        supportedSymbolsViewModel.observeSupportedSymbolsLiveData().observe(this) {
            supportedSymbolsAdapter = SupportedSymbolsAdapter(it)
            binding.supportedSymbolsRecyclerView.adapter = supportedSymbolsAdapter
        }
    }
}