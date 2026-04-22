package com.speedbump

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class SpeedbumpAccessibilityService : AccessibilityService() {

    private val TARGET_PACKAGES = setOf(
        "com.amazon.mShop.android.shopping", // Amazon Global
        "in.amazon.mShop.android.shopping",  // Amazon India
        "com.flipkart.android",              // Flipkart
        "com.meesho.supply",                 // Meesho
        "com.myntra.android",                // Myntra
        "com.ril.ajio",                      // Ajio
        "com.fsn.nykaa",                     // Nykaa
        "com.tul.tatacliq",                  // Tata CLiQ
        "com.jio.jiomart",                   // JioMart
        "com.tatadigital.tcp",               // Tata Neu
        "com.grofers.customerapp",           // Blinkit
        "com.zeptoconsumerapp",              // Zepto
        "com.bigbasket.mobileapp",           // BigBasket
        "com.dunzo.user",                    // Dunzo
        "in.swiggy.android",                 // Swiggy
        "com.application.zomato",            // Zomato
        "com.phonegap.rxcuriosity",          // PharmEasy
        "com.apollo.patientapp"              // Apollo 24/7
    )

    private val CHECKOUT_TEXTS = setOf(
        "Place Order",
        "Proceed to Buy",
        "Checkout"
    )

    private val CURRENCY_REGEX = Regex("₹\\s?([\\d,]+(\\.\\d{1,2})?)")
    private var hourlyWage: Double = 500.0
    private var overlay: SpeedbumpOverlay? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        try {
            Log.d("SpeedbumpService", "Service Connected")
            overlay = SpeedbumpOverlay(this)
        } catch (e: Exception) {
            Log.e("SpeedbumpService", "Error initializing overlay: ${e.message}")
        }
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        
        if (packageName in TARGET_PACKAGES) {
            val rootNode = rootInActiveWindow ?: return
            
            val prices = mutableListOf<Double>()
            var triggerFound = false

            traverseAndParse(rootNode, prices, { triggerFound = true })

            if (triggerFound && prices.isNotEmpty()) {
                val cartTotal = prices.maxOrNull() ?: 0.0
                onCheckoutDetected(packageName, cartTotal)
            }
        }
    }

    /**
     * Memory-efficient iterative traversal to find triggers and parse prices.
     */
    private fun traverseAndParse(
        rootNode: android.view.accessibility.AccessibilityNodeInfo,
        prices: MutableList<Double>,
        onTriggerFound: () -> Unit
    ) {
        val deque = ArrayDeque<android.view.accessibility.AccessibilityNodeInfo>()
        deque.add(rootNode)

        while (deque.isNotEmpty()) {
            val node = deque.removeFirst()
            
            val nodeText = node.text?.toString()?.trim() ?: ""
            val contentDesc = node.contentDescription?.toString()?.trim() ?: ""

            // 1. Check for Checkout Triggers
            if (CHECKOUT_TEXTS.any { it.equals(nodeText, ignoreCase = true) } ||
                CHECKOUT_TEXTS.any { it.equals(contentDesc, ignoreCase = true) }) {
                onTriggerFound()
            }

            // 2. Parse Prices
            extractPrice(nodeText)?.let { prices.add(it) }
            extractPrice(contentDesc)?.let { prices.add(it) }

            // 3. Traverse children
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { deque.add(it) }
            }
        }
    }

    private fun extractPrice(text: String): Double? {
        val match = CURRENCY_REGEX.find(text) ?: return null
        return try {
            match.groupValues[1].replace(",", "").toDouble()
        } catch (e: Exception) {
            null
        }
    }

    private fun onCheckoutDetected(packageName: String, cartTotal: Double) {
        val hoursLost = cartTotal / hourlyWage
        val formattedHours = "%.1f".format(hoursLost)
        
        Log.d("SpeedbumpService", "CHECKOUT_DETECTED in $packageName")
        Log.d("SpeedbumpService", "Cart Total: ₹$cartTotal")
        Log.d("SpeedbumpService", "Hours Lost: $formattedHours hours")
        
        // Launch aggressive overlay
        overlay?.show(hoursLost)
    }

    override fun onInterrupt() {
        Log.d("SpeedbumpService", "Service Interrupted")
    }
}
