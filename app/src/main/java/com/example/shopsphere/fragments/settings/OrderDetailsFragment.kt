package com.example.shopsphere.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopsphere.R
import com.example.shopsphere.adapters.BillingProductsAdapter
import com.example.shopsphere.data.order.OrderStatus
import com.example.shopsphere.data.order.getOrderStatus
import com.example.shopsphere.databinding.FragmentOrderDetailsBinding

class OrderDetailsFragment : Fragment() {

    private lateinit var binding: FragmentOrderDetailsBinding
    private val billingProductsAdapter by lazy { BillingProductsAdapter() }
    private val args by navArgs<OrderDetailsFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderDetailsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val order = args.order

        setupOrderRv()

        binding.apply {
            tvOrderId.text = "Order #${order.orderId}"

            stepView.setSteps(
                mutableListOf(
                    OrderStatus.Ordered.status,
                    OrderStatus.Confirmed.status,
                    OrderStatus.Shipped.status,
                    OrderStatus.Delivered.status
                )
            )

            val currentOrderStatus = when (getOrderStatus(order.orderStatus)) {
                is OrderStatus.Ordered -> 0
                is OrderStatus.Confirmed -> 1
                is OrderStatus.Shipped -> 2
                is OrderStatus.Delivered -> 3
                else -> 0
            }

            if (order.orderStatus == OrderStatus.Returned.status || order.orderStatus == OrderStatus.Cancelled.status) {
                binding.stepView.visibility = View.GONE
            }

            stepView.go(currentOrderStatus, true)
            if (currentOrderStatus == 3) {
                stepView.done(true)
            }

            tvFullName.text = order.address.fullName
            tvAddress.text =
                "${order.address.addressLine1}\n${order.address.street}\n${order.address.state}"
            tvPhoneNumber.text = order.address.phone

            tvTotalprice.text = "$ ${order.totalPrice}"
        }

        billingProductsAdapter.differ.submitList(order.products)

        binding.imgCloseOrder.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupOrderRv() {
        binding.rvProducts.apply {
            adapter = billingProductsAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }
    }
}