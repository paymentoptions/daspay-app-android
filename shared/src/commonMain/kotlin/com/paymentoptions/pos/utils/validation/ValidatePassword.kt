package com.paymentoptions.pos.utils.validation


fun validatePassword(password: String): Boolean {
    return password.length >= 8 && password.length <= 32
}