package com.paymentoptions.pos.shared

import com.paymentoptions.pos.network.MerchantSetting
import com.paymentoptions.pos.network.ProductListDataRecord
import com.paymentoptions.pos.ui.screens._flow.foodOrderFlow.Cart
import com.paymentoptions.pos.ui.screens._flow.foodOrderFlow.FoodItem
import com.paymentoptions.pos.ui.screens._flow.foodOrderFlow.getPayByLinkRequest
import com.paymentoptions.pos.utils.toPlainAmountString
import org.junit.Assert.assertEquals
import org.junit.Test

class CartTotalsTest {

    private fun item(price: Float, fee: Float?, feeEnabled: Boolean, qty: Int) = FoodItem(
        item = ProductListDataRecord(
            ProductName = "P$price",
            ProductPrice = price,
            ServiceFeeEnabled = feeEnabled,
            ServiceFeeAmount = fee,
        ),
        cartQuantity = qty,
    )

    // 2 × 10.00 with a 1.50 fee each, 1 × 5.00 whose fee is disabled.
    private fun cart(setting: MerchantSetting?, additional: Float = 0f) = Cart(
        foodItemMapByCategoryId = mutableMapOf(
            "c" to listOf(item(10f, 1.5f, true, 2), item(5f, 3f, false, 1)),
        ),
        itemQuantity = 3,
        itemTotal = 25f,
        additionalCharge = additional,
        merchantSetting = setting,
    )

    @Test
    fun serviceChargeIsPerItemFeeTimesQuantityForEnabledItemsOnly() {
        val c = cart(null).apply { updateTotals() }
        assertEquals(3f, c.serviceCharge, 0.001f)
        assertEquals(0f, c.gstCharge, 0.001f)
        assertEquals(28f, c.grandTotal, 0.001f)
    }

    @Test
    fun taxAppliesToItemsServiceAndAdditionalWhenCatalogEnabled() {
        val c = cart(MerchantSetting(TaxName = "GST", TaxOnOtherFeesPerc = 9, CatalogEnabled = true), additional = 2f)
            .apply { updateTotals() }
        // (25 + 3 + 2) × 9% = 2.70
        assertEquals(2.7f, c.gstCharge, 0.001f)
        assertEquals(32.7f, c.grandTotal, 0.001f)
    }

    @Test
    fun noTaxWhenCatalogDisabled() {
        val c = cart(MerchantSetting(TaxOnOtherFeesPerc = 9, CatalogEnabled = false)).apply { updateTotals() }
        assertEquals(0f, c.gstCharge, 0.001f)
    }

    @Test
    fun payByLinkLinesAddUpToGrandTotal() {
        val c = cart(MerchantSetting(TaxName = "GST", TaxOnOtherFeesPerc = 9, CatalogEnabled = true), additional = 2f)
            .apply { updateTotals() }
        val request = getPayByLinkRequest("test", c, "SGD")
        val sum = request.Product.sumOf { it.TotalPrice.replace(",", "").toDouble() }
        assertEquals(c.grandTotal.toDouble(), sum, 0.001)
        assertEquals("GST (9%)", request.Product.last().Name)
    }

    @Test
    fun plainAmountHasNoThousandsSeparator() {
        assertEquals("1234.50", 1234.5f.toPlainAmountString())
        assertEquals(1234.5f, 1234.5f.toPlainAmountString().toFloat(), 0.001f)
    }
}
