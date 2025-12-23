package com.example.shopsphere.fragments.shopping

import android.app.AlertDialog
import android.content.Context
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
import androidx.recyclerview.widget.RecyclerView
import com.example.shopsphere.R
import com.example.shopsphere.adapters.AddressAdapter
import com.example.shopsphere.adapters.BillingProductsAdapter
import com.example.shopsphere.data.Address
import com.example.shopsphere.data.CartProduct
import com.example.shopsphere.data.order.Order
import com.example.shopsphere.data.order.OrderStatus
import com.example.shopsphere.databinding.FragmentBillingBinding
import com.example.shopsphere.util.HorizontalItemDecoration
import com.example.shopsphere.util.Resource
import com.example.shopsphere.viewmodel.BillingViewModel
import com.example.shopsphere.viewmodel.OrderViewModel
import com.google.android.material.snackbar.Snackbar
import com.razorpay.Checkout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class BillingFragment : Fragment() {
    private lateinit var binding: FragmentBillingBinding
    private val addressAdapter by lazy { AddressAdapter() }
    private val billingProductsAdapter by lazy { BillingProductsAdapter() }
    val billingViewModel by viewModels<BillingViewModel>()
    private val args by navArgs<BillingFragmentArgs>()
    private var products = emptyList<CartProduct>()
    private var totalPrice = 0f

    private var selectedAddress: Address? = null
    private val orderViewModel by viewModels<OrderViewModel>()

    @Inject
    lateinit var application: Context

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBillingProductsRv()
        setupAddressRv()

        if (!args.payment) {
            binding.apply {
                buttonPlaceOrder.visibility = View.INVISIBLE
                totalBoxContainer.visibility = View.INVISIBLE
                middleLine.visibility = View.INVISIBLE
                bottomLine.visibility = View.INVISIBLE
            }
        }

        binding.imageCloseBilling.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.imageAddAddress.setOnClickListener {
            findNavController().navigate(R.id.action_billingFragment_to_addressFragment)
        }

        billingProductsAdapter.differ.submitList(products)

        binding.tvTotalPrice.text = "$ $totalPrice"

        billingProductsAdapter.onBillingProductClick = {
            val b = Bundle().apply { putParcelable("product", it.product) }
            findNavController().navigate(R.id.action_billingFragment_to_productDetailsFragment, b)
        }

        addressAdapter.onClick = {
            selectedAddress = it
            if (!args.payment) {
                val b = Bundle().apply { putParcelable("address", selectedAddress) }
                findNavController().navigate(R.id.action_billingFragment_to_addressFragment, b)
            }
        }

        binding.buttonPlaceOrder.setOnClickListener {
            if (selectedAddress == null) {
                Toast.makeText(requireContext(), "Please select an address", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            showOrderConfirmationDialog()
        }
    }

    private fun showOrderConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(requireContext()).apply {
            setTitle("Order items")
            setMessage("Do you want to order your cart items?")
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton("Order") { dialog, _ ->
                val order = Order(
                    OrderStatus.Ordered.status,
                    totalPrice,
                    products,
                    selectedAddress!!
                )
                orderViewModel.placeOrder(order)
                startPayment()
                dialog.dismiss()
            }
        }
        alertDialog.create()
        alertDialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
        * To ensure faster loading of the Checkout form,
        * call this method as early as possible in your checkout flow
        * */
        Checkout.preload(application)

        products = args.products.toList()
        totalPrice = args.totalPrice

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                billingViewModel.address.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.progressbarAddress.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            addressAdapter.differ.submitList(it.data)
                            binding.progressbarAddress.visibility = View.GONE
                        }

                        is Resource.Error -> {
                            binding.progressbarAddress.visibility = View.GONE
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
                orderViewModel.order.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.buttonPlaceOrder.startAnimation()
                        }

                        is Resource.Success -> {
                            binding.buttonPlaceOrder.revertAnimation()
                            findNavController().navigateUp()
                            Snackbar.make(
                                requireView(),
                                "Your order was placed!",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }

                        is Resource.Error -> {
                            binding.buttonPlaceOrder.revertAnimation()
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

    private fun startPayment() {
        try {
            val co = Checkout()
            co.setKeyID("rzp_test_FXQpXHXIzRl0XD")

            val options = JSONObject()
            options.put("name", "ShopSphere")
            options.put("description", "E=Commerce App")
            //You can omit the image option to fetch the image from the dashboard
            options.put("image", R.drawable.ic_kleine_shape)
            options.put("theme.color", "#FF3700B3");
            options.put("currency", "INR");
            options.put("amount", totalPrice.toInt() * 100)//pass amount in currency subunits

            val retryObj = JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);

            val prefill = JSONObject()
            prefill.put("email", "kuldeepjhala03@gmail.com")
            prefill.put("contact", "9167673619")

            options.put("prefill", prefill)
            co.open(activity, options)
        } catch (e: Exception) {
            Log.d("razorpay", "Error in payment: " + e.message)
            Toast.makeText(activity, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun setupAddressRv() {
        binding.rvAddress.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = addressAdapter
            addItemDecoration(HorizontalItemDecoration())
        }
    }

    private fun setupBillingProductsRv() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = billingProductsAdapter
            addItemDecoration(HorizontalItemDecoration())
        }
    }
}