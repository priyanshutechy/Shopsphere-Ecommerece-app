package com.example.shopsphere.fragments.categories

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.shopsphere.R
import com.example.shopsphere.data.Category
import com.example.shopsphere.util.Resource
import com.example.shopsphere.viewmodel.CategoryViewModel
import com.example.shopsphere.viewmodel.factory.BaseCategoryViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AccessoryFragment:BaseFragmentCategory() {

    @Inject
    lateinit var firestore: FirebaseFirestore

    val viewModel by viewModels<CategoryViewModel> {
        BaseCategoryViewModelFactory(firestore, Category.Accessory)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.offerProducts.collect {
                    when (it) {
                        is Resource.Loading -> {
                            showOfferLoading()
                        }

                        is Resource.Success -> {
                            offerAdapter.differ.submitList(it.data)
                            hideOfferLoading()
                        }

                        is Resource.Error -> {
                            Snackbar.make(
                                requireView(),
                                it.message.toString(),
                                Snackbar.LENGTH_LONG
                            ).show()
                            hideOfferLoading()
                        }

                        else -> Unit
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bestProduct.collect {
                    when (it) {
                        is Resource.Loading -> {
                            showBestProductLoading()
                        }

                        is Resource.Success -> {
                            bestProductsAdapter.differ.submitList(it.data)
                            hideBestProductLoading()
                        }

                        is Resource.Error -> {
                            Snackbar.make(
                                requireView(),
                                it.message.toString(),
                                Snackbar.LENGTH_LONG
                            ).show()
                            hideBestProductLoading()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onOfferPagingRequest() {
        viewModel.fetchOfferProducts()
    }

    override fun onBestProductsPagingRequest() {
        viewModel.fetchBestProducts()
    }
}