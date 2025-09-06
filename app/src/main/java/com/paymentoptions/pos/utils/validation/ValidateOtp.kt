package com.paymentoptions.pos.utils.validation

fun validateOtp(otp: String): Boolean {
    return otp.length == 6
}