package com.paymentoptions.pos.utils.validation

import android.util.Patterns


fun validateEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}