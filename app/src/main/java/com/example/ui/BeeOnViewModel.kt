package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.StoreRepository
import com.example.model.CartItem
import com.example.model.CurrencyFormatter
import com.example.model.LocalUserAccount
import com.example.model.OrderRecord
import com.example.model.PaymentMethod
import com.example.model.Product
import com.example.model.ProductCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BeeOnUiState(
    val allProducts: List<Product> = emptyList(),
    val selectedCategory: ProductCategory = ProductCategory.ALL,
    val searchQuery: String = "",
    val cartQuantities: Map<Int, Int> = emptyMap(),
    val whatsappNumber: String = StoreRepository.DEFAULT_WHATSAPP_NUMBER,
    val customerName: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CASH_ON_DELIVERY,
    val mfsSenderNumber: String = "",
    val mfsTransactionId: String = "",
    val registeredAccounts: List<LocalUserAccount> = emptyList(),
    val activeUserPhone: String = "",
    val orderHistory: List<OrderRecord> = emptyList(),
    val isAuthDialogOpen: Boolean = false,
    val authInitialSignUp: Boolean = false,
    val authErrorMessage: String? = null,
    val selectedProduct: Product? = null,
    val isCartOpen: Boolean = false,
    val isWhatsAppSetupOpen: Boolean = false,
    val pendingWhatsAppAction: PendingWhatsAppAction? = null
) {
    val filteredProducts: List<Product>
        get() {
            val q = searchQuery.trim().lowercase()
            return allProducts.filter { product ->
                val matchesCategory = selectedCategory == ProductCategory.ALL || product.category == selectedCategory
                val matchesQuery = q.isEmpty() ||
                    product.name.lowercase().contains(q) ||
                    product.description.lowercase().contains(q) ||
                    product.tag.lowercase().contains(q)
                matchesCategory && matchesQuery
            }
        }

    val cartItems: List<CartItem>
        get() = cartQuantities.mapNotNull { (id, qty) ->
            allProducts.find { it.id == id }?.let { product ->
                if (qty > 0) CartItem(product, qty) else null
            }
        }

    val cartCount: Int
        get() = cartQuantities.values.sum()

    val cartTotal: Int
        get() = cartItems.sumOf { it.subtotal }

    val hasConfiguredWhatsAppNumber: Boolean
        get() = whatsappNumber.length >= 8

    val activeUser: LocalUserAccount?
        get() = if (activeUserPhone.isBlank()) null else registeredAccounts.find { it.phone == activeUserPhone }

    val isSignedIn: Boolean
        get() = activeUser != null
}

sealed interface PendingWhatsAppAction {
    data object CheckoutCart : PendingWhatsAppAction
    data class QuickOrderProduct(val product: Product) : PendingWhatsAppAction
    data object GeneralInquiry : PendingWhatsAppAction
}

class BeeOnViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StoreRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(
        BeeOnUiState(
            allProducts = repository.products,
            whatsappNumber = StoreRepository.DEFAULT_WHATSAPP_NUMBER
        )
    )
    val uiState: StateFlow<BeeOnUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.preferencesFlow.collect { prefs ->
                _uiState.update { current ->
                    val activeAccount = prefs.registeredAccounts.find { it.phone == prefs.activeUserPhone }
                    current.copy(
                        cartQuantities = prefs.cartQuantities,
                        whatsappNumber = prefs.whatsappNumber,
                        customerName = prefs.customerName.ifBlank { activeAccount?.name.orEmpty() },
                        customerPhone = prefs.customerPhone.ifBlank { activeAccount?.phone.orEmpty() },
                        customerAddress = prefs.customerAddress.ifBlank { activeAccount?.address.orEmpty() },
                        selectedPaymentMethod = prefs.selectedPaymentMethod,
                        registeredAccounts = prefs.registeredAccounts,
                        activeUserPhone = prefs.activeUserPhone,
                        orderHistory = prefs.orderHistory
                    )
                }
            }
        }
    }

    fun selectCategory(category: ProductCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun addToCart(productId: Int, delta: Int = 1) {
        val currentMap = _uiState.value.cartQuantities.toMutableMap()
        val nextQty = (currentMap[productId] ?: 0) + delta
        if (nextQty <= 0) {
            currentMap.remove(productId)
        } else {
            currentMap[productId] = nextQty
        }
        _uiState.update { it.copy(cartQuantities = currentMap) }
        viewModelScope.launch { repository.saveCartQuantities(currentMap) }
    }

    fun removeFromCart(productId: Int) {
        val currentMap = _uiState.value.cartQuantities.toMutableMap()
        currentMap.remove(productId)
        _uiState.update { it.copy(cartQuantities = currentMap) }
        viewModelScope.launch { repository.saveCartQuantities(currentMap) }
    }

    fun clearCart() {
        _uiState.update { it.copy(cartQuantities = emptyMap()) }
        viewModelScope.launch { repository.saveCartQuantities(emptyMap()) }
    }

    fun openProductDetail(product: Product) {
        _uiState.update { it.copy(selectedProduct = product) }
    }

    fun closeProductDetail() {
        _uiState.update { it.copy(selectedProduct = null) }
    }

    fun updateCustomerName(name: String) {
        _uiState.update { it.copy(customerName = name) }
        viewModelScope.launch {
            val s = _uiState.value
            repository.saveCustomerDetails(name, s.customerPhone, s.customerAddress)
        }
    }

    fun updateCustomerPhone(phone: String) {
        val digits = phone.filter { it.isDigit() }
        _uiState.update { it.copy(customerPhone = digits) }
        viewModelScope.launch {
            val s = _uiState.value
            repository.saveCustomerDetails(s.customerName, digits, s.customerAddress)
        }
    }

    fun updateCustomerAddress(address: String) {
        _uiState.update { it.copy(customerAddress = address) }
        viewModelScope.launch {
            val s = _uiState.value
            repository.saveCustomerDetails(s.customerName, s.customerPhone, address)
        }
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
        viewModelScope.launch { repository.savePaymentMethod(method) }
    }

    fun updateMfsSenderNumber(number: String) {
        _uiState.update { it.copy(mfsSenderNumber = number.filter { ch -> ch.isDigit() }) }
    }

    fun updateMfsTransactionId(trxId: String) {
        _uiState.update { it.copy(mfsTransactionId = trxId.uppercase()) }
    }

    fun openAuthDialog(signUpTab: Boolean) {
        _uiState.update {
            it.copy(
                isAuthDialogOpen = true,
                authInitialSignUp = signUpTab,
                authErrorMessage = null
            )
        }
    }

    fun closeAuthDialog() {
        _uiState.update {
            it.copy(
                isAuthDialogOpen = false,
                authErrorMessage = null
            )
        }
    }

    fun clearAuthError() {
        _uiState.update { it.copy(authErrorMessage = null) }
    }

    fun signUp(name: String, phone: String, address: String, password: String) {
        val cleanName = name.trim()
        val cleanPhone = phone.filter { it.isDigit() }
        val cleanAddress = address.trim()

        if (cleanName.isEmpty()) {
            _uiState.update { it.copy(authErrorMessage = "Please enter your full name.") }
            return
        }
        if (cleanPhone.length < 10) {
            _uiState.update { it.copy(authErrorMessage = "Please enter a valid mobile number (e.g. 017XXXXXXXX).") }
            return
        }
        if (password.length < 4) {
            _uiState.update { it.copy(authErrorMessage = "Password must be at least 4 characters.") }
            return
        }
        val existing = _uiState.value.registeredAccounts.any { it.phone == cleanPhone }
        if (existing) {
            _uiState.update { it.copy(authErrorMessage = "An account with this number already exists. Please Sign In.") }
            return
        }

        val newAccount = LocalUserAccount(
            name = cleanName,
            phone = cleanPhone,
            address = cleanAddress,
            passwordHash = StoreRepository.hashPassword(password)
        )
        val updatedAccounts = _uiState.value.registeredAccounts + newAccount
        _uiState.update {
            it.copy(
                registeredAccounts = updatedAccounts,
                activeUserPhone = cleanPhone,
                customerName = cleanName,
                customerPhone = cleanPhone,
                customerAddress = cleanAddress.ifBlank { it.customerAddress },
                isAuthDialogOpen = false,
                authErrorMessage = null
            )
        }
        viewModelScope.launch {
            repository.saveAccountsAndActiveUser(updatedAccounts, cleanPhone)
            repository.saveCustomerDetails(cleanName, cleanPhone, cleanAddress.ifBlank { _uiState.value.customerAddress })
        }
    }

    fun signIn(phone: String, password: String) {
        val cleanPhone = phone.filter { it.isDigit() }
        if (cleanPhone.length < 10 || password.isEmpty()) {
            _uiState.update { it.copy(authErrorMessage = "Enter your mobile number and password.") }
            return
        }
        val account = _uiState.value.registeredAccounts.find { it.phone == cleanPhone }
        if (account == null) {
            _uiState.update { it.copy(authErrorMessage = "No account found with this number. Please Sign Up first.") }
            return
        }
        if (account.passwordHash != StoreRepository.hashPassword(password)) {
            _uiState.update { it.copy(authErrorMessage = "Incorrect password. Please try again.") }
            return
        }

        _uiState.update {
            it.copy(
                activeUserPhone = account.phone,
                customerName = account.name,
                customerPhone = account.phone,
                customerAddress = account.address.ifBlank { it.customerAddress },
                isAuthDialogOpen = false,
                authErrorMessage = null
            )
        }
        viewModelScope.launch {
            repository.setActiveUserPhone(account.phone)
            repository.saveCustomerDetails(account.name, account.phone, account.address.ifBlank { _uiState.value.customerAddress })
        }
    }

    fun signOut() {
        _uiState.update {
            it.copy(
                activeUserPhone = "",
                authErrorMessage = null
            )
        }
        viewModelScope.launch {
            repository.setActiveUserPhone("")
        }
    }

    fun recordCartOrder() {
        val s = _uiState.value
        if (s.cartItems.isEmpty()) return
        val itemsSummary = s.cartItems.joinToString(", ") { "${it.product.name} x${it.quantity}" }
        val ref = buildPaymentReference(s)
        val record = OrderRecord(
            id = "BG-${(System.currentTimeMillis() % 100000).toString().padStart(5, '0')}",
            timestampMillis = System.currentTimeMillis(),
            itemsSummary = itemsSummary,
            totalAmount = s.cartTotal,
            paymentMethod = s.selectedPaymentMethod,
            paymentReference = ref,
            customerName = s.customerName.trim(),
            customerPhone = s.customerPhone.trim(),
            customerAddress = s.customerAddress.trim()
        )
        val updated = listOf(record) + s.orderHistory
        _uiState.update { it.copy(orderHistory = updated) }
        viewModelScope.launch { repository.saveOrderHistory(updated) }
    }

    fun recordSingleProductOrder(product: Product) {
        val s = _uiState.value
        val ref = buildPaymentReference(s)
        val record = OrderRecord(
            id = "BG-${(System.currentTimeMillis() % 100000).toString().padStart(5, '0')}",
            timestampMillis = System.currentTimeMillis(),
            itemsSummary = "${product.name} x1",
            totalAmount = product.price,
            paymentMethod = s.selectedPaymentMethod,
            paymentReference = ref,
            customerName = s.customerName.trim(),
            customerPhone = s.customerPhone.trim(),
            customerAddress = s.customerAddress.trim()
        )
        val updated = listOf(record) + s.orderHistory
        _uiState.update { it.copy(orderHistory = updated) }
        viewModelScope.launch { repository.saveOrderHistory(updated) }
    }

    private fun buildPaymentReference(s: BeeOnUiState): String {
        if (!s.selectedPaymentMethod.requiresMfsDetails) return ""
        val parts = buildList {
            if (s.mfsSenderNumber.isNotBlank()) add("From: ${s.mfsSenderNumber}")
            if (s.mfsTransactionId.isNotBlank()) add("TrxID: ${s.mfsTransactionId}")
        }
        return parts.joinToString(" | ")
    }

    fun openWhatsAppSetup(pendingAction: PendingWhatsAppAction? = null) {
        _uiState.update {
            it.copy(
                isWhatsAppSetupOpen = true,
                pendingWhatsAppAction = pendingAction
            )
        }
    }

    fun closeWhatsAppSetup() {
        _uiState.update {
            it.copy(
                isWhatsAppSetupOpen = false,
                pendingWhatsAppAction = null
            )
        }
    }

    fun saveWhatsAppNumber(number: String) {
        val digits = number.filter { it.isDigit() }
        val normalized = if (digits.length == 11 && digits.startsWith("01")) {
            "88$digits"
        } else {
            digits
        }
        _uiState.update {
            it.copy(
                whatsappNumber = normalized,
                isWhatsAppSetupOpen = false
            )
        }
        viewModelScope.launch { repository.saveWhatsAppNumber(normalized) }
    }

    fun clearPendingWhatsAppAction() {
        _uiState.update { it.copy(pendingWhatsAppAction = null) }
    }

    fun buildCartOrderMessage(): String {
        val state = _uiState.value
        val sb = StringBuilder("Hello BeeOn Gadget! I want to order:\n")
        state.cartItems.forEach { item ->
            sb.append("- ${item.product.name} x ${item.quantity} = ${CurrencyFormatter.formatBdt(item.subtotal)}\n")
        }
        sb.append("\nTotal: ${CurrencyFormatter.formatBdt(state.cartTotal)}\n")
        val payLine = state.selectedPaymentMethod.accountNumber?.let { num ->
            "${state.selectedPaymentMethod.title} (Send Money: $num)"
        } ?: state.selectedPaymentMethod.title
        sb.append("Payment Method: $payLine\n")
        if (state.selectedPaymentMethod.requiresMfsDetails) {
            if (state.mfsSenderNumber.isNotBlank()) {
                sb.append("Sender Number: ${state.mfsSenderNumber}\n")
            }
            if (state.mfsTransactionId.isNotBlank()) {
                sb.append("TrxID: ${state.mfsTransactionId}\n")
            }
        }
        sb.append("Name: ${state.customerName.trim()}\n")
        sb.append("Phone: ${state.customerPhone.trim()}\n")
        sb.append("Address: ${state.customerAddress.trim()}\n")
        return sb.toString()
    }

    fun buildSingleProductOrderMessage(product: Product): String {
        val state = _uiState.value
        val sb = StringBuilder("Hello BeeOn Gadget! I want to order:\n")
        sb.append("- ${product.name} x 1 = ${CurrencyFormatter.formatBdt(product.price)}\n")
        sb.append("\nTotal: ${CurrencyFormatter.formatBdt(product.price)}\n")
        val payLine = state.selectedPaymentMethod.accountNumber?.let { num ->
            "${state.selectedPaymentMethod.title} (Send Money: $num)"
        } ?: state.selectedPaymentMethod.title
        sb.append("Payment Method: $payLine\n")
        sb.append("Name: ${state.customerName.trim()}\n")
        sb.append("Phone: ${state.customerPhone.trim()}\n")
        sb.append("Address: ${state.customerAddress.trim()}\n")
        return sb.toString()
    }

    fun buildGeneralContactMessage(): String {
        return "Hello BeeOn Gadget! I would like to know more about your products."
    }

    fun buildWhatsAppUri(message: String): Uri {
        val number = _uiState.value.whatsappNumber
        return Uri.parse("https://wa.me/$number?text=${Uri.encode(message)}")
    }
}
