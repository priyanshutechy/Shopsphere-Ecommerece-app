package com.example.shopsphere.util

sealed class RegisterValidation() {
    data object Success: RegisterValidation()
    data class Failed(val message:String):RegisterValidation()
}

data class RegisterFieldState(
    val email:RegisterValidation,
    val password:RegisterValidation
)