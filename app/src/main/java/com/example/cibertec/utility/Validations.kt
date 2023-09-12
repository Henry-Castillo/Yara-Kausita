package com.example.cibertec.utility

import android.util.Patterns

object Validations {
    fun validateMobile(mobile: String): Boolean {
        return mobile.length == 9
    }

    fun validatePasswordLength(password: String): Boolean {
        return password.length >= 4
    }

    fun validateEmail(email: String): Boolean {
        return (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches())
    }
}