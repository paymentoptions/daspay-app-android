package com.paymentoptions.pos.shared

import com.paymentoptions.pos.network.ConfigurationManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BaseUrlTest {
    @Test
    fun acceptsAbsoluteHttpUrls() {
        assertTrue(ConfigurationManager.isUsableBaseUrl("https://api-dev.paymentoptions.com/api/v1/"))
        assertTrue(ConfigurationManager.isUsableBaseUrl(" https://api.paymentoptions.com/api/v1/ "))
    }

    @Test
    fun rejectsValuesThatWouldSendRequestsToLocalhost() {
        listOf(null, "", "   ", "https:", "https://", "\$(CONFIG_BASE_URL)", "/api/v1/").forEach {
            assertFalse(it.toString(), ConfigurationManager.isUsableBaseUrl(it))
        }
    }
}
