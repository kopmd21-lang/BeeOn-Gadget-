package com.example

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.StoreRepository
import com.example.ui.BeeOnViewModel
import com.example.ui.PendingWhatsAppAction
import com.example.ui.components.AuthBottomSheet
import com.example.ui.components.BeeOnBottomBar
import com.example.ui.components.BeeOnDestination
import com.example.ui.components.BeeOnMarketplaceHeader
import com.example.ui.components.BeeOnNavRail
import com.example.ui.components.ProductDetailBottomSheet
import com.example.ui.components.ROUTE_ACCOUNT
import com.example.ui.components.ROUTE_CART
import com.example.ui.components.ROUTE_CATEGORIES
import com.example.ui.components.ROUTE_HOME
import com.example.ui.components.WhatsAppSetupDialog
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.theme.BeeOnGadgetTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BeeOnGadgetTheme {
                BeeOnApp()
            }
        }
    }
}

@Composable
fun BeeOnApp(
    viewModel: BeeOnViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun copyToClipboard(label: String, text: String, snackbarMessage: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(snackbarMessage)
        }
    }

    fun launchWhatsApp(message: String) {
        val uri = viewModel.buildWhatsAppUri(message)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            copyToClipboard(
                label = "BeeOn Gadget Order",
                text = message,
                snackbarMessage = "WhatsApp/Browser unavailable. Message copied to clipboard."
            )
        }
    }

    fun handleWhatsAppAction(action: PendingWhatsAppAction) {
        if (!uiState.hasConfiguredWhatsAppNumber) {
            viewModel.openWhatsAppSetup(action)
            return
        }
        val msg = when (action) {
            is PendingWhatsAppAction.CheckoutCart -> {
                viewModel.recordCartOrder()
                viewModel.buildCartOrderMessage()
            }
            is PendingWhatsAppAction.QuickOrderProduct -> {
                viewModel.recordSingleProductOrder(action.product)
                viewModel.buildSingleProductOrderMessage(action.product)
            }
            is PendingWhatsAppAction.GeneralInquiry -> viewModel.buildGeneralContactMessage()
        }
        launchWhatsApp(msg)
    }

    LaunchedEffect(uiState.whatsappNumber, uiState.isWhatsAppSetupOpen, uiState.pendingWhatsAppAction) {
        val pending = uiState.pendingWhatsAppAction
        if (!uiState.isWhatsAppSetupOpen && pending != null && uiState.hasConfiguredWhatsAppNumber) {
            val msg = when (pending) {
                is PendingWhatsAppAction.CheckoutCart -> {
                    viewModel.recordCartOrder()
                    viewModel.buildCartOrderMessage()
                }
                is PendingWhatsAppAction.QuickOrderProduct -> {
                    viewModel.recordSingleProductOrder(pending.product)
                    viewModel.buildSingleProductOrderMessage(pending.product)
                }
                is PendingWhatsAppAction.GeneralInquiry -> viewModel.buildGeneralContactMessage()
            }
            viewModel.clearPendingWhatsAppAction()
            launchWhatsApp(msg)
        }
    }

    fun navigateTo(destination: BeeOnDestination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val showHeaderSearch = currentRoute == ROUTE_HOME || currentRoute == ROUTE_CATEGORIES

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                BeeOnMarketplaceHeader(
                    searchQuery = uiState.searchQuery,
                    showSearchBar = showHeaderSearch,
                    cartCount = uiState.cartCount,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onSearchSubmitOrFocus = {
                        if (currentRoute != ROUTE_HOME && currentRoute != ROUTE_CATEGORIES) {
                            navigateTo(BeeOnDestination.HOME)
                        }
                    },
                    onOpenCart = { navigateTo(BeeOnDestination.CART) },
                    onOpenWhatsAppSetup = { viewModel.openWhatsAppSetup(null) }
                )
            },
            bottomBar = {
                if (!useNavRail) {
                    BeeOnBottomBar(
                        currentRoute = currentRoute,
                        cartCount = uiState.cartCount,
                        onNavigate = ::navigateTo
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    BeeOnNavRail(
                        currentRoute = currentRoute,
                        cartCount = uiState.cartCount,
                        onNavigate = ::navigateTo
                    )
                }

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_HOME,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(ROUTE_HOME) {
                        ShopScreen(
                            uiState = uiState,
                            onSearchQueryChange = viewModel::updateSearchQuery,
                            onSelectCategory = viewModel::selectCategory,
                            onAddToCart = { id, delta ->
                                viewModel.addToCart(id, delta)
                                if (delta > 0) {
                                    val prodName = uiState.allProducts.find { it.id == id }?.name ?: "Item"
                                    scope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        snackbarHostState.showSnackbar("Added $prodName to cart")
                                    }
                                }
                            },
                            onProductClick = viewModel::openProductDetail
                        )
                    }
                    composable(ROUTE_CATEGORIES) {
                        CategoriesScreen(
                            uiState = uiState,
                            onSelectCategory = viewModel::selectCategory,
                            onAddToCart = { id, delta ->
                                viewModel.addToCart(id, delta)
                                if (delta > 0) {
                                    val prodName = uiState.allProducts.find { it.id == id }?.name ?: "Item"
                                    scope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        snackbarHostState.showSnackbar("Added $prodName to cart")
                                    }
                                }
                            },
                            onProductClick = viewModel::openProductDetail
                        )
                    }
                    composable(ROUTE_CART) {
                        CartScreen(
                            uiState = uiState,
                            onChangeQty = viewModel::addToCart,
                            onRemoveItem = viewModel::removeFromCart,
                            onClearCart = viewModel::clearCart,
                            onCustomerNameChange = viewModel::updateCustomerName,
                            onCustomerPhoneChange = viewModel::updateCustomerPhone,
                            onCustomerAddressChange = viewModel::updateCustomerAddress,
                            onSelectPaymentMethod = viewModel::selectPaymentMethod,
                            onMfsSenderChange = viewModel::updateMfsSenderNumber,
                            onMfsTrxIdChange = viewModel::updateMfsTransactionId,
                            onCopyMfsNumber = {
                                val numberToCopy = uiState.selectedPaymentMethod.accountNumber
                                    ?: StoreRepository.OFFICE_MFS_NUMBER
                                copyToClipboard(
                                    label = "${uiState.selectedPaymentMethod.title} Send Money Number",
                                    text = numberToCopy,
                                    snackbarMessage = "Copied ${uiState.selectedPaymentMethod.title} number: $numberToCopy"
                                )
                            },
                            onOpenMfsApp = { method ->
                                val numberToCopy = method.accountNumber ?: StoreRepository.OFFICE_MFS_NUMBER
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("${method.title} Number", numberToCopy))

                                val pkg = method.appPackageName
                                val launchIntent = pkg?.let { context.packageManager.getLaunchIntentForPackage(it) }
                                if (launchIntent != null) {
                                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(launchIntent)
                                    scope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        snackbarHostState.showSnackbar("Copied $numberToCopy & opening ${method.title}")
                                    }
                                } else {
                                    val ussd = method.ussdCode
                                    if (!ussd.isNullOrBlank()) {
                                        val dialIntent = Intent(
                                            Intent.ACTION_DIAL,
                                            Uri.parse("tel:${Uri.encode(ussd)}")
                                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                        try {
                                            context.startActivity(dialIntent)
                                            scope.launch {
                                                snackbarHostState.currentSnackbarData?.dismiss()
                                                snackbarHostState.showSnackbar("Copied $numberToCopy — opening $ussd dialer")
                                            }
                                        } catch (_: ActivityNotFoundException) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Copied ${method.title} number: $numberToCopy")
                                            }
                                        }
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Copied ${method.title} number: $numberToCopy")
                                        }
                                    }
                                }
                            },
                            onDialUssd = { method ->
                                val numberToCopy = method.accountNumber ?: StoreRepository.OFFICE_MFS_NUMBER
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("${method.title} Number", numberToCopy))
                                val ussd = method.ussdCode ?: return@CartScreen
                                val dialIntent = Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.parse("tel:${Uri.encode(ussd)}")
                                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                try {
                                    context.startActivity(dialIntent)
                                    scope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        snackbarHostState.showSnackbar("Copied $numberToCopy — dial $ussd")
                                    }
                                } catch (_: ActivityNotFoundException) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Dial $ussd & Send Money to $numberToCopy")
                                    }
                                }
                            },
                            onOpenSignIn = { viewModel.openAuthDialog(signUpTab = false) },
                            onOrderViaWhatsApp = {
                                handleWhatsAppAction(PendingWhatsAppAction.CheckoutCart)
                            },
                            onCopyOrderSummary = {
                                copyToClipboard(
                                    label = "BeeOn Gadget Order",
                                    text = viewModel.buildCartOrderMessage(),
                                    snackbarMessage = "Order summary copied to clipboard"
                                )
                            },
                            onContinueShopping = { navigateTo(BeeOnDestination.HOME) }
                        )
                    }
                    composable(ROUTE_ACCOUNT) {
                        AccountScreen(
                            activeUser = uiState.activeUser,
                            orderHistory = uiState.orderHistory,
                            whatsappNumber = uiState.whatsappNumber,
                            cartCount = uiState.cartCount,
                            onOpenSignIn = { viewModel.openAuthDialog(signUpTab = false) },
                            onOpenSignUp = { viewModel.openAuthDialog(signUpTab = true) },
                            onSignOut = {
                                viewModel.signOut()
                                scope.launch {
                                    snackbarHostState.showSnackbar("Signed out")
                                }
                            },
                            onWhatsAppUs = { handleWhatsAppAction(PendingWhatsAppAction.GeneralInquiry) },
                            onSaveWhatsAppNumber = { number ->
                                viewModel.saveWhatsAppNumber(number)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Office WhatsApp number saved")
                                }
                            },
                            onCopyInquiry = {
                                copyToClipboard(
                                    label = "BeeOn Gadget Inquiry",
                                    text = viewModel.buildGeneralContactMessage(),
                                    snackbarMessage = "Inquiry message copied"
                                )
                            },
                            onOpenCart = { navigateTo(BeeOnDestination.CART) },
                            onBrowseShop = { navigateTo(BeeOnDestination.HOME) }
                        )
                    }
                }
            }
        }
    }

    uiState.selectedProduct?.let { product ->
        ProductDetailBottomSheet(
            product = product,
            quantityInCart = uiState.cartQuantities[product.id] ?: 0,
            onDismiss = viewModel::closeProductDetail,
            onAddToCart = {
                viewModel.addToCart(product.id, 1)
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar("Added ${product.name} to cart")
                }
            },
            onQuickOrderViaWhatsApp = {
                handleWhatsAppAction(PendingWhatsAppAction.QuickOrderProduct(product))
            },
            onAskWhatsApp = {
                handleWhatsAppAction(PendingWhatsAppAction.GeneralInquiry)
            }
        )
    }

    if (uiState.isAuthDialogOpen) {
        AuthBottomSheet(
            initialSignUp = uiState.authInitialSignUp,
            errorMessage = uiState.authErrorMessage,
            onDismiss = viewModel::closeAuthDialog,
            onClearError = viewModel::clearAuthError,
            onSignIn = { phone, password ->
                viewModel.signIn(phone, password)
            },
            onSignUp = { name, phone, address, password ->
                viewModel.signUp(name, phone, address, password)
            }
        )
    }

    if (uiState.isWhatsAppSetupOpen) {
        WhatsAppSetupDialog(
            currentNumber = uiState.whatsappNumber,
            onDismiss = viewModel::closeWhatsAppSetup,
            onSave = viewModel::saveWhatsAppNumber
        )
    }
}
