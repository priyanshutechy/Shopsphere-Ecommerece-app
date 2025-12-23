package com.example.shopsphere.fragments.shopping

import android.os.Bundle
import android.util.Log
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopsphere.R
import com.example.shopsphere.adapters.ColoursAdapter
import com.example.shopsphere.adapters.SizesAdapter
import com.example.shopsphere.adapters.ViewPager2ImagesAdapter
import com.example.shopsphere.data.CartProduct
import com.example.shopsphere.databinding.FragmentProductDetailsBinding
import com.example.shopsphere.util.Resource
import com.example.shopsphere.viewmodel.DetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductDetailsFragment : Fragment() {

    private val args by navArgs<ProductDetailsFragmentArgs>()
    private lateinit var binding: FragmentProductDetailsBinding
    private val viewPagerAdapter by lazy { ViewPager2ImagesAdapter() }
    private val sizesAdapter by lazy { SizesAdapter() }
    private val colourAdapter by lazy { ColoursAdapter() }
    private var selectedColour: Int? = null
    private var selectedSize: String? = null
    private val viewModel by viewModels<DetailsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductDetailsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product = args.product

        setupViewPager()
        setupSizesRv()
        setupColoursRv()

        binding.imageClose.setOnClickListener {
            findNavController().navigateUp()
        }

        sizesAdapter.onItemClick = {
            selectedSize = it
        }

        colourAdapter.onItemClick = {
            selectedColour = it
        }

        binding.buttonAddToCart.setOnClickListener {
            Log.d("TAG","Button clicked");
            viewModel.addUpdateProductInCart(CartProduct(product, 1, selectedColour, selectedSize))
        }

        binding.apply {
            tvProductName.text = product.name
            tvProductPrice.text = "$ ${product.price}"
            tvProductDescription.text = product.description

            if (product.colors.isNullOrEmpty())
                tvProductColours.visibility = View.INVISIBLE

            if (product.sizes.isNullOrEmpty())
                tvProductSize.visibility = View.INVISIBLE

        }

        viewPagerAdapter.differ.submitList(product.images)
        product.colors?.let { colourAdapter.differ.submitList(it) }
        product.sizes?.let { sizesAdapter.differ.submitList(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.addToCart.collectLatest {
                    when(it){
                        is Resource.Loading ->{
                            binding.buttonAddToCart.startAnimation()
                        }
                        is Resource.Success ->{
                            binding.buttonAddToCart.revertAnimation()
                        }
                        is Resource.Error ->{
                            binding.buttonAddToCart.revertAnimation()
                            Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun setupColoursRv() {
        binding.rvColour.apply {
            adapter = colourAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupSizesRv() {
        binding.rvSize.apply {
            adapter = sizesAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupViewPager() {
        binding.apply {
            viewPagerProductImages.adapter = viewPagerAdapter
        }
    }
}