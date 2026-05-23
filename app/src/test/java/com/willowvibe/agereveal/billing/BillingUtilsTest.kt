package com.willowvibe.agereveal.billing

import com.android.billingclient.api.BillingClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BillingUtilsTest {

    @Test
    fun `parseBillingPeriodToDays parses days`() {
        assertEquals(7, BillingUtils.parseBillingPeriodToDays("P7D"))
        assertEquals(1, BillingUtils.parseBillingPeriodToDays("P1D"))
        assertEquals(14, BillingUtils.parseBillingPeriodToDays("P14D"))
    }

    @Test
    fun `parseBillingPeriodToDays parses weeks`() {
        assertEquals(7, BillingUtils.parseBillingPeriodToDays("P1W"))
        assertEquals(14, BillingUtils.parseBillingPeriodToDays("P2W"))
    }

    @Test
    fun `parseBillingPeriodToDays parses months`() {
        assertEquals(30, BillingUtils.parseBillingPeriodToDays("P1M"))
        assertEquals(60, BillingUtils.parseBillingPeriodToDays("P2M"))
    }

    @Test
    fun `parseBillingPeriodToDays parses years`() {
        assertEquals(365, BillingUtils.parseBillingPeriodToDays("P1Y"))
        assertEquals(730, BillingUtils.parseBillingPeriodToDays("P2Y"))
    }

    @Test
    fun `parseBillingPeriodToDays returns null for invalid format`() {
        assertNull(BillingUtils.parseBillingPeriodToDays("invalid"))
        assertNull(BillingUtils.parseBillingPeriodToDays("7D"))
        assertNull(BillingUtils.parseBillingPeriodToDays(""))
    }

    @Test
    fun `parseBillingPeriodToDays is case insensitive`() {
        assertEquals(7, BillingUtils.parseBillingPeriodToDays("p7d"))
        assertEquals(30, BillingUtils.parseBillingPeriodToDays("p1m"))
    }

    @Test
    fun `billingErrorMessage returns human readable text`() {
        assertEquals("Play Store timed out. Please retry.",
            BillingUtils.billingErrorMessage(BillingClient.BillingResponseCode.SERVICE_TIMEOUT))
        assertEquals("Can't reach Play Store. Check your connection.",
            BillingUtils.billingErrorMessage(BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE))
        assertEquals("Billing is not available on this device.",
            BillingUtils.billingErrorMessage(BillingClient.BillingResponseCode.BILLING_UNAVAILABLE))
        assertEquals("You already own this subscription.",
            BillingUtils.billingErrorMessage(BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED))
        assertEquals("Purchase cancelled.",
            BillingUtils.billingErrorMessage(BillingClient.BillingResponseCode.USER_CANCELED))
    }

    @Test
    fun `billingErrorMessage returns generic fallback for unknown code`() {
        val msg = BillingUtils.billingErrorMessage(999)
        assertEquals(true, msg.contains("999"))
        assertEquals(true, msg.contains("Please retry"))
    }
}
