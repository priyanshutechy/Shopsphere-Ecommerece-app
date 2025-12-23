package com.example.shopsphere.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopsphere.R
import com.example.shopsphere.adapters.HomeViewpagerAdapter
import com.example.shopsphere.adapters.SearchProductAdapter
import com.example.shopsphere.databinding.FragmentHomeBinding
import com.example.shopsphere.fragments.categories.AccessoryFragment
import com.example.shopsphere.fragments.categories.ChairFragment
import com.example.shopsphere.fragments.categories.CupboardFragment
import com.example.shopsphere.fragments.categories.FurnitureFragment
import com.example.shopsphere.fragments.categories.MainCategoryFragment
import com.example.shopsphere.fragments.categories.TableFragment
import com.example.shopsphere.util.Resource
import com.example.shopsphere.util.hideBottomNavigationView
import com.example.shopsphere.util.showBottomNavigationView
import com.example.shopsphere.viewmodel.HomeSearchViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val searchProductAdapter by lazy { SearchProductAdapter() }
    private val viewModel by viewModels<HomeSearchViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSearchRv()

        searchProductAdapter.onProductClick = {
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment, b)
        }

        val categoriesFragments = arrayListOf<Fragment>(
            MainCategoryFragment(),
            ChairFragment(),
            CupboardFragment(),
            TableFragment(),
            AccessoryFragment(),
            FurnitureFragment()
        )

        binding.viewpagerHome.isUserInputEnabled = false

        val viewPager2Adapter =
            HomeViewpagerAdapter(categoriesFragments, childFragmentManager, lifecycle)
        binding.viewpagerHome.adapter = viewPager2Adapter
        TabLayoutMediator(binding.tabLayout, binding.viewpagerHome) { tab, position ->
            when (position) {
                0 -> tab.text = "Main"
                1 -> tab.text = "Chair"
                2 -> tab.text = "Cupboard"
                3 -> tab.text = "Table"
                4 -> tab.text = "Accessory"
                5 -> tab.text = "Furniture"

            }
        }.attach()

        binding.searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (!query.isNullOrBlank()) {
                        viewModel.searchProducts(query)
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (!newText.isNullOrBlank()) {
                        viewModel.searchProducts(newText)
                    } else {
                        viewModel.searchProducts("")
                    }
                    return true
                }

            })

            setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.searchConstraintLayout.visibility = View.VISIBLE
                    hideBottomNavigationView()
                } else {
                    setQuery("", false)
                    binding.searchConstraintLayout.visibility = View.GONE
                    showBottomNavigationView()
                }
            }

            setOnCloseListener {
                binding.searchConstraintLayout.visibility = View.GONE
                true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.search.collectLatest {
                    when (it) {
                        is Resource.Loading -> {

                        }

                        is Resource.Success -> {
                            if (it.data.isNullOrEmpty()){
                                binding.searchConstraintLayout.visibility = View.GONE
                            }else{
                                binding.searchConstraintLayout.visibility = View.VISIBLE
                            }
                                searchProductAdapter.differ.submitList(it.data)
                        }

                        is Resource.Error -> {
                            binding.searchConstraintLayout.visibility = View.GONE
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
    }

    private fun setupSearchRv() {
        binding.searchRv.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = searchProductAdapter
        }
    }
}