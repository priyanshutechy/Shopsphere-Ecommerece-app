package com.example.shopsphere.fragments.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopsphere.R
import com.example.shopsphere.adapters.BestProductsAdapter
import com.example.shopsphere.databinding.FragmentBaseCategoryBinding
import com.example.shopsphere.util.showBottomNavigationView

open class BaseFragmentCategory : Fragment(R.layout.fragment_base_category) {
    private lateinit var binding: FragmentBaseCategoryBinding
    protected val offerAdapter: BestProductsAdapter by lazy { BestProductsAdapter() }
    protected val bestProductsAdapter: BestProductsAdapter by lazy { BestProductsAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBaseCategoryBinding.inflate(inflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOfferRv()
        setupBestProductsRv()

        offerAdapter.onClick = {
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment, b)
        }

        bestProductsAdapter.onClick = {
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment, b)
        }

        binding.rvOfferProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollHorizontally(1) && dx != 0) {
                    onOfferPagingRequest()
                }
            }

        })

        binding.nestedScrollBaseCategory.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            if (v.getChildAt(0).bottom <= v.height + scrollY) {
                onBestProductsPagingRequest()
            }
        })
    }

    fun showOfferLoading() {
        binding.offerProductsProgressBar.visibility = View.VISIBLE
    }

    fun hideOfferLoading() {
        binding.offerProductsProgressBar.visibility = View.INVISIBLE
    }

    fun showBestProductLoading() {
        binding.bestProductsProgressBar.visibility = View.VISIBLE

    }

    fun hideBestProductLoading() {
        binding.bestProductsProgressBar.visibility = View.INVISIBLE

    }

    open fun onOfferPagingRequest() {

    }

    open fun onBestProductsPagingRequest() {

    }

    private fun setupOfferRv() {
        binding.rvOfferProducts.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = offerAdapter
        }
    }

    private fun setupBestProductsRv() {
        binding.rvBestProducts.apply {
            layoutManager =
                GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            adapter = bestProductsAdapter
        }
    }

    override fun onResume() {
        super.onResume()

        showBottomNavigationView()
    }

}