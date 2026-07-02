package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MareViewModel

@Composable
fun SavedTab(
    viewModel: MareViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isThemeDark.collectAsState()
    val savedArticles by viewModel.savedArticles.collectAsState()
    val savedProducts by viewModel.savedProducts.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val weatherState by viewModel.weather.collectAsState()

    val currencyConfig = remember(userProfile?.location, weatherState) {
        getCurrencyConfig(userProfile?.location, weatherState)
    }

    val resolvedLocationName = remember(userProfile?.location, weatherState) {
        val city = weatherState?.city ?: userProfile?.location ?: "your location"
        val country = weatherState?.country ?: ""
        if (country.isNotEmpty() && !city.lowercase().contains(country.lowercase())) {
            "$city, $country"
        } else {
            city
        }
    }

    val resolvedCountryName = remember(userProfile?.location, weatherState) {
        val loc = (userProfile?.location ?: "London").lowercase()
        val country = (weatherState?.country ?: "").lowercase()
        val countryCode = (weatherState?.countryCode ?: "").lowercase()
        when {
            loc.contains("india") || loc.contains("mumbai") || loc.contains("delhi") || loc.contains("inr") || loc.contains("dibrugarh") || loc.contains("assam") || country.contains("india") -> "India"
            loc.contains("london") || loc.contains("uk") || loc.contains("united kingdom") || loc.contains("gbp") || country.contains("united kingdom") || countryCode == "gb" -> "UK"
            loc.contains("tokyo") || loc.contains("japan") || loc.contains("jpy") || country.contains("japan") || countryCode == "jp" -> "Japan"
            loc.contains("paris") || loc.contains("milan") || loc.contains("europe") || loc.contains("france") || loc.contains("italy") || loc.contains("spain") || loc.contains("germany") || loc.contains("eur") || country.contains("france") || countryCode == "fr" -> "Europe"
            else -> "USA"
        }
    }

    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBgColor = MaterialTheme.colorScheme.surface
    val cardBorderColor = MaterialTheme.colorScheme.outlineVariant

    var activeSubTab by remember { mutableStateOf("Articles") } // "Articles" or "Products"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Section
        Text(
            text = "Your Bookmarked Vault",
            fontSize = 24.sp,
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Serif,
            color = primaryTextColor,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Your synchronized collection of luxury articles, silhouettes, and price grids.",
            fontSize = 11.sp,
            color = secondaryTextColor,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Sub-tabs Selection Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SubTabSelectionChip(
                text = "Articles (${savedArticles.size})",
                isSelected = activeSubTab == "Articles",
                icon = Icons.Default.MenuBook,
                isDark = isDark
            ) {
                activeSubTab = "Articles"
            }

            SubTabSelectionChip(
                text = "Products (${savedProducts.size})",
                isSelected = activeSubTab == "Products",
                icon = Icons.Default.GridView,
                isDark = isDark
            ) {
                activeSubTab = "Products"
            }
        }

        // Contents
        if (activeSubTab == "Articles") {
            if (savedArticles.isEmpty()) {
                EmptySavedState("No saved articles found. Save styling writeups inside Articles tab.", secondaryTextColor)
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(savedArticles) { article ->
                        ArticleFeedCard(
                            article = article,
                            isSaved = true,
                            cardBg = cardBgColor,
                            borderColor = cardBorderColor,
                            primaryText = primaryTextColor,
                            secondaryText = secondaryTextColor,
                            onBookmarkClick = { viewModel.toggleArticleBookmark(article) }
                        )
                    }
                }
            }
        } else {
            if (savedProducts.isEmpty()) {
                EmptySavedState("No saved products found. Save luxury products inside Collection tab.", secondaryTextColor)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(savedProducts) { product ->
                        ProductGridCard(
                            product = product,
                            isSaved = true,
                            cardBg = cardBgColor,
                            borderColor = cardBorderColor,
                            primaryText = primaryTextColor,
                            secondaryText = secondaryTextColor,
                            currencyConfig = currencyConfig,
                            resolvedLocationName = resolvedLocationName,
                            resolvedCountryName = resolvedCountryName,
                            onBookmarkClick = { viewModel.toggleProductBookmark(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubTabSelectionChip(
    text: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) {
                if (isDark) Color(0xFF202D29) else Color(0xFFE2EBE6)
            } else {
                if (isDark) Color(0xFF2D3135) else Color(0xFFFFFFFF)
            },
            contentColor = if (isSelected) {
                if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29)
            } else {
                if (isDark) Color(0xFFC4C7C5) else Color(0xFF5A6E6E)
            }
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun EmptySavedState(text: String, color: Color) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = null,
                tint = color.copy(alpha = 0.3f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = text,
                fontSize = 13.sp,
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}
