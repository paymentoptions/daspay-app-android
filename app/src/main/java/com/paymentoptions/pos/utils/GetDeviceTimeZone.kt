package com.paymentoptions.pos.utils

import java.util.TimeZone

fun getDeviceTimeZone(): String {
    val timeZone = TimeZone.getDefault()
    return timeZone.id
}