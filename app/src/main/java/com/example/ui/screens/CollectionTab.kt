package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SavedProductEntity
import com.example.ui.viewmodel.MareViewModel
import com.example.ui.components.fadeInUpOnMount
import com.example.ui.components.hoverScaleAndShadow
import org.json.JSONArray
import org.json.JSONObject

data class CurrencyConfig(
    val symbol: String,
    val rate: Double,
    val code: String
)

fun getCurrencyConfig(location: String?, weatherState: com.example.ui.viewmodel.WeatherState? = null): CurrencyConfig {
    val loc = (location ?: "London").lowercase()
    val country = (weatherState?.country ?: "").lowercase()
    val countryCode = (weatherState?.countryCode ?: "").lowercase()
    val city = (weatherState?.city ?: "").lowercase()

    return when {
        loc.contains("london") || loc.contains("uk") || loc.contains("united kingdom") || loc.contains("gbp") ||
        country.contains("united kingdom") || country.contains("uk") || countryCode == "gb" || city.contains("london") -> {
            CurrencyConfig(symbol = "£", rate = 0.79, code = "GBP")
        }
        loc.contains("paris") || loc.contains("milan") || loc.contains("europe") || loc.contains("france") || loc.contains("italy") || loc.contains("spain") || loc.contains("germany") || loc.contains("eur") ||
        country.contains("france") || country.contains("italy") || country.contains("spain") || country.contains("germany") || country.contains("europe") ||
        countryCode == "fr" || countryCode == "it" || countryCode == "es" || countryCode == "de" || countryCode == "eu" -> {
            CurrencyConfig(symbol = "€", rate = 0.92, code = "EUR")
        }
        loc.contains("tokyo") || loc.contains("japan") || loc.contains("jpy") ||
        country.contains("japan") || countryCode == "jp" || city.contains("tokyo") -> {
            CurrencyConfig(symbol = "¥", rate = 155.0, code = "JPY")
        }
        loc.contains("india") || loc.contains("mumbai") || loc.contains("delhi") || loc.contains("inr") || loc.contains("dibrugarh") || loc.contains("assam") -> {
            CurrencyConfig(symbol = "₹", rate = 83.0, code = "INR")
        }
        loc.contains("canada") || loc.contains("toronto") || loc.contains("cad") ||
        country.contains("canada") || countryCode == "ca" || city.contains("toronto") -> {
            CurrencyConfig(symbol = "CA$", rate = 1.36, code = "CAD")
        }
        loc.contains("australia") || loc.contains("sydney") || loc.contains("aud") ||
        country.contains("australia") || countryCode == "au" || city.contains("sydney") -> {
            CurrencyConfig(symbol = "A$", rate = 1.50, code = "AUD")
        }
        else -> {
            CurrencyConfig(symbol = "$", rate = 1.0, code = "USD")
        }
    }
}

fun formatPrice(priceInUsd: Double, config: CurrencyConfig): String {
    val converted = priceInUsd * config.rate
    return if (config.code == "JPY") {
        "${config.symbol}${String.format("%.0f", converted)}"
    } else {
        "${config.symbol}${String.format("%.2f", converted)}"
    }
}

