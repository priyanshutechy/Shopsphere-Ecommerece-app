package com.example.shopsphere.fragments.shopping

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopsphere.R
import com.example.shopsphere.adapters.CartProductAdapter
import com.example.shopsphere.databinding.FragmentCartBinding
import com.example.shopsphere.firebase.FirebaseCommon
import com.example.shopsphere.util.Resource
import com.example.shopsphere.util.VerticalItemDecoration
import com.example.shopsphere.viewmodel.CartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CartFragment : Fragment(R.layout.fragment_cart) {

    private lateinit var binding: FragmentCartBinding
    private val cartProductAdapter by lazy { CartProductAdapter() }
    private val viewModel by activityViewModels<CartViewModel>()

    var totalPrice = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCartRv()

        binding.imageCloseCart.setOnClickListener {
            findNavController().navigateUp()
        }

        cartProductAdapter.onProductClick = {
            val b = Bundle().apply { putParcelable("product", it.product) }
            findNavController().navigate(R.id.action_cartFragment_to_productDetailsFragment, b)
        }

        cartProductAdapter.onPlusClick = {
            viewModel.changeQuantity(it, FirebaseCommon.QuantityChanging.INCREASE)
        }

        cartProductAdapter.onMinusClick = {
            viewModel.changeQuantity(it, FirebaseCommon.QuantityChanging.DECREASE)
        }

        binding.buttonCheckOut.setOnClickListener {
            val action = CartFragmentDirections.actionCartFragmentToBillingFragment(
                totalPrice,
                cartProductAdapter.differ.currentList.toTypedArray(),
                true
            )
            findNavController().navigate(action)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cartProducts.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.progressbarCart.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            binding.progressbarCart.visibility = View.INVISIBLE
                            if (it.data!!.isEmpty()) {
                                showEmptyCart()
                                hideOtherViews()
                            } else {
                                hideEmptyCart()
                                showOtherViews()
                                cartProductAdapter.differ.submitList(it.data)
                            }
                        }

                        is Resource.Error -> {
                            binding.progressbarCart.visibility = View.INVISIBLE
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
                viewModel.productPrice.collectLatest { price ->
                    price?.let {
                        totalPrice = it
                        binding.tvTotalPrice.text = "$ ${String.format("%.2f", price)}"
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteDialog.collectLatest {
                    val alertDialog = AlertDialog.Builder(requireContext()).apply {
                        setTitle("Delete item from cart")
                        setMessage("Do you want to delete this item from the cart?")
                        setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        setPositiveButton("Delete") { dialog, _ ->
                            viewModel.deleteCartProduct(it)
                            dialog.dismiss()
                        }
                    }
                    alertDialog.create()
                    alertDialog.show()
                }
            }
        }
    }

    private fun showOtherViews() {
        binding.apply {
            rvCart.visibility = View.VISIBLE
            totalBoxContainer.visibility = View.VISIBLE
            buttonCheckOut.visibility = View.VISIBLE
        }
    }

    private fun hideOtherViews() {
        binding.apply {
            rvCart.visibility = View.GONE
            totalBoxContainer.visibility = View.GONE
            buttonCheckOut.visibility = View.GONE
        }
    }

    private fun hideEmptyCart() {
        binding.apply {
            layoutCartEmpty.visibility = View.GONE
        }
    }

    private fun showEmptyCart() {
        binding.apply {
            layoutCartEmpty.visibility = View.VISIBLE
        }
    }

    private fun setupCartRv() {
        binding.rvCart.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = cartProductAdapter
            addItemDecoration(VerticalItemDecoration())
        }
    }

}