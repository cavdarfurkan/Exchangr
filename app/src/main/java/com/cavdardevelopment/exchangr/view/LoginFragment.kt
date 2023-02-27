package com.cavdardevelopment.exchangr.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.cavdardevelopment.exchangr.databinding.FragmentLoginBinding
import com.cavdardevelopment.exchangr.util.callbacks.ResultCallback
import com.cavdardevelopment.exchangr.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        // Initializations
        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]

        // OnClickListeners
        binding.signInButton.setOnClickListener {logInOnClick()}
        binding.guestTextButton.setOnClickListener {asGuestOnClick()}

        return binding.root
    }

    private fun logInOnClick() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        if (email.isBlank() || password.isBlank()) return
        authViewModel.signIn(email, password, logInCallback())
    }

    private fun asGuestOnClick() {
        startMainActivity()
    }

    private fun startMainActivity() {
        startActivity(Intent(activity, MainActivity::class.java))
        activity?.finish()
    }

    private fun logInCallback() = object : ResultCallback {
        override fun onResult(success: Boolean, result: String?) {
            if (success) {
                startMainActivity()
                return
            }
            else {
                Toast.makeText(requireContext(), result, Toast.LENGTH_LONG).show()
            }
        }

        override fun onFailed(e: Exception?) {
            super.onFailed(e)
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    binding.emailTextInputLayout.isErrorEnabled = true
                    binding.emailEditText.error = e.message
                }
            }
        }
    }
}
