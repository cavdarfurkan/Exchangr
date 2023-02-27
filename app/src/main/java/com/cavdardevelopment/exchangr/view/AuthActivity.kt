package com.cavdardevelopment.exchangr.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.cavdardevelopment.exchangr.databinding.ActivityAuthBinding
import com.cavdardevelopment.exchangr.view.adapter.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initializations
        auth = Firebase.auth
        auth.useAppLanguage()
        sharedPreferences = this.getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)

        val fragments = listOf(LoginFragment(), RegisterFragment())
        binding.viewPager.adapter = ViewPagerAdapter(this, fragments)

        // Starting items of tab and viewpager
        binding.viewPager.currentItem = 0
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0))

        // Set the selected tab when the viewPager swiped
        binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val tab = binding.tabLayout.getTabAt(position)
                binding.tabLayout.selectTab(tab)
                super.onPageSelected(position)
            }
        })

        // Set the correct fragment on viewPager when a tab selected
        binding.tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    binding.viewPager.setCurrentItem(it.position, true)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        val isLoggedOut = sharedPreferences.getBoolean("isLoggedOut", false)

        if (currentUser != null || !isLoggedOut) {
            Log.i("firebase", "onStart: Logged in")
            sharedPreferences.edit().remove("isLoggedOut").apply()
            startMainActivity()
        }
        else {
            sharedPreferences.edit().putBoolean("isLoggedOut", true).apply()
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}