@Composable
fun CollectionTab(
    viewModel: MareViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isThemeDark.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val savedProducts by viewModel.savedProducts.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val weatherState by viewModel.weather.collectAsState()

    var activeSubTab by remember { mutableStateOf("Catalog") } // "Catalog" or "Lookbook"
    var searchQuery by remember { mutableStateOf("") }
    var selectedSort by remember { mutableStateOf("Popularity") } // "Popularity", "Low-High", "High-Low"
    var selectedCollection by remember { mutableStateOf("All") }

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

    var isLuxuryOnly by remember { mutableStateOf(false) }

    // Try to parse a numeric budget/price limit from search query
    val budgetParserResult = remember(searchQuery, currencyConfig) {
        val q = searchQuery.lowercase().trim()
        var maxPriceInUsd: Double? = null
        var textQuery = searchQuery
        
        // Match numbers optionally preceded by "under", "below", "less than", "budget", "price" or currency symbols
        val budgetRegex = Regex("(?:under|below|less\\s+than|budget|price|max|maximum)?\\s*(?:[\\$\\u20B9\\u00A3\\u20AC\\u00A5]|rs\\.?|rupees)?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(?:usd|inr|gbp|eur|jpy|cad|aud|rupees|dollars|pounds|euros)?")
        val match = budgetRegex.find(q)
        if (match != null) {
            val numberStr = match.groupValues[1]
            val value = numberStr.toDoubleOrNull()
            if (value != null) {
                var rate = currencyConfig.rate
                val fullMatchText = match.value
                if (fullMatchText.contains("$") || fullMatchText.contains("usd") || fullMatchText.contains("dollars")) {
                    rate = 1.0
                } else if (fullMatchText.contains("₹") || fullMatchText.contains("inr") || fullMatchText.contains("rs") || fullMatchText.contains("rupees")) {
                    rate = 83.0
                } else if (fullMatchText.contains("£") || fullMatchText.contains("gbp") || fullMatchText.contains("pounds")) {
                    rate = 0.79
                } else if (fullMatchText.contains("€") || fullMatchText.contains("eur") || fullMatchText.contains("euros")) {
                    rate = 0.92
                } else if (fullMatchText.contains("¥") || fullMatchText.contains("jpy")) {
                    rate = 155.0
                } else if (fullMatchText.contains("ca$") || fullMatchText.contains("cad")) {
                    rate = 1.36
                } else if (fullMatchText.contains("a$") || fullMatchText.contains("aud")) {
                    rate = 1.50
                }
                
                maxPriceInUsd = value / rate
                
                // Get remaining text query
                var clean = q.replace(fullMatchText, "").trim()
                clean = clean.replace(Regex("^[\\s,;\\-\\:]+|[\\s,;\\-\\:]+$"), "")
                textQuery = clean
            }
        }
        maxPriceInUsd to textQuery
    }

    val maxBudgetInUsd = budgetParserResult.first
    val parsedTextQuery = budgetParserResult.second

    // Filter and Sort Logic
    val filteredProducts = remember(allProducts, searchQuery, selectedSort, selectedCollection, isLuxuryOnly, maxBudgetInUsd, parsedTextQuery) {
        var list = allProducts.filter {
            val titleAndDesc = (it.title + " " + it.description).lowercase()
            val matchesText = parsedTextQuery.isEmpty() || titleAndDesc.contains(parsedTextQuery.lowercase())
            val matchesCollection = selectedCollection == "All" || it.collection.equals(selectedCollection, ignoreCase = true)
            matchesText && matchesCollection
        }
        
        // If luxury only is selected
        if (isLuxuryOnly) {
            list = list.filter {
                it.mainPrice >= 95.0 || 
                it.title.contains("Luxury", ignoreCase = true) ||
                it.title.contains("Luxe", ignoreCase = true) ||
                it.title.contains("Premium", ignoreCase = true) ||
                it.description.contains("Luxury", ignoreCase = true) ||
                it.description.contains("Luxe", ignoreCase = true) ||
                it.description.contains("Premium", ignoreCase = true)
            }
        }
        
        // If a budget/price limit is parsed
        if (maxBudgetInUsd != null) {
            list = list.filter { it.mainPrice <= maxBudgetInUsd }
        }

        list = when (selectedSort) {
            "Low-High" -> list.sortedBy { it.mainPrice }
            "High-Low" -> list.sortedByDescending { it.mainPrice }
            else -> list.sortedByDescending { it.rating } // Popularity
        }
        list
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title Header
        Text(
            text = if (activeSubTab == "Catalog") "Collection & Catalog" else "AI Mix-and-Match Lookbook",
            fontSize = 24.sp,
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Serif,
            color = primaryTextColor,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = if (activeSubTab == "Catalog") "Infinite search index cross-comparing personalized items." else "Design beautiful styling ensembles using your digital wardrobe.",
            fontSize = 12.sp,
            color = secondaryTextColor,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // SubTab Selection Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { activeSubTab = "Catalog" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubTab == "Catalog") {
                        if (isDark) Color(0xFF202D29) else Color(0xFFE2EBE6)
                    } else {
                        if (isDark) Color(0xFF2D3135) else Color(0xFFFFFFFF)
                    },
                    contentColor = if (activeSubTab == "Catalog") {
                        if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29)
                    } else {
                        if (isDark) Color(0xFFC4C7C5) else Color(0xFF5A6E6E)
                    }
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Luxury Catalog", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = { activeSubTab = "Lookbook" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubTab == "Lookbook") {
                        if (isDark) Color(0xFF202D29) else Color(0xFFE2EBE6)
                    } else {
                        if (isDark) Color(0xFF2D3135) else Color(0xFFFFFFFF)
                    },
                    contentColor = if (activeSubTab == "Lookbook") {
                        if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29)
                    } else {
                        if (isDark) Color(0xFFC4C7C5) else Color(0xFF5A6E6E)
                    }
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "AI Lookbook Studio", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        if (activeSubTab == "Catalog") {

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter fashion, skincare, accessories...", fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29),
                unfocusedBorderColor = cardBorderColor,
                focusedContainerColor = cardBgColor,
                unfocusedContainerColor = cardBgColor
            ),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = secondaryTextColor
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = secondaryTextColor
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        maxBudgetInUsd?.let { budgetUsd ->
            val formattedBudget = formatPrice(budgetUsd, currencyConfig)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .background(Color(0xFFC48E5A).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFC48E5A).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = Color(0xFFC48E5A),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Checking Prices: Showing products under $formattedBudget suitable for your economy!",
                    fontSize = 10.sp,
                    color = primaryTextColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBgColor.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isLuxuryOnly) Icons.Default.Star else Icons.Default.Spa,
                    contentDescription = null,
                    tint = if (isLuxuryOnly) Color(0xFFD4AF37) else Color(0xFF54C3A3),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = "Collection Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryTextColor)
                    Text(
                        text = if (isLuxuryOnly) "Luxury Mode (showing premium brands)" else "Standard Mode (showing all products)",
                        fontSize = 9.sp,
                        color = secondaryTextColor
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isLuxuryOnly) "Luxury" else "Normal",
                    fontSize = 11.sp,
                    color = primaryTextColor,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = isLuxuryOnly,
                    onCheckedChange = { isLuxuryOnly = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFD4AF37),
                        checkedTrackColor = Color(0xFF202D29),
                        uncheckedThumbColor = Color(0xFF54C3A3),
                        uncheckedTrackColor = Color(0xFF202D29)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sort Row Selection Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Sort by:", fontSize = 11.sp, color = secondaryTextColor)

            SortChip("Popularity", selectedSort == "Popularity", isDark) {
                selectedSort = "Popularity"
            }
            SortChip("Price: Low-High", selectedSort == "Low-High", isDark) {
                selectedSort = "Low-High"
            }
            SortChip("Price: High-Low", selectedSort == "High-Low", isDark) {
                selectedSort = "High-Low"
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Collections horizontal filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Collection:", fontSize = 11.sp, color = secondaryTextColor, modifier = Modifier.padding(end = 6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val collectionsList = listOf("All", "Fashion", "Skincare", "Accessories")
                items(collectionsList) { collName ->
                    SortChip(
                        text = collName,
                        isSelected = selectedCollection == collName,
                        isDark = isDark,
                        onClick = { selectedCollection = collName }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Infinite Grid
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Empty Grid",
                        tint = secondaryTextColor.copy(alpha = 0.3f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No matching items found inside the active catalog.",
                        fontSize = 13.sp,
                        color = secondaryTextColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                itemsIndexed(filteredProducts) { index, item ->
                    if (index >= filteredProducts.size - 6) {
                        LaunchedEffect(filteredProducts.size) {
                            viewModel.loadMoreProducts()
                        }
                    }
                    val isSaved = savedProducts.any { it.id == item.id }
                    ProductGridCard(
                        product = item,
                        isSaved = isSaved,
                        cardBg = cardBgColor,
                        borderColor = cardBorderColor,
                        primaryText = primaryTextColor,
                        secondaryText = secondaryTextColor,
                        currencyConfig = currencyConfig,
                        resolvedLocationName = resolvedLocationName,
                        resolvedCountryName = resolvedCountryName,
                        onBookmarkClick = { viewModel.toggleProductBookmark(item) }
                    )
                }
            }
        }
        } else {
            AiLookbookStudioView(
                viewModel = viewModel,
                isDark = isDark,
                cardBgColor = cardBgColor,
                cardBorderColor = cardBorderColor,
                primaryTextColor = primaryTextColor,
                secondaryTextColor = secondaryTextColor
            )
        }
    }
}

