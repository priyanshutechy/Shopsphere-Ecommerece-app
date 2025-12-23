package com.example.shopsphere.fragments.loginRegister

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.shopsphere.R
import com.example.shopsphere.activities.ShoppingActivity
import com.example.shopsphere.databinding.FragmentLoginBinding
import com.example.shopsphere.dialog.setupBottomSheetDialog
import com.example.shopsphere.util.Resource
import com.example.shopsphere.viewmodel.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDontHaveAccountRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.apply {
            buttonLoginLogin.setOnClickListener {
                val email = etEmailLogin.text.toString().trim()
                val password = etPasswordLogin.text.toString().trim()
                viewModel.login(email, password)
            }
        }

        binding.tvForgotPasswordLogin.setOnClickListener {
            setupBottomSheetDialog { email ->
                viewModel.resetPassword(email)
            }
        }

        binding.googleLogin.setOnClickListener {
            viewModel.googleLogin(requireContext())
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.login.collect {
                    when (it) {
                        is Resource.Loading -> {
                            binding.buttonLoginLogin.startAnimation()
                        }

                        is Resource.Success -> {
                            binding.buttonLoginLogin.revertAnimation()
                            Intent(
                                requireActivity(),
                                ShoppingActivity::class.java
                            ).also { intent ->
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                // pop the activity from the stack so that when users log into their account, they will navigate to the shopping activity and if they navigate back we dont need to go back to login and register activity
                                startActivity(intent)

                            }
                        }

                        is Resource.Error -> {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG)
                                .show()
                            binding.buttonLoginLogin.revertAnimation()
                        }

                        else -> Unit
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.resetPassword.collect {
                    when (it) {
                        is Resource.Loading -> {

                        }

                        is Resource.Success -> {
                            Snackbar.make(
                                requireView(),
                                "Reset link was sent to your email",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }

                        is Resource.Error -> {
                            Snackbar.make(
                                requireView(),
                                "Error: ${it.message}",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }
}