package com.paymentoptions.pos.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt

fun getDeviceTimeZone(): String = TimeZone.currentSystemDefault().id

fun getTimezoneOffset(): Int {
    val now = Clock.System.now()
    val zone = TimeZone.currentSystemDefault()
    return zone.offsetAt(now).totalSeconds * 1000
}
