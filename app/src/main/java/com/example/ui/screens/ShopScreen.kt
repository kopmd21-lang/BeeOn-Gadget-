package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Cable
import androidx.compose.material.icons.outlined.CreditScore
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CurrencyFormatter
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.ui.BeeOnUiState
import com.example.ui.components.ProductArtwork
import com.example.ui.theme.BengaliFontFamily
import com.example.ui.theme.DarazOrange
import com.example.ui.theme.DarazOrangeBright
import com.example.ui.theme.DarazOrangeSoft
import com.example.ui.theme.DeepInk
import com.example.ui.theme.DiscountCoral
import com.example.ui.theme.DiscountSoft
import com.example.ui.theme.DisplayFontFamily
import com.example.ui.theme.ElectricHoney
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftSlateText
import com.example.ui.theme.StarAmber
import com.example.ui.theme.WhatsAppEmerald
import kotlinx.coroutines.launch

private data class PromoBannerSlide(
    val badge: String,
    val title: String,
    val subtitleBn: String,
    val accentColor: Color,
    val imageResId: Int? = null,
    val targetCategory: ProductCategory
)

@Composable
fun ShopScreen(
    uiState: BeeOnUiState,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (ProductCategory) -> Unit,
    onAddToCart: (Int, Int) -> Unit,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val flashDeals = uiState.allProducts.filter { it.isFlashDeal }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val columns = when {
            maxWidth >= 900.dp -> 4
            maxWidth >= 600.dp -> 3
            else -> 2
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            state = gridState,
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Daraz-style Promotional Carousel Banner
            item(span = { GridItemSpan(maxLineSpan) }) {
                DarazPromoCarousel(
                    onSlideAction = { category ->
                        onSelectCategory(category)
                        coroutineScope.launch {
                            gridState.animateScrollToItem(4)
                        }
                    },
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            // 2. Daraz Quick Category Icon Strip
            item(span = { GridItemSpan(maxLineSpan) }) {
                QuickCategoryIconStrip(
                    selectedCategory = uiState.selectedCategory,
                    onSelectCategory = { cat ->
                        onSelectCategory(cat)
                        coroutineScope.launch {
                            gridState.animateScrollToItem(4)
                        }
                    }
                )
            }

            // 3. Trust / Service Guarantee Strip
            item(span = { GridItemSpan(maxLineSpan) }) {
                MarketplaceTrustStrip(
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            // 4. Flash Deals Horizontal Shelf (hide while actively searching)
            if (uiState.searchQuery.isBlank() && flashDeals.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FlashDealsSection(
                        deals = flashDeals,
                        onProductClick = onProductClick,
                        onSeeAll = {
                            onSelectCategory(ProductCategory.ALL)
                            coroutineScope.launch {
                                gridState.animateScrollToItem(4)
                            }
                        }
                    )
                }
            }

            // 5. "Just For You" Marketplace Section Header & Filter Chips
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(top = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Just For You",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Find the right gadget for your everyday needs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = DarazOrangeSoft,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${uiState.filteredProducts.size} items",
                                color = DarazOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ProductCategory.entries) { category ->
                            val selected = uiState.selectedCategory == category
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectCategory(category) },
                                label = {
                                    Text(
                                        text = category.displayName,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    selectedContainerColor = DarazOrange,
                                    selectedLabelColor = PureWhite
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = MaterialTheme.colorScheme.outline,
                                    selectedBorderColor = DarazOrange
                                ),
                                modifier = Modifier
                                    .heightIn(min = 38.dp)
                                    .testTag("filter_${category.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // 6. Marketplace 2-Column Product Grid
            if (uiState.filteredProducts.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptySearchCard(
                        onResetFilters = {
                            onSearchQueryChange("")
                            onSelectCategory(ProductCategory.ALL)
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            } else {
                items(
                    items = uiState.filteredProducts,
                    key = { it.id }
                ) { product ->
                    val indexInFiltered = uiState.filteredProducts.indexOf(product)
                    val col = indexInFiltered % columns
                    val startPad = if (col == 0) 12.dp else 0.dp
                    val endPad = if (col == columns - 1) 12.dp else 0.dp
                    val qtyInCart = uiState.cartQuantities[product.id] ?: 0

                    DarazProductCard(
                        product = product,
                        quantityInCart = qtyInCart,
                        onAdd = { onAddToCart(product.id, 1) },
                        onDecrement = { onAddToCart(product.id, -1) },
                        onClick = { onProductClick(product) },
                        modifier = Modifier.padding(start = startPad, end = endPad)
                    )
                }
            }
        }
    }
}

@Composable
private fun DarazPromoCarousel(
    onSlideAction: (ProductCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val slides = listOf(
        PromoBannerSlide(
            badge = "BEEON MEGA DEALS",
            title = "Upgrade Your Life\nWith Smart Gadgets",
            subtitleBn = "স্মার্ট গ্যাজেট ও মোবাইল এক্সেসরিজে সেরা অফার!",
            accentColor = ElectricHoney,
            imageResId = R.drawable.img_hero_banner,
            targetCategory = ProductCategory.ALL
        ),
        PromoBannerSlide(
            badge = "AUDIO COLLECTION",
            title = "Wireless Earbuds &\nSpeakers Up to 24% Off",
            subtitleBn = "ক্রিস্টাল ক্লিয়ার সাউন্ড এখন সাশ্রয়ী দামে।",
            accentColor = ElectricHoney,
            imageResId = R.drawable.img_prod_earbuds,
            targetCategory = ProductCategory.AUDIO
        ),
        PromoBannerSlide(
            badge = "NATIONWIDE DELIVERY",
            title = "Easy WhatsApp Order\nAcross Bangladesh",
            subtitleBn = "সারা বাংলাদেশে দ্রুত হোম ডেলিভারি সুবিধা।",
            accentColor = WhatsAppEmerald,
            imageResId = R.drawable.img_prod_smartwatch,
            targetCategory = ProductCategory.SMART_GADGETS
        )
    )

    val pagerState = rememberPagerState(pageCount = { slides.size })

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 12.dp),
            pageSpacing = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val slide = slides[page]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(MidnightNavy, Color(0xFF1F2D46), DeepInk)
                        )
                    )
                    .clickable { onSlideAction(slide.targetCategory) }
            ) {
                if (slide.imageResId != null) {
                    Image(
                        painter = painterResource(id = slide.imageResId),
                        contentDescription = slide.title,
                        contentScale = ContentScale.Crop,
                        alpha = 0.42f,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MidnightNavy.copy(alpha = 0.92f),
                                    MidnightNavy.copy(alpha = 0.65f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = slide.accentColor,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = slide.badge,
                            color = if (slide.accentColor == ElectricHoney) MidnightNavy else PureWhite,
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Column {
                        Text(
                            text = slide.title,
                            color = PureWhite,
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = slide.subtitleBn,
                            color = SoftSlateText,
                            fontFamily = BengaliFontFamily,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        color = DarazOrange,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Shop Now",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Page indicator dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(slides.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(6.dp)
                        .width(if (isSelected) 18.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) DarazOrange else Color(0xFFCBD5E1))
                )
            }
        }
    }
}

@Composable
private fun QuickCategoryIconStrip(
    selectedCategory: ProductCategory,
    onSelectCategory: (ProductCategory) -> Unit
) {
    val items = listOf(
        Triple(ProductCategory.ALL, Icons.Outlined.Apps, Color(0xFFFFEFE6) to DarazOrange),
        Triple(ProductCategory.AUDIO, Icons.Outlined.Headphones, Color(0xFFEAF3FF) to Color(0xFF1D4ED8)),
        Triple(ProductCategory.MOBILE_ACCESSORIES, Icons.Outlined.PhoneAndroid, Color(0xFFE6F8F1) to WhatsAppEmerald),
        Triple(ProductCategory.SMART_GADGETS, Icons.Outlined.Watch, Color(0xFFFFF6DC) to Color(0xFFB45309)),
        Triple(ProductCategory.ACCESSORIES, Icons.Outlined.Cable, Color(0xFFF3EBFF) to Color(0xFF6D28D9))
    )

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { (category, icon, colors) ->
                val isSelected = selectedCategory == category
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectCategory(category) }
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) DarazOrange else colors.first,
                        border = if (isSelected) BorderStroke(2.dp, ElectricHoney) else null,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = category.displayName,
                                tint = if (isSelected) PureWhite else colors.second,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = category.shortLabel,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) DarazOrange else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun MarketplaceTrustStrip(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrustPill(Icons.Outlined.LocalShipping, "সারা দেশে ডেলিভারি")
            TrustPill(Icons.Outlined.VerifiedUser, "100% Support")
            TrustPill(Icons.Outlined.CreditScore, "Easy Order")
        }
    }
}

@Composable
private fun TrustPill(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DarazOrange,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontFamily = BengaliFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FlashDealsSection(
    deals: List<Product>,
    onProductClick: (Product) -> Unit,
    onSeeAll: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = DarazOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Flash Deals",
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = DiscountSoft,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "HOT OFFERS",
                            color = DiscountCoral,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "Shop More >",
                    color = DarazOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onSeeAll)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(deals, key = { "flash_${it.id}" }) { product ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .width(126.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onProductClick(product) }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(98.dp)
                            ) {
                                ProductArtwork(
                                    visual = product.visual,
                                    contentDescription = product.name,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Surface(
                                    color = DiscountCoral,
                                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                                    modifier = Modifier.align(Alignment.TopStart)
                                ) {
                                    Text(
                                        text = "-${product.discountPercent}%",
                                        color = PureWhite,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = CurrencyFormatter.formatBdt(product.price),
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = DarazOrange
                                )
                                Text(
                                    text = CurrencyFormatter.formatBdt(product.originalPrice),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textDecoration = TextDecoration.LineThrough
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { 0.72f },
                                    color = DarazOrange,
                                    trackColor = DarazOrangeSoft,
                                    strokeCap = StrokeCap.Round,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = product.soldLabel,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DarazProductCard(
    product: Product,
    quantityInCart: Int,
    onAdd: () -> Unit,
    onDecrement: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
            ) {
                ProductArtwork(
                    visual = product.visual,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize()
                )
                // Discount badge (top-start)
                Surface(
                    color = DiscountCoral,
                    shape = RoundedCornerShape(bottomEnd = 10.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "-${product.discountPercent}%",
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                // Tag badge (bottom-start)
                Surface(
                    color = MidnightNavy.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = product.tag,
                        color = ElectricHoney,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = CurrencyFormatter.formatBdt(product.price),
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = DarazOrange
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = CurrencyFormatter.formatBdt(product.originalPrice),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = StarAmber,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${product.rating} (${product.reviewCount}) • ${product.soldLabel}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (quantityInCart == 0) {
                    Button(
                        onClick = onAdd,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarazOrange,
                            contentColor = PureWhite
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("add_to_cart_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.add_to_cart),
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                } else {
                    Surface(
                        color = DarazOrangeSoft,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, DarazOrange.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = onDecrement,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("dec_product_${product.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Remove,
                                    contentDescription = "Decrease quantity",
                                    tint = DarazOrange
                                )
                            }
                            Text(
                                text = "$quantityInCart in cart",
                                fontFamily = DisplayFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = DarazOrange
                            )
                            IconButton(
                                onClick = onAdd,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("inc_product_${product.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = "Increase quantity",
                                    tint = DarazOrange
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySearchCard(
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.empty_search_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onResetFilters,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarazOrange,
                    contentColor = PureWhite
                ),
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Text("Show All Products", fontWeight = FontWeight.Bold)
            }
        }
    }
}