@Composable
fun ProductGridCard(
    product: SavedProductEntity,
    isSaved: Boolean,
    cardBg: Color,
    borderColor: Color,
    primaryText: Color,
    secondaryText: Color,
    currencyConfig: CurrencyConfig,
    resolvedLocationName: String,
    resolvedCountryName: String,
    onBookmarkClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fadeInUpOnMount(duration = 500)
            .hoverScaleAndShadow(shape = RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        var expandedPriceLayer by remember { mutableStateOf(false) }

        // Parse platforms JSON outside the UI block to comply with Compose rules
        val pricePlatforms = remember(product.pricesJson) {
            val list = mutableListOf<Pair<String, Double>>()
            try {
                val pricesArray = JSONArray(product.pricesJson)
                for (i in 0 until pricesArray.length()) {
                    val obj = pricesArray.getJSONObject(i)
                    list.add(obj.getString("platform") to obj.getDouble("price"))
                }
            } catch (e: Exception) {
                // Return empty list if invalid or missing JSON
            }
            list
        }

        val context = LocalContext.current
        val painter = if (product.imageUrl.startsWith("http")) {
            coil.compose.rememberAsyncImagePainter(
                model = product.imageUrl,
                placeholder = painterResource(id = com.example.R.drawable.img_ootd_minimalist),
                error = painterResource(id = com.example.R.drawable.img_ootd_minimalist)
            )
        } else {
            val imageResId = remember(product.imageUrl) {
                val resId = context.resources.getIdentifier(product.imageUrl, "drawable", context.packageName)
                if (resId != 0) resId else com.example.R.drawable.img_ootd_minimalist
            }
            painterResource(id = imageResId)
        }

        Column {
            // Product Visual Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color.LightGray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.TopEnd
            ) {
                Image(
                    painter = painter,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                IconButton(onClick = onBookmarkClick) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save Product",
                        tint = if (isSaved) Color(0xFFC48E5A) else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                // Collection Badge
                if (product.collection.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .background(Color(0xFFC48E5A).copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = product.collection.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC48E5A)
                        )
                    }
                }

                Text(
                    text = product.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatPrice(product.mainPrice, currencyConfig),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFC48E5A)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFD4AF37),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${product.rating}",
                            fontSize = 11.sp,
                            color = primaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Brand Availability and Delivery status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF54C3A3),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Delivers to $resolvedLocationName",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF54C3A3),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFFC48E5A),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "$resolvedCountryName Brand - Local Stock",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    fontSize = 11.sp,
                    color = secondaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Price Comparison Block (Collapsible)
                Button(
                    onClick = { expandedPriceLayer = !expandedPriceLayer },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (primaryText == Color(0xFFE2E2E6)) Color(0xFF202D29) else Color(0xFFE2EBE6),
                        contentColor = if (primaryText == Color(0xFFE2E2E6)) Color(0xFFA8D1C2) else Color(0xFF202D29)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (expandedPriceLayer) "Hide Platforms" else "Compare Prices",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expandedPriceLayer) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle",
                        modifier = Modifier.size(12.dp)
                    )
                }

                if (expandedPriceLayer) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (primaryText == Color(0xFFE2E2E6)) Color(0xFF1A1C1E) else Color(0xFFF5F6F5),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (pricePlatforms.isEmpty()) {
                            Text(text = "Price matrix temporary offline", fontSize = 8.sp, color = secondaryText)
                        } else {
                            pricePlatforms.forEach { (platformName, platformPrice) ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(Color(0xFF54C3A3), RoundedCornerShape(3.dp))
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = platformName, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = primaryText)
                                        }
                                        Text(
                                            text = formatPrice(platformPrice, currencyConfig),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC48E5A)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.padding(start = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalShipping,
                                            contentDescription = null,
                                            tint = secondaryText.copy(alpha = 0.7f),
                                            modifier = Modifier.size(9.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "Express Delivery to $resolvedLocationName Active",
                                            fontSize = 8.sp,
                                            color = secondaryText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SortChip(
    text: String,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) {
                    if (isDark) Color(0xFF202D29) else Color(0xFFE2EBE6)
                } else {
                    if (isDark) Color(0xFF2D3135) else Color(0xFFFFFFFF)
                }
            )
            .border(
                1.dp,
                if (isSelected) {
                    if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29)
                } else {
                    if (isDark) Color.Transparent else Color(0xFFD1D5DB)
                },
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) {
                if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29)
            } else {
                if (isDark) Color(0xFFC4C7C5) else Color(0xFF5A6E6E)
            }
        )
    }
}

