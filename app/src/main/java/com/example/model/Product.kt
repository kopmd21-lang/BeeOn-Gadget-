package com.example.model

import androidx.annotation.DrawableRes
import java.text.NumberFormat
import java.util.Locale

enum class ProductCategory(val displayName: String, val shortLabel: String) {
    ALL("All Products", "All"),
    AUDIO("Audio", "Audio"),
    MOBILE_ACCESSORIES("Mobile Accessories", "Mobile"),
    SMART_GADGETS("Smart Gadgets", "Smart"),
    ACCESSORIES("Accessories", "Cables")
}

enum class IllustrationType {
    HEADPHONES,
    MINI_UPS,
    PHONE_STAND,
    CHARGING_CABLE,
    ACCESSORIES_COMBO
}

sealed interface ProductVisual {
    data class Photo(@DrawableRes val resId: Int) : ProductVisual
    data class StudioIllustration(val type: IllustrationType) : ProductVisual
}

data class Product(
    val id: Int,
    val name: String,
    val category: ProductCategory,
    val price: Int,
    val originalPrice: Int,
    val discountPercent: Int,
    val rating: Double,
    val reviewCount: Int,
    val soldLabel: String,
    val isFlashDeal: Boolean,
    val tag: String,
    val description: String,
    val highlights: List<String>,
    val visual: ProductVisual
)

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val subtotal: Int get() = product.price * quantity
}

object CurrencyFormatter {
    private val bdFormat: NumberFormat by lazy {
        NumberFormat.getIntegerInstance(Locale("en", "BD"))
    }

    fun formatBdt(amount: Int): String {
        return "৳${bdFormat.format(amount)}"
    }
}
