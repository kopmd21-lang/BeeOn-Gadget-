package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.R
import com.example.model.IllustrationType
import com.example.model.LocalUserAccount
import com.example.model.OrderRecord
import com.example.model.PaymentMethod
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.model.ProductVisual
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "beeon_gadget_prefs")

data class PersistedStorePrefs(
    val cartQuantities: Map<Int, Int> = emptyMap(),
    val whatsappNumber: String = StoreRepository.DEFAULT_WHATSAPP_NUMBER,
    val customerName: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CASH_ON_DELIVERY,
    val registeredAccounts: List<LocalUserAccount> = emptyList(),
    val activeUserPhone: String = "",
    val orderHistory: List<OrderRecord> = emptyList()
)

class StoreRepository(private val context: Context) {

    companion object {
        const val DEFAULT_WHATSAPP_NUMBER = "8801537214123"
        const val OFFICE_MFS_NUMBER = "01537214123"

        fun hashPassword(rawPassword: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(rawPassword.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    private object Keys {
        val CART_QUANTITIES = stringPreferencesKey("cart_quantities")
        val WHATSAPP_NUMBER = stringPreferencesKey("whatsapp_number")
        val CUSTOMER_NAME = stringPreferencesKey("customer_name")
        val CUSTOMER_PHONE = stringPreferencesKey("customer_phone")
        val CUSTOMER_ADDRESS = stringPreferencesKey("customer_address")
        val PAYMENT_METHOD = stringPreferencesKey("payment_method")
        val REGISTERED_ACCOUNTS = stringPreferencesKey("registered_accounts_json")
        val ACTIVE_USER_PHONE = stringPreferencesKey("active_user_phone")
        val ORDER_HISTORY = stringPreferencesKey("order_history_json")
    }

    val products: List<Product> = listOf(
        Product(
            id = 1,
            name = "Wireless Bluetooth Earbuds",
            category = ProductCategory.AUDIO,
            price = 950,
            originalPrice = 1250,
            discountPercent = 24,
            rating = 4.8,
            reviewCount = 142,
            soldLabel = "620+ sold",
            isFlashDeal = true,
            tag = "Popular",
            description = "Enjoy clear sound and convenient wireless listening.",
            highlights = listOf("Bluetooth 5.3 low-latency pairing", "Compact pocket charging case", "Crisp vocal clarity for calls & music"),
            visual = ProductVisual.Photo(R.drawable.img_prod_earbuds)
        ),
        Product(
            id = 2,
            name = "Bluetooth Headphone",
            category = ProductCategory.AUDIO,
            price = 1550,
            originalPrice = 1950,
            discountPercent = 21,
            rating = 4.9,
            reviewCount = 96,
            soldLabel = "340+ sold",
            isFlashDeal = true,
            tag = "Featured",
            description = "Comfortable design for music, calls and everyday use.",
            highlights = listOf("Soft memory-foam ear cushions", "Deep bass & balanced stereo drivers", "Foldable travel-ready headband"),
            visual = ProductVisual.StudioIllustration(IllustrationType.HEADPHONES)
        ),
        Product(
            id = 3,
            name = "Mini DC UPS",
            category = ProductCategory.SMART_GADGETS,
            price = 1200,
            originalPrice = 1450,
            discountPercent = 17,
            rating = 4.8,
            reviewCount = 210,
            soldLabel = "890+ sold",
            isFlashDeal = true,
            tag = "Useful",
            description = "A compact power backup solution for compatible devices.",
            highlights = listOf("Keeps Wi-Fi routers online during outages", "Multi-voltage DC output protection", "Smart LED battery level indicator"),
            visual = ProductVisual.StudioIllustration(IllustrationType.MINI_UPS)
        ),
        Product(
            id = 4,
            name = "Mobile Phone Stand",
            category = ProductCategory.MOBILE_ACCESSORIES,
            price = 1220,
            originalPrice = 1490,
            discountPercent = 18,
            rating = 4.7,
            reviewCount = 68,
            soldLabel = "250+ sold",
            isFlashDeal = false,
            tag = "Trending",
            description = "A handy stand for watching videos and video calls.",
            highlights = listOf("Dual-hinge angle & height adjustment", "Anti-slip silicone cradle pads", "Sturdy weighted aluminum base"),
            visual = ProductVisual.StudioIllustration(IllustrationType.PHONE_STAND)
        ),
        Product(
            id = 5,
            name = "Fast Charging Cable",
            category = ProductCategory.ACCESSORIES,
            price = 250,
            originalPrice = 350,
            discountPercent = 29,
            rating = 4.9,
            reviewCount = 315,
            soldLabel = "1.4k sold",
            isFlashDeal = true,
            tag = "Everyday",
            description = "A practical charging accessory for daily use.",
            highlights = listOf("Reinforced braided nylon jacket", "High-speed power delivery & data sync", "Tangle-free everyday durability"),
            visual = ProductVisual.StudioIllustration(IllustrationType.CHARGING_CABLE)
        ),
        Product(
            id = 6,
            name = "Smart Watch",
            category = ProductCategory.SMART_GADGETS,
            price = 1850,
            originalPrice = 2350,
            discountPercent = 21,
            rating = 4.8,
            reviewCount = 124,
            soldLabel = "480+ sold",
            isFlashDeal = true,
            tag = "New",
            description = "A smart everyday accessory with useful features.",
            highlights = listOf("Vibrant full-touch display", "Activity, step & heart-rate tracking", "Instant call & message notifications"),
            visual = ProductVisual.Photo(R.drawable.img_prod_smartwatch)
        ),
        Product(
            id = 7,
            name = "Portable Bluetooth Speaker",
            category = ProductCategory.AUDIO,
            price = 1350,
            originalPrice = 1650,
            discountPercent = 18,
            rating = 4.7,
            reviewCount = 87,
            soldLabel = "310+ sold",
            isFlashDeal = false,
            tag = "Popular",
            description = "Bring your favourite audio with you on the go.",
            highlights = listOf("360° room-filling wireless sound", "Compact cylindrical travel body", "Built-in rechargeable long-play battery"),
            visual = ProductVisual.Photo(R.drawable.img_prod_speaker)
        ),
        Product(
            id = 8,
            name = "Mobile Accessories Combo",
            category = ProductCategory.MOBILE_ACCESSORIES,
            price = 650,
            originalPrice = 890,
            discountPercent = 27,
            rating = 4.8,
            reviewCount = 154,
            soldLabel = "530+ sold",
            isFlashDeal = false,
            tag = "Value Pack",
            description = "Useful accessories to complement your smartphone.",
            highlights = listOf("Curated everyday mobile essentials", "Great value bundle pricing", "Universal smartphone compatibility"),
            visual = ProductVisual.StudioIllustration(IllustrationType.ACCESSORIES_COMBO)
        )
    )

    val preferencesFlow: Flow<PersistedStorePrefs> = context.dataStore.data.map { prefs ->
        val storedWhatsApp = prefs[Keys.WHATSAPP_NUMBER]
        val paymentName = prefs[Keys.PAYMENT_METHOD]
        val paymentMethod = PaymentMethod.entries.find { it.name == paymentName }
            ?: PaymentMethod.CASH_ON_DELIVERY

        PersistedStorePrefs(
            cartQuantities = decodeCart(prefs[Keys.CART_QUANTITIES].orEmpty()),
            whatsappNumber = if (storedWhatsApp.isNullOrBlank()) DEFAULT_WHATSAPP_NUMBER else storedWhatsApp,
            customerName = prefs[Keys.CUSTOMER_NAME].orEmpty(),
            customerPhone = prefs[Keys.CUSTOMER_PHONE].orEmpty(),
            customerAddress = prefs[Keys.CUSTOMER_ADDRESS].orEmpty(),
            selectedPaymentMethod = paymentMethod,
            registeredAccounts = decodeAccounts(prefs[Keys.REGISTERED_ACCOUNTS].orEmpty()),
            activeUserPhone = prefs[Keys.ACTIVE_USER_PHONE].orEmpty(),
            orderHistory = decodeOrders(prefs[Keys.ORDER_HISTORY].orEmpty())
        )
    }

    suspend fun saveCartQuantities(cart: Map<Int, Int>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CART_QUANTITIES] = encodeCart(cart)
        }
    }

    suspend fun saveWhatsAppNumber(rawNumber: String) {
        val digits = rawNumber.filter { it.isDigit() }
        val normalized = if (digits.length == 11 && digits.startsWith("01")) {
            "88$digits"
        } else {
            digits
        }
        context.dataStore.edit { prefs ->
            prefs[Keys.WHATSAPP_NUMBER] = normalized
        }
    }

    suspend fun saveCustomerDetails(name: String, phone: String, address: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CUSTOMER_NAME] = name
            prefs[Keys.CUSTOMER_PHONE] = phone
            prefs[Keys.CUSTOMER_ADDRESS] = address
        }
    }

    suspend fun savePaymentMethod(method: PaymentMethod) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PAYMENT_METHOD] = method.name
        }
    }

    suspend fun saveAccountsAndActiveUser(accounts: List<LocalUserAccount>, activePhone: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.REGISTERED_ACCOUNTS] = encodeAccounts(accounts)
            prefs[Keys.ACTIVE_USER_PHONE] = activePhone
        }
    }

    suspend fun setActiveUserPhone(activePhone: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_USER_PHONE] = activePhone
        }
    }

    suspend fun saveOrderHistory(orders: List<OrderRecord>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ORDER_HISTORY] = encodeOrders(orders.take(30))
        }
    }

    private fun encodeCart(cart: Map<Int, Int>): String {
        return cart.entries
            .filter { it.value > 0 }
            .joinToString(",") { "${it.key}:${it.value}" }
    }

    private fun decodeCart(encoded: String): Map<Int, Int> {
        if (encoded.isBlank()) return emptyMap()
        return encoded.split(",")
            .mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    val id = parts[0].toIntOrNull()
                    val qty = parts[1].toIntOrNull()
                    if (id != null && qty != null && qty > 0) id to qty else null
                } else null
            }
            .toMap()
    }

    private fun encodeAccounts(accounts: List<LocalUserAccount>): String {
        val arr = JSONArray()
        accounts.forEach { acc ->
            arr.put(
                JSONObject().apply {
                    put("name", acc.name)
                    put("phone", acc.phone)
                    put("address", acc.address)
                    put("passwordHash", acc.passwordHash)
                }
            )
        }
        return arr.toString()
    }

    private fun decodeAccounts(json: String): List<LocalUserAccount> {
        if (json.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        LocalUserAccount(
                            name = obj.optString("name"),
                            phone = obj.optString("phone"),
                            address = obj.optString("address"),
                            passwordHash = obj.optString("passwordHash")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun encodeOrders(orders: List<OrderRecord>): String {
        val arr = JSONArray()
        orders.forEach { order ->
            arr.put(
                JSONObject().apply {
                    put("id", order.id)
                    put("timestampMillis", order.timestampMillis)
                    put("itemsSummary", order.itemsSummary)
                    put("totalAmount", order.totalAmount)
                    put("paymentMethod", order.paymentMethod.name)
                    put("paymentReference", order.paymentReference)
                    put("customerName", order.customerName)
                    put("customerPhone", order.customerPhone)
                    put("customerAddress", order.customerAddress)
                }
            )
        }
        return arr.toString()
    }

    private fun decodeOrders(json: String): List<OrderRecord> {
        if (json.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val methodName = obj.optString("paymentMethod")
                    val method = PaymentMethod.entries.find { it.name == methodName }
                        ?: PaymentMethod.CASH_ON_DELIVERY
                    add(
                        OrderRecord(
                            id = obj.optString("id"),
                            timestampMillis = obj.optLong("timestampMillis"),
                            itemsSummary = obj.optString("itemsSummary"),
                            totalAmount = obj.optInt("totalAmount"),
                            paymentMethod = method,
                            paymentReference = obj.optString("paymentReference"),
                            customerName = obj.optString("customerName"),
                            customerPhone = obj.optString("customerPhone"),
                            customerAddress = obj.optString("customerAddress")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }
}
