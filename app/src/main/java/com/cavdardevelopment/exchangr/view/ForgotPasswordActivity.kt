package com.cavdardevelopment.exchangr.view

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.cavdardevelopment.exchangr.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth


class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }
        binding.sendCodeButton.setOnClickListener{ sendCodeButtonOnClick() }
    }

    private fun sendCodeButtonOnClick() {
        val email = binding.emailEditText.text.toString().trim()
        if (email.isBlank()) return
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener {
            Toast.makeText(this, "Password reset mail sent. Please check your mail.", Toast.LENGTH_LONG).show()
            finish()
        }

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}