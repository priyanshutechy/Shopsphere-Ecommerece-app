package com.example.shopsphere.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.shopsphere.R
import com.example.shopsphere.databinding.ActivityShoppingBinding
import com.example.shopsphere.fragments.settings.AllOrdersFragmentDirections
import com.example.shopsphere.fragments.shopping.BillingFragment
import com.example.shopsphere.fragments.shopping.BillingFragmentDirections
import com.example.shopsphere.fragments.shopping.BillingFragmentNavigator
import com.example.shopsphere.util.Resource
import com.example.shopsphere.viewmodel.CartViewModel
import com.example.shopsphere.viewmodel.OrderViewModel
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShoppingActivity : AppCompatActivity(), PaymentResultWithDataListener{

    private lateinit var ShoppingNavController: NavController

    private val binding by lazy {
        ActivityShoppingBinding.inflate(layoutInflater)
    }

    val viewmodel by viewModels<CartViewModel>()
    private val orderViewModel by viewModels<OrderViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val ShoppingNavHostFragment = supportFragmentManager
            .findFragmentById(R.id.Shopping_nav_host_fragment) as NavHostFragment
        ShoppingNavController = ShoppingNavHostFragment.navController

        binding.bottomNavigation.setupWithNavController(ShoppingNavController)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewmodel.cartProducts.collectLatest {
                    when (it) {
                        is Resource.Success -> {
                            val count = it.data?.size ?: 0
                            val bottomNavigation = binding.bottomNavigation
                            bottomNavigation.getOrCreateBadge(R.id.cartFragment).apply {
                                number = count
                                backgroundColor = resources.getColor(R.color.g_blue, null)
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
        Log.d("razorpay", "Payment Successful: $p0 \n Payment Data: $p1")
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show()
        orderViewModel.clearCart()
        Log.d("razorpay", "Cart Cleared")

    }

    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
        Log.d("razorpay", "Error in payment: $p1 \n Payment Data: $p2")
        Toast.makeText(this, "Payment Unsuccessful, some error occured", Toast.LENGTH_SHORT).show()
    }

}