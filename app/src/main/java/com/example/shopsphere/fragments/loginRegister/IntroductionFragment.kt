package com.example.shopsphere.fragments.loginRegister

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.shopsphere.R
import com.example.shopsphere.activities.ShoppingActivity
import com.example.shopsphere.databinding.FragmentIntroductionBinding
import com.example.shopsphere.viewmodel.IntroductionViewModel
import com.example.shopsphere.viewmodel.IntroductionViewModel.Companion.ACCOUNT_OPTIONS_FRAGMENT
import com.example.shopsphere.viewmodel.IntroductionViewModel.Companion.SHOPPING_ACTIVITY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class IntroductionFragment : Fragment(R.layout.fragment_introduction) {

    private lateinit var binding: FragmentIntroductionBinding
    private val viewModel by viewModels<IntroductionViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentIntroductionBinding.inflate(inflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigate.collect() {
                    when (it) {
                        SHOPPING_ACTIVITY -> {
                            Intent(requireActivity(), ShoppingActivity::class.java).also { intent ->
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                // pop the activity from the stack so that when users log into their account, they will navigate to the shopping activity and if they navigate back we dont need to go back to login and register activity
                                startActivity(intent)
                            }
                        }

                        ACCOUNT_OPTIONS_FRAGMENT -> {
                            findNavController().navigate(R.id.action_introductionFragment_to_accountOptionsFragment)
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonStart.setOnClickListener {
            viewModel.startButtonClick()
            findNavController().navigate(R.id.action_introductionFragment_to_accountOptionsFragment)
        }
    }
}