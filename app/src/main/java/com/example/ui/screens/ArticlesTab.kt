package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.api.GeminiApiHelper
import com.example.data.SavedArticleEntity
import com.example.data.UserProfileEntity
import com.example.ui.viewmodel.MareViewModel
import com.example.ui.components.fadeInUpOnMount
import com.example.ui.components.hoverScaleAndShadow
import com.example.ui.components.GroundedFashionTrendsWidget
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ArticlesTab(
    viewModel: MareViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isThemeDark.collectAsState()
    val allArticles by viewModel.allArticles.collectAsState()
    val savedArticles by viewModel.savedArticles.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBgColor = MaterialTheme.colorScheme.surface
    val cardBorderColor = MaterialTheme.colorScheme.outlineVariant

    var searchQuery by remember { mutableStateOf("") }
    var selectedArticleForReader by remember { mutableStateOf<SavedArticleEntity?>(null) }

    // Sophisticated, fully personalized article score engine
    val scoredArticles = remember(allArticles, userProfile) {
        allArticles.map { article ->
            val matchReport = calculatePersonalizedMatch(article, userProfile)
            article to matchReport
        }.sortedByDescending { it.second.totalScore }
    }

    // Filter based on search bar query
    val filteredArticles = remember(scoredArticles, searchQuery) {
        if (searchQuery.isBlank()) {
            scoredArticles
        } else {
            val q = searchQuery.lowercase().trim()
            scoredArticles.filter { (article, _) ->
                article.title.lowercase().contains(q) ||
                article.category.lowercase().contains(q) ||
                article.summary.lowercase().contains(q) ||
                article.content.lowercase().contains(q)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Premium Editorial Header
            Text(
                text = "The Editorial Wave",
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Serif,
                color = primaryTextColor,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Intelligent luxury analytics cross-comparing your custom profile against world-class fashion houses and clinical dermatological formulations.",
                fontSize = 12.sp,
                color = secondaryTextColor,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                lineHeight = 16.sp
            )

            // Personal Profile Summary Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AttributeIndicatorChip("Shape: ${userProfile?.bodyShape ?: "Hourglass"}", isDark)
                AttributeIndicatorChip("Skin: ${userProfile?.skinTone ?: "Medium Olive"}", isDark)
                AttributeIndicatorChip("Style: ${userProfile?.stylePreference ?: "Minimal Luxury"}", isDark)
            }

            // Elegant Search Box matching Mare Aesthetic
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search world brands, catalogs, magazines...", fontSize = 13.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDark) Color(0xFF98C5BC) else Color(0xFF2D6A6A),
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
                },
                singleLine = true
            )
        }

        item {
            GroundedFashionTrendsWidget(
                viewModel = viewModel,
                isDark = isDark,
                cardBg = cardBgColor,
                borderColor = cardBorderColor,
                primaryText = primaryTextColor,
                secondaryText = secondaryTextColor
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (filteredArticles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No editorial matches found for \"$searchQuery\"",
                        fontSize = 14.sp,
                        color = secondaryTextColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            itemsIndexed(filteredArticles) { index, (article, matchReport) ->
                // Infinite scrolling threshold trigger
                if (index >= filteredArticles.size - 3) {
                    LaunchedEffect(filteredArticles.size) {
                        viewModel.loadMoreArticles()
                    }
                }

                val isSaved = savedArticles.any { it.id == article.id }
                ArticleFeedCard(
                    article = article,
                    matchReport = matchReport,
                    isSaved = isSaved,
                    cardBg = cardBgColor,
                    borderColor = cardBorderColor,
                    primaryText = primaryTextColor,
                    secondaryText = secondaryTextColor,
                    onBookmarkClick = { viewModel.toggleArticleBookmark(article) },
                    onOpenReader = { selectedArticleForReader = article }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Gorgeous full-screen dialog reader
    selectedArticleForReader?.let { article ->
        val matchReport = remember(article, userProfile) {
            calculatePersonalizedMatch(article, userProfile)
        }
        EditorialArticleReaderDialog(
            article = article,
            matchReport = matchReport,
            userProfile = userProfile,
            allArticles = allArticles,
            onArticleSelected = { selectedArticleForReader = it },
            onDismiss = { selectedArticleForReader = null },
            isDark = isDark,
            isSaved = savedArticles.any { it.id == article.id },
            onBookmarkToggle = { viewModel.toggleArticleBookmark(article) }
        )
    }
}

// Data class to store calculated match parameters
data class MatchReport(
    val totalScore: Int,
    val matchPercentage: Int,
    val isShapeMatch: Boolean,
    val isSkinToneMatch: Boolean,
    val isStyleMatch: Boolean,
    val matchedColors: List<String>,
    val isLocationMatch: Boolean,
    val matchedReasons: List<String>
)

// Computes a highly thorough match report by analyzing all aspects of user profile against the article
private fun calculatePersonalizedMatch(article: SavedArticleEntity, profile: UserProfileEntity?): MatchReport {
    if (profile == null) {
        return MatchReport(
            totalScore = 60,
            matchPercentage = 60,
            isShapeMatch = false,
            isSkinToneMatch = false,
            isStyleMatch = false,
            matchedColors = emptyList(),
            isLocationMatch = false,
            matchedReasons = listOf("Curated based on general trends")
        )
    }

    var score = 60 // Base score
    val reasons = mutableListOf<String>()

    // 1. Body Shape Check
    val shape = profile.bodyShape.lowercase()
    val shapeMatch = article.title.lowercase().contains(shape) || article.content.lowercase().contains(shape)
    if (shapeMatch) {
        score += 12
        reasons.add("Specifically matches your ${profile.bodyShape} silhouette geometry")
    }

    // 2. Skin Tone Check
    val tone = profile.skinTone.lowercase().replace("medium ", "").replace("dark ", "").replace("pale ", "")
    val skinMatch = article.title.lowercase().contains(tone) || article.content.lowercase().contains(tone) ||
                    (profile.skinTone.lowercase().contains("olive") && article.content.lowercase().contains("melanin"))
    if (skinMatch) {
        score += 12
        reasons.add("Optimized to elevate your ${profile.skinTone} complexion glow")
    }

    // 3. Style Preference Check
    val stylePref = profile.stylePreference.lowercase().split(" ").firstOrNull() ?: "minimal"
    val styleMatch = article.title.lowercase().contains(stylePref) || article.content.lowercase().contains(stylePref) ||
                     article.summary.lowercase().contains(stylePref)
    if (styleMatch) {
        score += 10
        reasons.add("Aligned with your '${profile.stylePreference}' aesthetic standard")
    }

    // 4. Color Check
    val colors = profile.favoriteColors.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
    val matched = colors.filter { c ->
        article.title.lowercase().contains(c) || article.content.lowercase().contains(c)
    }
    if (matched.isNotEmpty()) {
        score += matched.size * 3
        reasons.add("Features colorways in your preferred palette (${matched.joinToString(", ") { it.replaceFirstChar { it.uppercase() } }})")
    }

    // 5. Climate / Location Check
    val loc = profile.location.lowercase()
    val locMatch = article.content.lowercase().contains(loc) || 
                   (profile.location.lowercase().contains("london") && article.content.lowercase().contains("humidity"))
    if (locMatch) {
        score += 8
        reasons.add("Tailored for climate characteristics of ${profile.location}")
    }

    // Constrain percentage between 60% and 99%
    val percentage = score.coerceIn(60, 99)

    return MatchReport(
        totalScore = score,
        matchPercentage = percentage,
        isShapeMatch = shapeMatch,
        isSkinToneMatch = skinMatch,
        isStyleMatch = styleMatch,
        matchedColors = matched,
        isLocationMatch = locMatch,
        matchedReasons = if (reasons.isEmpty()) listOf("Curated luxury matching based on seasonal trends") else reasons
    )
}

@Composable
fun ArticleFeedCard(
    article: SavedArticleEntity,
    matchReport: MatchReport? = null,
    isSaved: Boolean,
    cardBg: Color,
    borderColor: Color,
    primaryText: Color,
    secondaryText: Color,
    onBookmarkClick: () -> Unit,
    onOpenReader: (() -> Unit)? = null
) {
    var isExpandedLocal by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fadeInUpOnMount(duration = 500)
            .hoverScaleAndShadow(shape = RoundedCornerShape(24.dp))
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable { 
                if (onOpenReader != null) {
                    onOpenReader()
                } else {
                    isExpandedLocal = !isExpandedLocal
                }
            },
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: Category, Bookmark, & Glowing Match Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2D6A6A).copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = article.category.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D6A6A),
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Gorgeous Personalized Match percentage pill
                    if (matchReport != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFC48E5A).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${matchReport.matchPercentage}% Personal Match",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9E6D38),
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isSaved) Color(0xFFC48E5A) else secondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Article Title
            Text(
                text = article.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Serif,
                color = primaryText,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Cover Image
            if (article.imageUrl.isNotEmpty()) {
                val context = LocalContext.current
                val painter = if (article.imageUrl.startsWith("http")) {
                    coil.compose.rememberAsyncImagePainter(
                        model = article.imageUrl,
                        placeholder = painterResource(id = com.example.R.drawable.img_ootd_minimalist),
                        error = painterResource(id = com.example.R.drawable.img_ootd_minimalist)
                    )
                } else {
                    val imageResId = remember(article.imageUrl) {
                        val resId = context.resources.getIdentifier(article.imageUrl, "drawable", context.packageName)
                        if (resId != 0) resId else com.example.R.drawable.img_ootd_minimalist
                    }
                    painterResource(id = imageResId)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Image(
                        painter = painter,
                        contentDescription = article.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                                    startY = 50f
                                )
                            )
                    )

                    Text(
                        text = article.readTime,
                        fontSize = 10.sp,
                        color = Color.White,
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Brief summary or full content if expanded local
            Text(
                text = if (onOpenReader == null && isExpandedLocal) article.content else article.summary,
                fontSize = 13.sp,
                color = secondaryText,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Matching Attributes Quick Chips
            if (matchReport != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (matchReport.isShapeMatch) {
                        MiniMatchChip("Shape Match", Color(0xFF2D6A6A))
                    }
                    if (matchReport.isSkinToneMatch) {
                        MiniMatchChip("Tone Match", Color(0xFFC48E5A))
                    }
                    if (matchReport.isStyleMatch) {
                        MiniMatchChip("Aesthetic Match", Color(0xFF5F5E5E))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Beautiful Call-to-action button to Open the beautiful Full Screen Reader or toggle expand
            Button(
                onClick = {
                    if (onOpenReader != null) {
                        onOpenReader()
                    } else {
                        isExpandedLocal = !isExpandedLocal
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2D6A6A),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (onOpenReader != null) "Launch Premium Reader" else if (isExpandedLocal) "Collapse Article" else "Read Entire Feature",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MiniMatchChip(text: String, tintColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(tintColor.copy(alpha = 0.08f))
            .border(1.dp, tintColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            color = tintColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorialArticleReaderDialog(
    article: SavedArticleEntity,
    matchReport: MatchReport,
    userProfile: UserProfileEntity?,
    allArticles: List<SavedArticleEntity>,
    onArticleSelected: (SavedArticleEntity) -> Unit,
    onDismiss: () -> Unit,
    isDark: Boolean,
    isSaved: Boolean,
    onBookmarkToggle: () -> Unit
) {
    var readingFontSize by remember { mutableStateOf(16f) }
    var aiGuideText by remember { mutableStateOf<String?>(null) }
    var isGeneratingAi by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(article.id) {
        aiGuideText = null
        isGeneratingAi = false
    }

    val surfaceColor = if (isDark) Color(0xFF151D1A) else Color(0xFFF4F3EF)
    val textColor = if (isDark) Color(0xFFF4F3EF) else Color(0xFF1C2D27)
    val cardBgColor = if (isDark) Color(0xFF1F2825) else Color(0xFFFFFFFF)
    val borderColor = if (isDark) Color(0xFF2E3D36) else Color(0xFFC5DCD6)

    val scrollState = remember(article.id) { ScrollState(0) }
    val progress = if (scrollState.maxValue > 0) {
        scrollState.value.toFloat() / scrollState.maxValue.toFloat()
    } else {
        0f
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false // Forces full screen canvas
        )
    ) {
        Scaffold(
            topBar = {
                Surface(
                    color = surfaceColor,
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onDismiss) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = textColor
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Editorial Reader",
                                    fontSize = 16.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Bookmark Inside Reader
                                IconButton(onClick = onBookmarkToggle) {
                                    Icon(
                                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark",
                                        tint = if (isSaved) Color(0xFFC48E5A) else textColor
                                    )
                                }

                                // Font size decrease
                                IconButton(
                                    onClick = { readingFontSize = (readingFontSize - 2f).coerceAtLeast(12f) },
                                    enabled = readingFontSize > 12f
                                ) {
                                    Text("A-", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                                }

                                // Font size indicator
                                Text(
                                    text = "${readingFontSize.toInt()}sp",
                                    fontSize = 11.sp,
                                    color = textColor.copy(alpha = 0.6f)
                                )

                                // Font size increase
                                IconButton(
                                    onClick = { readingFontSize = (readingFontSize + 2f).coerceAtMost(26f) },
                                    enabled = readingFontSize < 26f
                                ) {
                                    Text("A+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
                                }
                            }
                        }

                        // Premium, high-contrast reading progress indicator reflecting scrolling depth
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = Color(0xFFC48E5A),
                            trackColor = Color.Transparent
                        )
                    }
                }
            },
            containerColor = surfaceColor
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
            ) {
                // Large cover banner image with parallax gradient overlay
                if (article.imageUrl.isNotEmpty()) {
                    val context = LocalContext.current
                    val painter = if (article.imageUrl.startsWith("http")) {
                        coil.compose.rememberAsyncImagePainter(
                            model = article.imageUrl,
                            placeholder = painterResource(id = com.example.R.drawable.img_ootd_minimalist),
                            error = painterResource(id = com.example.R.drawable.img_ootd_minimalist)
                        )
                    } else {
                        val imageResId = remember(article.imageUrl) {
                            val resId = context.resources.getIdentifier(article.imageUrl, "drawable", context.packageName)
                            if (resId != 0) resId else com.example.R.drawable.img_ootd_minimalist
                        }
                        painterResource(id = imageResId)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Elegant vertical gradient fading into the surface color
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            surfaceColor.copy(alpha = 0.4f),
                                            surfaceColor
                                        ),
                                        startY = 100f
                                    )
                                )
                        )
                    }
                }

                // Content area sheet with elegant overlapping rounded corners
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = if (article.imageUrl.isNotEmpty()) (-28).dp else 0.dp)
                        .background(surfaceColor, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    // Category & Read Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = article.category.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D6A6A),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = article.readTime,
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title
                    Text(
                        text = article.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.Serif,
                        color = textColor,
                        lineHeight = 32.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // PERSONALIZATION BREAKDOWN CARD
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
                        color = cardBgColor,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mare Personalization Report",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0xFFC48E5A))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${matchReport.matchPercentage}% Score",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Structured Checklists showing how it matches
                            matchReport.matchedReasons.forEach { reason ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2D6A6A),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = reason,
                                        fontSize = 12.sp,
                                        color = textColor.copy(alpha = 0.8f),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // MAIN READABLE ARTICLE TEXT - Multi-paragraph parsed rich editor with inline high-res images!
                    val paragraphs = remember(article.content) { article.content.split("\n\n") }
                    paragraphs.forEachIndexed { pIndex, paragraph ->
                        val trimmed = paragraph.trim()
                        if (trimmed.startsWith("### ")) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 22.dp, bottom = 12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(20.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFF2D6A6A))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = trimmed.removePrefix("### "),
                                    fontSize = (readingFontSize + 3).sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }
                        } else if (trimmed.startsWith("> ")) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                                    .padding(vertical = 16.dp)
                                    .background(Color(0xFFC48E5A).copy(alpha = 0.05f), RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                                    .padding(end = 12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .fillMaxHeight()
                                        .background(Color(0xFFC48E5A))
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = trimmed.removePrefix("> "),
                                    fontSize = (readingFontSize + 1).sp,
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic,
                                    color = textColor.copy(alpha = 0.85f),
                                    lineHeight = (readingFontSize * 1.55f).sp,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        } else if (trimmed.startsWith("- ")) {
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                trimmed.split("\n").forEach { listItem ->
                                    val cleanItem = listItem.trim().removePrefix("- ").trim()
                                    if (cleanItem.isNotEmpty()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "•",
                                                fontSize = (readingFontSize + 2).sp,
                                                color = Color(0xFF2D6A6A),
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Text(
                                                text = cleanItem,
                                                fontSize = readingFontSize.sp,
                                                fontFamily = FontFamily.Serif,
                                                color = textColor,
                                                lineHeight = (readingFontSize * 1.52f).sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = trimmed,
                                fontSize = readingFontSize.sp,
                                fontFamily = FontFamily.Serif,
                                color = textColor,
                                lineHeight = (readingFontSize * 1.55f).sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            )
                        }

                        // Inject beautiful inline high-res images in-between editorial blocks to simulate real premium websites
                        if (pIndex == 1) {
                            getSecondaryImageForArticle(article.id)?.let { (imgRes, caption) ->
                                EditorialInlineImage(imageResName = imgRes, caption = caption, isDark = isDark)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // GEMINI INTERACTIVE AI STYLING COMPANION SECTION
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFC48E5A).copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                        color = Color(0xFFC48E5A).copy(alpha = 0.05f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFC48E5A),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Personal styling Guide",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }

                            Text(
                                text = "Ask Mare's AI Fashion and Skincare Director to analyze this brand's publication specifically for your style preferences and London climate conditions.",
                                fontSize = 11.sp,
                                color = textColor.copy(alpha = 0.65f),
                                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                                lineHeight = 15.sp
                            )

                            if (aiGuideText == null && !isGeneratingAi) {
                                Button(
                                    onClick = {
                                        isGeneratingAi = true
                                        scope.launch {
                                            try {
                                                val profileStr = userProfile?.let { p ->
                                                    """
                                                    - User Name: ${p.name}
                                                    - Gender: ${p.gender}
                                                    - Age Group: ${p.ageGroup}
                                                    - Body Shape: ${p.bodyShape}
                                                    - Skin Tone: ${p.skinTone}
                                                    - Style Preference: ${p.stylePreference}
                                                    - Favorite Colors: ${p.favoriteColors}
                                                    - Location: ${p.location}
                                                    - Clothing Size: ${p.clothingSize}
                                                    """.trimIndent()
                                                } ?: "General style seeker"

                                                val prompt = """
                                                    You are Mare's AI Director of Fashion and Skincare.
                                                    The user has read an article/catalog titled "${article.title}" by ${article.category}.
                                                    
                                                    Article Content Summary:
                                                    "${article.summary}"
                                                    
                                                    Article Content Full:
                                                    "${article.content}"
                                                    
                                                    User Personal Profile:
                                                    $profileStr
                                                    
                                                    Please perform an intelligent analysis and provide a customized styling guide for this user:
                                                    1. Direct Personal Styling Application: Explain step-by-step how to integrate this brand's piece or skincare recommendation into their actual wardrobe or ritual, taking into account their body shape (${userProfile?.bodyShape ?: "Hourglass"}), skin tone (${userProfile?.skinTone ?: "Olive"}), clothing size (${userProfile?.clothingSize ?: "M"}), and favorite colors.
                                                    2. Climate/Location Coordination: Mention how to adapt this style specifically for ${userProfile?.location ?: "London"}'s typical environment and temperatures.
                                                    
                                                    Provide your expert fashion advice in concise, beautifully styled bullet points. Speak directly to ${userProfile?.name ?: "the user"} in a warm, sophisticated, high-end editorial voice. Do not write an essay, keep it highly actionable and structured.
                                                """.trimIndent()

                                                val response = GeminiApiHelper.callGemini(
                                                    model = "gemini-3.5-flash",
                                                    prompt = prompt,
                                                    systemInstruction = "You are Mare's AI Fashion and Skincare Director, a world-class luxury style advisor."
                                                )
                                                aiGuideText = response
                                            } catch (e: Exception) {
                                                aiGuideText = "Unable to coordinate with Mare AI servers: ${e.localizedMessage}. Please verify your API Key and network connection."
                                            } finally {
                                                isGeneratingAi = false
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFC48E5A),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Analyze with Mare AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else if (isGeneratingAi) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color(0xFFC48E5A),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Analyzing wardrobe coordinates...",
                                        fontSize = 12.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = textColor.copy(alpha = 0.7f)
                                    )
                                }
                            } else {
                                // Beautiful AI text block
                                Column {
                                    Text(
                                        text = aiGuideText ?: "",
                                        fontSize = 12.sp,
                                        color = textColor,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    OutlinedButton(
                                        onClick = { aiGuideText = null },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color(0xFFC48E5A)
                                        ),
                                        border = BorderStroke(1.dp, Color(0xFFC48E5A)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Reset AI Guidance", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // RELATED ARTICLES SECTION
                    val relatedArticles = remember(article, allArticles) {
                        allArticles
                            .filter { it.id != article.id }
                            .sortedWith(
                                compareByDescending<SavedArticleEntity> { it.category == article.category }
                                    .thenBy { it.id }
                            )
                            .take(3)
                    }

                    if (relatedArticles.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))

                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 1.dp,
                            color = if (isDark) Color(0xFF2A3331) else Color(0xFFE5E3DF)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Related Curated Editions",
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Medium,
                            color = textColor,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            relatedArticles.forEach { related ->
                                RelatedArticleCard(
                                    article = related,
                                    isDark = isDark,
                                    onClick = { onArticleSelected(related) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

/**
 * Returns a high-resolution secondary illustration or photo for mid-article inclusion
 */
private fun getSecondaryImageForArticle(articleId: String): Pair<String, String>? {
    return when (articleId) {
        "1" -> Pair("img_ootd_smart_poplin", "Figure 2: Gucci pre-fall collection exploring natural-slub cotton coordinates and fluid structured lines.")
        "2" -> Pair("img_product_spf", "Figure 2: Layering active serums with micro-particle UV blockers ensures ultimate defense in city humidity.")
        "3" -> Pair("img_ootd_cozy_wool", "Figure 2: Structured Chanel drapes adapt gracefully to seasonal wind-flow, elevating urban street aesthetics.")
        "4" -> Pair("img_product_serum", "Figure 2: Formulations with zero-cast zinc and organic filters melt beautifully into warm skin complexions.")
        "5" -> Pair("img_ootd_male_cold", "Figure 2: Prada drop-shoulder wool tailoring provides relaxed, modern urban protective warmth.")
        "6" -> Pair("img_product_serum", "Figure 2: Applying protective lipid barriers protects sensitive skin layers from icy metropolitan drafts.")
        else -> null
    }
}

/**
 * Renders an inline luxury image styled gracefully with a caption for detailed reading
 */
@Composable
fun EditorialInlineImage(
    imageResName: String,
    caption: String,
    isDark: Boolean
) {
    val context = LocalContext.current
    val imageResId = remember(imageResName) {
        val resId = context.resources.getIdentifier(imageResName, "drawable", context.packageName)
        if (resId != 0) resId else com.example.R.drawable.img_ootd_minimalist
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .border(
                    1.dp,
                    if (isDark) Color(0xFF2E3D36) else Color(0xFFC5DCD6),
                    RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF1F2825) else Color(0xFFFFFFFF)
            )
        ) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = caption,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = caption,
            fontSize = 11.sp,
            fontStyle = FontStyle.Italic,
            fontFamily = FontFamily.Serif,
            color = if (isDark) Color(0xFFA8D1C2) else Color(0xFF5F5E5E),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun AttributeIndicatorChip(text: String, isDark: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDark) Color(0xFF202D29) else Color(0xFFD4F6EB))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            color = if (isDark) Color(0xFFA8D1C2) else Color(0xFF1E3A2F),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun RelatedArticleCard(
    article: SavedArticleEntity,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val imageResId = remember(article.imageUrl) {
        val resId = context.resources.getIdentifier(article.imageUrl, "drawable", context.packageName)
        if (resId != 0) resId else com.example.R.drawable.img_ootd_minimalist
    }

    val cardBg = if (isDark) Color(0xFF1F2825) else Color(0xFFFFFFFF)
    val borderColor = if (isDark) Color(0xFF2E3D36) else Color(0xFFC5DCD6)
    val textColor = if (isDark) Color(0xFFF4F3EF) else Color(0xFF1C2D27)
    val secondaryColor = if (isDark) Color(0xFF8FA499) else Color(0xFF5D737E)

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .fadeInUpOnMount(duration = 450)
            .hoverScaleAndShadow(shape = RoundedCornerShape(16.dp))
            .testTag("related_article_${article.id}")
            .clickable(onClick = onClick)
            .border(
                1.dp,
                borderColor,
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mini image thumbnail
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.LightGray.copy(alpha = 0.1f)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Text Info Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = article.category.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC48E5A),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = article.title,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    maxLines = 2,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = article.readTime,
                    fontSize = 11.sp,
                    color = secondaryColor
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Click chevron
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Read related article",
                tint = Color(0xFF2D6A6A),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

