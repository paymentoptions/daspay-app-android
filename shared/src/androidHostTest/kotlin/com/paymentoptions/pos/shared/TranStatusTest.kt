package com.paymentoptions.pos.shared

import com.paymentoptions.pos.network.endpoints.isSuccessfulTranStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TranStatusTest {
    @Test
    fun onlyApprovedVoidedAdjustedCountAsSuccess() {
        listOf("APPROVED", "VOIDED", "ADJUSTED", "approved", " Voided ").forEach {
            assertTrue(it, isSuccessfulTranStatus(it))
        }
        listOf("DECLINED", "FAILED", "PENDING", "", null).forEach {
            assertFalse(it.toString(), isSuccessfulTranStatus(it))
        }
    }
}
