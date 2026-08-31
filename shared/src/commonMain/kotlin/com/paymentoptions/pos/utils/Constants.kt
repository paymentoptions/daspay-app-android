package com.paymentoptions.pos.utils

const val retrofitTimeout: Long = 15
const val inProduction: Boolean = true

/**
 * MineSec SoftPOS profile IDs. 
 * Note: Android and iOS require separate profiles in the MineSec dashboard.
 */
const val MINESEC_PROFILE_ID_ANDROID = "prof_01KH8NQC4PVFKRNH31ZPC2QJNN"
const val MINESEC_PROFILE_ID_IOS     = "prof_01KH8NQC4PVFKRNH31ZPC2QJNN"

// Temporary alias for backward compatibility with existing Android code
const val MINESEC_PROFILE_ID = MINESEC_PROFILE_ID_ANDROID

//const private val MINESEC_PROFILE_ID_DEV = "prof_01KH8NQC4PVFKRNH31ZPC2QJNN"
//
//const private val MINESEC_PROFILE_ID_PROD = "prof_01KRMSZV8148Z2REJ43VW9HQNE"
//
//const val MINESEC_PROFILE_ID = MINESEC_PROFILE_ID_DEV

const private val MINESEC_LICENCE_DEV = "payment-options.license"

const private val MINESEC_LICENCE_PROD = "prod/payment-options.license"

const val MINESEC_LICENCE_FILE = MINESEC_LICENCE_DEV

