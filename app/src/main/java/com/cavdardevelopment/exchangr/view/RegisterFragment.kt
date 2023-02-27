package com.cavdardevelopment.exchangr.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.cavdardevelopment.exchangr.databinding.FragmentRegisterBinding
import com.cavdardevelopment.exchangr.util.callbacks.ResultCallback
import com.cavdardevelopment.exchangr.util.exception.ConfirmPasswordException
import com.cavdardevelopment.exchangr.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        // Initializations
        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]

        // OnClickListeners
        binding.signInButton.setOnClickListener {registerOnClick()}

        return binding.root
    }

    private fun registerOnClick() {
        val email = binding.emailEditText.text.toString().trim()
        val password1 = binding.passwordEditText1.text.toString().trim()
        val password2 = binding.passwordEditText2.text.toString().trim()
        if (email.isBlank() || password1.isBlank() || password2.isBlank()) return
        authViewModel.createAccount(email, password1, password2, registerCallback())
    }

    private fun startMainActivity() {
        startActivity(Intent(activity, MainActivity::class.java))
        activity?.finish()
    }

    private fun registerCallback() = object : ResultCallback {
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
                is FirebaseAuthWeakPasswordException -> {
                    binding.passwordTextInputLayout1.isErrorEnabled = true
                    binding.passwordTextInputLayout2.isErrorEnabled = true
                    binding.passwordEditText2.error = e.message
                }
                is FirebaseAuthInvalidCredentialsException -> {
                    binding.emailTextInputLayout.isErrorEnabled = true
                    binding.emailEditText.error = e.message
                }
                is ConfirmPasswordException -> {
                    binding.passwordTextInputLayout1.isErrorEnabled = true
                    binding.passwordTextInputLayout2.isErrorEnabled = true
                    binding.passwordEditText1.error = e.message
                    binding.passwordEditText2.error = e.message
                }
            }
        }
    }
}
