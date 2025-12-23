package com.example.shopsphere.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.shopsphere.data.Address
import com.example.shopsphere.databinding.FragmentAddressBinding
import com.example.shopsphere.util.Resource
import com.example.shopsphere.viewmodel.AddressViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddressFragment : Fragment() {

    private lateinit var binding: FragmentAddressBinding
    val viewModel by viewModels<AddressViewModel>()
    val args by navArgs<AddressFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addNewAddress.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.progressbarAddress.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            binding.progressbarAddress.visibility = View.INVISIBLE
                            findNavController().navigateUp()
                        }

                        is Resource.Error -> {
                            Toast.makeText(
                                requireContext(),
                                it.message.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> Unit
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collectLatest {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddressBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addressArgs = args.address
        if (addressArgs == null) {
            binding.buttonDelelte.visibility = View.GONE
        } else {
            binding.apply {
                etAddressTitle.setText(addressArgs.addressTitle)
                etFullName.setText(addressArgs.fullName)
                etAddressLine1.setText(addressArgs.addressLine1)
                etStreet.setText(addressArgs.street)
                etPhone.setText(addressArgs.phone)
                etCity.setText(addressArgs.city)
                etState.setText(addressArgs.state)
            }
        }

        binding.apply {
            buttonSave.setOnClickListener {
                val addressTitle = etAddressTitle.text.toString()
                val fullName = etFullName.text.toString()
                val addressLine1 = etAddressLine1.text.toString()
                val street = etStreet.text.toString()
                val phone = etPhone.text.toString()
                val city = etCity.text.toString()
                val state = etState.text.toString()

                val address =
                    Address(addressTitle, fullName, addressLine1, street, phone, city, state)

                viewModel.addAddress(address)
            }

            imageAddressClose.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }
}