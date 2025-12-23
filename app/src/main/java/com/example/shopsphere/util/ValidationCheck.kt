package com.example.shopsphere.util

import android.util.Patterns

fun validateEmail(email: String): RegisterValidation {
    if (email.isEmpty())
        return RegisterValidation.Failed("Email cannot be empty")

    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        return RegisterValidation.Failed("Incorrect email format")

    return RegisterValidation.Success
}

fun validatePassword(password: String): RegisterValidation {
    if (password.isEmpty())
        return RegisterValidation.Failed("Password cannot be empty")

    if (password.length < 8)
        return RegisterValidation.Failed("Password should contain at least 8 characters ")

    return RegisterValidation.Success
}
