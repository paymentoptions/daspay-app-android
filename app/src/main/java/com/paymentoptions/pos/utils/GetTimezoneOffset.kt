package com.paymentoptions.pos.utils

import java.util.GregorianCalendar

fun getTimezoneOffset(): Int {
    val mCalendar = GregorianCalendar()
    val mTimeZone = mCalendar.timeZone
    return mTimeZone.rawOffset
}