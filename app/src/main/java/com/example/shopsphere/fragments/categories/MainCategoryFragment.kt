package com.example.shopsphere.fragments.categories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopsphere.R
import com.example.shopsphere.adapters.BestDealsAdapter
import com.example.shopsphere.adapters.BestProductsAdapter
import com.example.shopsphere.adapters.SpecialProductsAdapter
import com.example.shopsphere.databinding.FragmentMainCategoryBinding
import com.example.shopsphere.util.Resource
import com.example.shopsphere.util.hideBottomNavigationView
import com.example.shopsphere.util.showBottomNavigationView
import com.example.shopsphere.viewmodel.MainCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private val TAG = "MainCategoryFragment"

@AndroidEntryPoint
class MainCategoryFragment : Fragment(R.layout.fragment_main_category) {
    private lateinit var binding: FragmentMainCategoryBinding
    private lateinit var specialProductsAdapter: SpecialProductsAdapter
    private lateinit var bestDealsAdapter: BestDealsAdapter
    private lateinit var bestProductsAdapter: BestProductsAdapter
    private val viewModel by viewModels<MainCategoryViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        hideBottomNavigationView()
        binding = FragmentMainCategoryBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpecialProductsRv()
        setupBestDealsRv()
        setupBestProductsRv()

        specialProductsAdapter.onClick = {
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment, b)
        }

        bestDealsAdapter.onClick = {
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment, b)
        }

        bestProductsAdapter.onClick = {
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment, b)
        }

        binding.rvSpecialProducts.setOnScrollChangeListener { v, scrollX, _, _, _ ->
            if (v.bottom <= v.width + scrollX) {
                viewModel.fetchSpecialProducts()
            }
        }

        binding.rvBestDealsProducts.setOnScrollChangeListener { v, scrollX, _, _, _ ->
            if (v.bottom <= v.width + scrollX) {
                viewModel.fetchBestDeals()
            }
        }

        binding.nestedScrollMainCategory.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            if (v.getChildAt(0).bottom <= v.height + scrollY) {
                viewModel.fetchBestProducts()
            }
        })

    }

    private fun setupSpecialProductsRv() {
        specialProductsAdapter = SpecialProductsAdapter()
        binding.rvSpecialProducts.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = specialProductsAdapter
        }
    }

    private fun setupBestProductsRv() {
        bestProductsAdapter = BestProductsAdapter()
        binding.rvBestProducts.apply {
            layoutManager =
                GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            adapter = bestProductsAdapter
        }
    }

    private fun setupBestDealsRv() {
        bestDealsAdapter = BestDealsAdapter()
        binding.rvBestDealsProducts.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = bestDealsAdapter
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.specialProduct.collect {
                    when (it) {
                        is Resource.Loading -> {
                            binding.specialProductsProgressBar.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            specialProductsAdapter.differ.submitList(it.data)
                            binding.specialProductsProgressBar.visibility = View.GONE
                        }

                        is Resource.Error -> {
                            binding.specialProductsProgressBar.visibility = View.GONE
                            Log.e(TAG, it.message.toString())
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        else -> Unit
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bestDealsProduct.collect {
                    when (it) {
                        is Resource.Loading -> {
                            binding.bestDealsProgressBar.visibility = View.VISIBLE

                        }

                        is Resource.Success -> {
                            bestDealsAdapter.differ.submitList(it.data)
                            binding.bestDealsProgressBar.visibility = View.GONE
                        }

                        is Resource.Error -> {
                            binding.bestDealsProgressBar.visibility = View.GONE
                            Log.e(TAG, it.message.toString())
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        else -> Unit
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bestProducts.collect {
                    when (it) {
                        is Resource.Loading -> {
                            binding.bestProductsProgressBar.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            bestProductsAdapter.differ.submitList(it.data)
                            binding.bestProductsProgressBar.visibility = View.GONE
                        }

                        is Resource.Error -> {
                            binding.bestProductsProgressBar.visibility = View.GONE
                            Log.e(TAG, it.message.toString())
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun hideLoading() {
        binding.mainCategoryProgressBar.visibility = View.GONE
    }

    private fun showLoading() {
        binding.mainCategoryProgressBar.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()

        showBottomNavigationView()
    }

}