@Composable
fun AiLookbookStudioView(
    viewModel: MareViewModel,
    isDark: Boolean,
    cardBgColor: Color,
    cardBorderColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color
) {
    val closetItems by viewModel.closetItems.collectAsState()
    val savedOutfits by viewModel.savedOutfits.collectAsState()
    val isAnalyzing by viewModel.isOutfitAnalyzing.collectAsState()
    val analysisResult by viewModel.outfitAnalysisResult.collectAsState()
    val analysisScore by viewModel.outfitAnalysisScore.collectAsState()

    val selectedItems = remember { mutableStateListOf<com.example.data.ClosetItemEntity>() }
    var lookbookTitle by remember { mutableStateOf("") }
    val scrollState = androidx.compose.foundation.rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Studio Intro Card
        Card(
            modifier = Modifier.fillMaxWidth().fadeInUpOnMount(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            border = BorderStroke(1.dp, cardBorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFFA8D1C2) else Color(0xFF2C4E41),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "AI MIX-AND-MATCH DESIGNER",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29),
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Combine elements from your digital wardrobe. Our luxury styling intelligence analyzes textural play, silhouette harmony, color balance, and real-time weather suitability.",
                    fontSize = 12.sp,
                    color = secondaryTextColor,
                    lineHeight = 16.sp
                )
            }
        }

        // Closet Selector Section
        Text(
            text = "Select Wardrobe Pieces",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Serif,
            color = primaryTextColor
        )

        if (closetItems.isEmpty()) {
            // Empty State - seed wardrobe basics button
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = "Empty Closet",
                        tint = secondaryTextColor.copy(alpha = 0.4f),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Your digital closet is currently empty",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryTextColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Seed your closet with timeless luxury capsule items to start creating drapes.",
                        fontSize = 11.sp,
                        color = secondaryTextColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.addClosetItem("Charcoal Wool Blazer", "Outerwear", "Charcoal", "Virgin Wool", "Unstructured elegant fit")
                            viewModel.addClosetItem("Cream Silk Blouse", "Tops", "Cream", "Mulberry Silk", "Fluid flow drape")
                            viewModel.addClosetItem("Relaxed Linen Trouser", "Bottoms", "Sand", "Organic Linen", "Wide-leg tailored cut")
                            viewModel.addClosetItem("Minimalist Leather Loafer", "Shoes", "Espresso", "Calfskin Leather", "Ultra-soft construction")
                            viewModel.addClosetItem("Gold Signet Ring", "Accessories", "Gold", "18k Solid Gold", "Geometric structural face")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF2E3D36) else Color(0xFFE2ECE9),
                            contentColor = if (isDark) Color(0xFFA8D1C2) else Color(0xFF1E352F)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Seed Luxury Capsule Basics", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Grid of closet items
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                closetItems.forEach { item ->
                    val isSelected = selectedItems.any { it.id == item.id }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) {
                                    selectedItems.removeAll { it.id == item.id }
                                } else {
                                    if (selectedItems.size < 5) {
                                        selectedItems.add(item)
                                    }
                                }
                            }
                            .border(
                                width = 1.dp,
                                color = if (isSelected) {
                                    if (isDark) Color(0xFF2C4E41) else Color(0xFFB4D2CB)
                                } else {
                                    cardBorderColor.copy(alpha = 0.5f)
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(
                                if (isSelected) {
                                    if (isDark) Color(0xFF1C2724) else Color(0xFFF3FAF8)
                                } else {
                                    cardBgColor
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSelected) {
                                    if (isDark) Color(0xFFA8D1C2) else Color(0xFF2C4E41)
                                } else {
                                    secondaryTextColor.copy(alpha = 0.5f)
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = item.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryTextColor
                                )
                                Text(
                                    text = "${item.category} • ${item.color} • ${item.material}",
                                    fontSize = 10.sp,
                                    color = secondaryTextColor
                                )
                            }
                        }
                        
                        // Category pill
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isDark) Color(0xFF2A2E32) else Color(0xFFF1F3F5),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = item.category,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryTextColor
                            )
                        }
                    }
                }
            }
        }

        // Selection Draft Board
        if (selectedItems.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACTIVE LOOK COMBINATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryTextColor,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Dotted divider or Row of cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedItems.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, cardBorderColor, RoundedCornerShape(10.dp))
                                    .background(
                                        if (isDark) Color(0xFF212529) else Color(0xFFFAFAFA),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedItems.remove(item) }
                                    .padding(8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = when (item.category.lowercase()) {
                                            "outerwear" -> Icons.Default.DryCleaning
                                            "tops" -> Icons.Default.Checkroom
                                            "bottoms" -> Icons.Default.Checkroom
                                            "shoes" -> Icons.Default.Checkroom
                                            else -> Icons.Default.Palette
                                        },
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFFA8D1C2) else Color(0xFF2C4E41),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = primaryTextColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isAnalyzing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = if (isDark) Color(0xFFA8D1C2) else Color(0xFF2C4E41),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Cross-analyzing styling layers...",
                                fontSize = 12.sp,
                                color = secondaryTextColor
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.analyzeOutfitCompatibility(selectedItems) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29),
                                contentColor = if (isDark) Color(0xFF202D29) else Color(0xFFFFFFFF)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Analyze Styling Harmony", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Analysis Results Card
        if (analysisScore != null && analysisResult != null) {
            val score = analysisScore ?: 80
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(2.dp, if (score >= 90) Color(0xFFD4AF37) else cardBorderColor)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "STYLE INTELLIGENCE DIALOGUE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC48E5A),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Compatibility Verdict",
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Serif,
                                color = primaryTextColor
                            )
                        }
                        
                        // Compatibility Rating Circle
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (score >= 90) Color(0xFFFFFBEB) else Color(0xFFF0FDF4),
                                    shape = CircleShape
                                )
                                .border(1.dp, if (score >= 90) Color(0xFFFBBF24) else Color(0xFF34D399), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$score%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (score >= 90) Color(0xFFB45309) else Color(0xFF047857)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = analysisResult ?: "",
                        fontSize = 12.sp,
                        color = secondaryTextColor,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = cardBorderColor.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Save this look to Lookbook",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryTextColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = lookbookTitle,
                            onValueChange = { lookbookTitle = it },
                            placeholder = { Text("E.g. Linen Spring Mix", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29),
                                unfocusedBorderColor = cardBorderColor
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                if (lookbookTitle.isBlank()) {
                                    lookbookTitle = "Curated Mix"
                                }
                                val names = selectedItems.joinToString(", ") { it.name }
                                val ids = selectedItems.map { it.id }
                                viewModel.saveOutfitMix(lookbookTitle, ids, names, score, analysisResult ?: "")
                                lookbookTitle = ""
                                selectedItems.clear()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color(0xFF2E3D36) else Color(0xFFE2ECE9),
                                contentColor = if (isDark) Color(0xFFA8D1C2) else Color(0xFF1E352F)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Saved Lookbooks History
        if (savedOutfits.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "My Curated Lookbooks",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif,
                color = primaryTextColor
            )

            savedOutfits.forEach { outfit ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                    border = BorderStroke(1.dp, cardBorderColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = outfit.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryTextColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = outfit.itemNames,
                                    fontSize = 10.sp,
                                    color = secondaryTextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Score badge
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (outfit.compatibilityScore >= 90) Color(0xFFFFFBEB) else Color(0xFFF0FDF4),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${outfit.compatibilityScore}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (outfit.compatibilityScore >= 90) Color(0xFFB45309) else Color(0xFF047857)
                                    )
                                }
                                
                                IconButton(onClick = { viewModel.deleteSavedOutfit(outfit) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = secondaryTextColor.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = outfit.stylingVerdict,
                            fontSize = 11.sp,
                            color = secondaryTextColor,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}
