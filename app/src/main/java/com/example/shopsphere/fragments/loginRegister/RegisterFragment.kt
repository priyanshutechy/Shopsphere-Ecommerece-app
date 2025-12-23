package com.example.shopsphere.fragments.loginRegister

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.shopsphere.R
import com.example.shopsphere.activities.ShoppingActivity
import com.example.shopsphere.data.User
import com.example.shopsphere.databinding.FragmentRegisterBinding
import com.example.shopsphere.util.RegisterFieldState
import com.example.shopsphere.util.RegisterValidation
import com.example.shopsphere.util.Resource
import com.example.shopsphere.viewmodel.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val TAG = "RegisterFragment"

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val viewModel by viewModels<RegisterViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {

            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.register.collect {
                    when (it) {
                        is Resource.Loading -> {
                            Log.d("registerButtom", "button click loading")
                            binding.buttonRegisterRegister.startAnimation()

                        }

                        is Resource.Success -> {
                            Log.d(TAG, it.data.toString())
                            binding.buttonRegisterRegister.revertAnimation()
                        }

                        is Resource.Error -> {
                            Log.d(TAG, it.message.toString())
                            binding.buttonRegisterRegister.revertAnimation()
                        }

                        else -> Unit
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.validation.collect { validation ->
                    if (validation.email is RegisterValidation.Failed) {
                        withContext(Dispatchers.Main) {
                            binding.etEmailRegister.apply {
                                requestFocus()
                                error = validation.email.message
                            }
                        }
                    }
                    if (validation.password is RegisterValidation.Failed) {
                        withContext(Dispatchers.Main) {
                            binding.etPasswordRegister.apply {
                                requestFocus()
                                error = validation.password.message
                            }
                        }
                    }

                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.googleRegister.collect {
                    when (it) {
                        is Resource.Loading -> {
                            binding.buttonRegisterRegister.startAnimation()
                        }

                        is Resource.Success -> {
                            binding.buttonRegisterRegister.revertAnimation()
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
                            binding.buttonRegisterRegister.revertAnimation()
                        }

                        else -> Unit
                    }
                }
            }
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        binding.buttonRegisterRegister.setOnClickListener {
            Log.e("registerButtom", "button clicked")
            val user = User(
                binding.etFirstNameRegister.text.toString().trim(),
                binding.etLastNameRegister.text.toString().trim(),
                binding.etEmailRegister.text.toString().trim()
            )
            val password = binding.etPasswordRegister.text.toString()
            viewModel.createAccountWithEmailAndPassword(user, password)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDoYouHaveAccountLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        binding.googleLogin.setOnClickListener {
            viewModel.googleLogin(requireContext())
        }

    }
}