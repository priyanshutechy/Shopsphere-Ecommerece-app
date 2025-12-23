package com.example.shopsphere.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.shopsphere.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginRegisterActivity : AppCompatActivity(R.layout.activity_login_register) {

    private lateinit var LoginNavController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val LoginNavHostFragment = supportFragmentManager
            .findFragmentById(R.id.Login_nav_host_fragment) as NavHostFragment
        LoginNavController = LoginNavHostFragment.navController

    }
}