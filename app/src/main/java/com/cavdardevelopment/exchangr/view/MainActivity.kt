package com.cavdardevelopment.exchangr.view

import androidx.appcompat.app.AppCompatActivity

import kotlin.math.abs

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect

import android.os.Build
import android.os.Bundle
import android.util.Log

import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager

import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import android.widget.Toast

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import com.cavdardevelopment.exchangr.BuildConfig

import com.cavdardevelopment.exchangr.R
import com.cavdardevelopment.exchangr.databinding.ActivityMainBinding
import com.cavdardevelopment.exchangr.model.database.AppDatabase
import com.cavdardevelopment.exchangr.util.listener.AppBarState
import com.cavdardevelopment.exchangr.util.callbacks.ItemMoveCallback
import com.cavdardevelopment.exchangr.util.callbacks.ResultCallback
import com.cavdardevelopment.exchangr.util.extensions.addOnStateChangedListener
import com.cavdardevelopment.exchangr.util.extensions.normalize
import com.cavdardevelopment.exchangr.view.adapter.WatchListAdapter
import com.cavdardevelopment.exchangr.viewmodel.MainActivityViewModel
import com.cavdardevelopment.exchangr.viewmodel.MainActivityViewModelFactory
import com.cavdardevelopment.exchangr.viewmodel.SQLMethod
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainActivityViewModel: MainActivityViewModel
    private lateinit var watchListAdapter: WatchListAdapter
    private lateinit var appDatabase: AppDatabase
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        toolbarConfiguration()

        // Inits
        sharedPreferences = this.getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
        appDatabase = AppDatabase.databaseInstance(this.applicationContext)
        mainActivityViewModel = ViewModelProvider(this, MainActivityViewModelFactory(appDatabase))[MainActivityViewModel::class.java]
        // When the data inside the recyclerViewAdapter updates,
        // reorderDatabase function is called to save the reordered list into database
        // deleteFromDatabase function is called to delete a record from the database
        watchListAdapter = WatchListAdapter({mainActivityViewModel.updateWatchListOrder(it)}, {mainActivityViewModel.removeWatchListItem(it)})
        val callback: ItemTouchHelper.Callback = ItemMoveCallback(watchListAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(binding.watchListRecyclerView)

        // Api calls and database saves&loads
            // SwipeRefresh refresh icon is visible instead of showing blank screen
            // until data are loaded and downloaded
        binding.swipeRefresh.isRefreshing = true
        mainActivityViewModel.getSupportedSymbols()
        mainActivityViewModel.loadWatchList(isLoggedOut()).invokeOnCompletion {
            binding.swipeRefresh.isRefreshing = false
        }
        mainActivityViewModel.updateData(isLoggedOut(), null)
        sharedPreferences.edit().remove("isLoggedOut").apply()

        // Autocomplete text views & SearchView
        val autoCompleteAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1).apply {
            binding.autoCompleteTextViewBase.setAdapter(this)
            binding.autoCompleteTextViewQuote.setAdapter(this)
        }

        // Observe live data
        mainActivityViewModel.supportedSymbolsLiveData.observe(this) {
            autoCompleteAdapter.clear()
            autoCompleteAdapter.addAll(it.codes)
            autoCompleteAdapter.notifyDataSetChanged()
        }

        mainActivityViewModel.watchListLiveData.observe(this) {
            watchListAdapter.setData(it)
            binding.watchListRecyclerView.adapter = watchListAdapter
        }

        // OnClick Listeners
        binding.addButton.setOnClickListener {
            binding.addButton.isEnabled = false

            val base = binding.autoCompleteTextViewBase.text.toString().uppercase()
            val quote = binding.autoCompleteTextViewQuote.text.toString().uppercase()

            mainActivityViewModel.getRateAndFluctuation(base, quote, SQLMethod.ADD,
                object : ResultCallback {
                    override fun onResult(success: Boolean, result: String?) {
                        if (!success) {
                            Log.i("info", result.toString())
                            Toast.makeText(applicationContext, result, Toast.LENGTH_LONG).show()
                        }

                        binding.autoCompleteTextViewBase.text.clear()
                        binding.autoCompleteTextViewQuote.text.clear()
                        binding.addButton.isEnabled = true
                    }
                })
        }

        // SwipeRefresh Listener
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = true
            mainActivityViewModel.updateData(isLoggedOut(), object : ResultCallback {
                override fun onResult(success: Boolean, result: String?) {
                    if (!success) {
                        Toast.makeText(applicationContext, result, Toast.LENGTH_LONG).show()
                    }
                    binding.swipeRefresh.isRefreshing = false
                }
            })
        }

    }


    private fun toolbarConfiguration() {
        setSupportActionBar(binding.toolbar)
//        supportActionBar?.title = null
//        For the visibility, fade and translateY animations, custom listener is used
        binding.appBarLayout.addOnStateChangedListener {appBarLayout, state, verticalOffset ->
            when (state) {
                AppBarState.EXPANDED -> {
                    binding.toolbar.visibility = View.GONE
                    binding.headerLinearLayout.visibility = View.VISIBLE
                }
                AppBarState.COLLAPSED -> {
                    binding.toolbar.visibility = View.VISIBLE
                    binding.headerLinearLayout.visibility = View.GONE
                }
                else -> {
                    // AppBarState.IDLE
                    binding.toolbar.visibility = View.VISIBLE
                    binding.headerLinearLayout.visibility = View.VISIBLE
                }
            }

            binding.toolbar.translationY = abs(verticalOffset.toFloat()).normalize(0f, appBarLayout.totalScrollRange.toFloat(), binding.toolbar.height.toFloat()/3f, 0f)
            binding.headerLinearLayout.translationY = abs(verticalOffset.toFloat()).normalize(0f, appBarLayout.totalScrollRange.toFloat(), 0f, binding.appBarLayout.height.toFloat())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                binding.toolbar.transitionAlpha = abs(verticalOffset.toFloat()).normalize(0f, appBarLayout.totalScrollRange.toFloat(), 0f, 1f)
                binding.headerLinearLayout.transitionAlpha = abs(verticalOffset.toFloat()).normalize(0f, appBarLayout.totalScrollRange.toFloat(), 1f, 0f)
            }
            else {
                binding.toolbar.alpha = abs(verticalOffset.toFloat()).normalize(0f, appBarLayout.totalScrollRange.toFloat(), 0f, 1f)
                binding.headerLinearLayout.alpha = abs(verticalOffset.toFloat()).normalize(0f, appBarLayout.totalScrollRange.toFloat(), 1f, 0f)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_menu, menu)

        val searchItem = menu.findItem(R.id.menu_item_search_view)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                mainActivityViewModel.filterWatchList(newText)
                return false
            }

        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_add_button -> {
                binding.appBarLayout.setExpanded(true)
                true
            }
            R.id.menu_item_search_view -> true
            R.id.menu_item_swipe_refresh -> {
                binding.swipeRefresh.isRefreshing = true
                mainActivityViewModel.updateData(isLoggedOut(), object : ResultCallback {
                    override fun onResult(success: Boolean, result: String?) {
                        if (!success) {
                            Toast.makeText(applicationContext, result, Toast.LENGTH_LONG).show()
                        }
                        binding.swipeRefresh.isRefreshing = false
                    }
                })
                true
            }
            R.id.menu_item_supported_symbols -> {
                startActivity(Intent(this, SupportedSymbolsActivity::class.java))
                true
            }
            R.id.menu_item_log_out -> {
                Firebase.auth.signOut()
                sharedPreferences.edit().putBoolean("isLoggedOut", true).apply()
                mainActivityViewModel.clearDatabase()
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Clear focus when touched outside of EditTexts and SearchView
        if (ev != null) {
            if (ev.action == MotionEvent.ACTION_DOWN) {
                val v = currentFocus
                if (v is EditText) {
                    val outRect = Rect()
                    v.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                        v.clearFocus()
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.windowToken, 0)
                    }
                }
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    private fun isLoggedOut(): Boolean {
        return sharedPreferences.getBoolean("isLoggedOut", false)
    }
}