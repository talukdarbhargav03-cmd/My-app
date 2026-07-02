package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MareViewModel
import com.example.ui.viewmodel.OotdRecommendation
import com.example.ui.components.fadeInUpOnMount
import com.example.ui.components.hoverScaleAndShadow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
fun HomeTab(
    viewModel: MareViewModel,
    modifier: Modifier = Modifier,
    onNavigateToArticles: () -> Unit,
    onNavigateToCollection: () -> Unit
) {
    val weatherState by viewModel.weather.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val isDark by viewModel.isThemeDark.collectAsState()
    val ootdIdx by viewModel.ootdIndex.collectAsState()

    val ootd = viewModel.generateOotdRecommendation()
    val palette = viewModel.generateColourPalette()

    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBgColor = MaterialTheme.colorScheme.surface
    val cardBorderColor = MaterialTheme.colorScheme.outlineVariant

    // ScrollState for Home
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sticky/Top Curved Seafoam Header with Serif Logo
        CurvedSeafoamHeader(isDark)

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Weather & Greeting Block
        GreetingWeatherBlock(
            username = userProfile?.name ?: "Maya",
            weatherState = weatherState,
            isDark = isDark,
            primaryText = primaryTextColor,
            secondaryText = secondaryTextColor
        )

        // TASK 2: Atmospheric Routine Adjustments Widget
        Spacer(modifier = Modifier.height(16.dp))
        WeatherRoutineAdjustmentsWidget(
            weatherState = weatherState,
            isDark = isDark,
            cardBg = cardBgColor,
            borderColor = cardBorderColor,
            primaryText = primaryTextColor,
            secondaryText = secondaryTextColor
        )

        // Module One: The Outfit of the Day (OOTD Card)
        Spacer(modifier = Modifier.height(16.dp))
        OotdCard(
            viewModel = viewModel,
            ootd = ootd,
            weatherState = weatherState,
            cardBg = cardBgColor,
            borderColor = cardBorderColor,
            primaryText = primaryTextColor,
            secondaryText = secondaryTextColor,
            onActionClick = onNavigateToCollection
        )

        // TASK 1: AI Editorial Styling Assistant
        Spacer(modifier = Modifier.height(16.dp))
        StylingAssistantWidget(
            viewModel = viewModel,
            isDark = isDark,
            cardBg = cardBgColor,
            borderColor = cardBorderColor,
            primaryText = primaryTextColor,
            secondaryText = secondaryTextColor
        )

        // Module Two: Real-Time Weather-Adaptive AI Recommendations (Styling & Skincare)
        Spacer(modifier = Modifier.height(16.dp))
        AiAdaptiveRecommendationsCard(
            viewModel = viewModel,
            cardBg = cardBgColor,
            borderColor = cardBorderColor,
            primaryText = primaryTextColor,
            secondaryText = secondaryTextColor
        )

        // Module Two.5: Weather-Adaptive Outfit Layering & Palette Grid
        Spacer(modifier = Modifier.height(16.dp))
        WeatherAdaptiveLayeringGrid(
            weatherState = weatherState,
            palette = palette,
            cardBg = cardBgColor,
            borderColor = cardBorderColor,
            primaryText = primaryTextColor,
            secondaryText = secondaryTextColor
        )



        // Module Three: Personalised Daily Colour Palette Block
        Spacer(modifier = Modifier.height(16.dp))
        ColourPaletteBlock(
            viewModel = viewModel,
            palette = palette,
            cardBg = cardBgColor,
            borderColor = cardBorderColor,
            primaryText = primaryTextColor,
            secondaryText = secondaryTextColor
        )

        // Module Four: Search & Sort Shortcut Bar
        Spacer(modifier = Modifier.height(16.dp))
        SearchSortShortcut(
            cardBg = cardBgColor,
            borderColor = cardBorderColor,
            primaryText = primaryTextColor,
            onClick = onNavigateToCollection
        )

        // Module Five: Integrated AI Chatbot & Photo Analyzer Widget
        Spacer(modifier = Modifier.height(24.dp))
        ChatbotWidget(
            viewModel = viewModel,
            cardBg = cardBgColor,
            borderColor = cardBorderColor,
            primaryText = primaryTextColor,
            secondaryText = secondaryTextColor,
            isDark = isDark
        )

        Spacer(modifier = Modifier.height(100.dp)) // Nav bar offset
    }
}

@Composable
fun CurvedSeafoamHeader(isDark: Boolean) {
    // Elegant curved light-mint textured seafoam background with the serif "M A R E" logo as requested
    val headerBg = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF1F2825), Color(0xFF2E3D36))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFC5DCD6), Color(0xFFB4D2CB))
        )
    }

    val serifColor = if (isDark) Color(0xFFC5DCD6) else Color(0xFF1C2D27)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
            .background(headerBg)
            .drawBehind {
                // Draw luxury waves overlay lines representing ocean fluid aesthetics
                val waveColor = if (isDark) Color(0x1F5D737E) else Color(0x2BC5DCD6)
                val width = size.width
                val height = size.height
                drawCircle(
                    color = waveColor,
                    radius = height * 1.5f,
                    center = Offset(width / 2, -height * 0.5f)
                )
                drawCircle(
                    color = waveColor,
                    radius = height * 1.1f,
                    center = Offset(width / 4, -height * 0.2f)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(
                text = "M A R E",
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Serif,
                letterSpacing = 8.sp,
                color = serifColor
            )
            Text(
                text = "THE EFFORTLESS WAVE OF STYLE AND SKIN",
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                color = if (isDark) Color(0xFFC5DCD6).copy(alpha = 0.8f) else Color(0xFF1C2D27).copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun GreetingWeatherBlock(
    username: String,
    weatherState: com.example.ui.viewmodel.WeatherState,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color
) {
    val dynamicWeatherDesc = when {
        weatherState.humidity > 70 -> "High humidity today in ${weatherState.city}."
        weatherState.temperature > 25 -> "Warm sunshine today in ${weatherState.city}."
        weatherState.temperature < 15 -> "Chilly breeze today in ${weatherState.city}."
        else -> "Beautiful clear skies today in ${weatherState.city}."
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left column: Greeting name & description
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$username.",
                fontSize = 34.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dynamicWeatherDesc,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = secondaryText,
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Right column: Huge temperature & humidity
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${weatherState.temperature.toInt()}°C",
                fontSize = 44.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = "Humidity Icon",
                    tint = secondaryText,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${weatherState.humidity}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = secondaryText
                )
            }
        }
    }
}

@Composable
fun OotdCard(
    viewModel: MareViewModel,
    ootd: com.example.ui.viewmodel.OotdRecommendation,
    weatherState: com.example.ui.viewmodel.WeatherState,
    cardBg: Color,
    borderColor: Color,
    primaryText: Color,
    secondaryText: Color,
    onActionClick: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val profile = userProfile ?: com.example.data.UserProfileEntity()

    val bodyShape = profile.bodyShape
    val skinTone = profile.skinTone

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .fadeInUpOnMount(duration = 600, delayMillis = 100)
            .hoverScaleAndShadow(shape = RoundedCornerShape(24.dp))
            .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column {
            // 1. Beautiful image banner at the top of the card as shown in Image 1
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp) // Generous high-fashion height
                    .background(Color(0xFFE2D8D0))
            ) {
                Image(
                    painter = painterResource(id = ootd.imageResId),
                    contentDescription = "${ootd.title} Outfit",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // "Ideal for 22°C" Badge with overlay in top left
                Box(
                    modifier = Modifier
                        .padding(top = 20.dp, start = 20.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Sun Icon",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ideal for ${weatherState.temperature.toInt()}°C",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 2. Body Text and elements below the image
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                // Wide-tracked tiny header
                Text(
                    text = "OUTFIT OF THE DAY",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (primaryText == Color(0xFFE2E2E6)) Color(0xFFA8D1C2) else Color(0xFF2E4E42),
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Elegant serif style title
                Text(
                    text = ootd.title,
                    fontSize = 32.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = primaryText
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Fabric Subtitle
                Text(
                    text = "Fabric: ${ootd.fabric}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = secondaryText
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Justification details from image
                Text(
                    text = ootd.justification,
                    fontSize = 15.sp,
                    color = secondaryText,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Teal/Mint Green dynamic pill badge:
                // "Optimized for your Pear body shape & Cool Summer skin tone"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (primaryText == Color(0xFFE2E2E6)) Color(0xFF202D29) else Color(0xFFE2EFEA),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Optimization Icon",
                            tint = if (primaryText == Color(0xFFE2E2E6)) Color(0xFFA8D1C2) else Color(0xFF1E3A31),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Optimized for your $bodyShape body shape\n& $skinTone skin tone",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (primaryText == Color(0xFFE2E2E6)) Color(0xFFA8D1C2) else Color(0xFF1E3A31),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val ootdIdx by viewModel.ootdIndex.collectAsState()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Dark "View Details" button, fully matching Image 3
                    Button(
                        onClick = onActionClick,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (primaryText == Color(0xFFE2E2E6)) Color.White else Color(0xFF1A1C1E),
                            contentColor = if (primaryText == Color(0xFFE2E2E6)) Color(0xFF1A1C1E) else Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "View Details",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Outlined "Cycle Look" button
                    OutlinedButton(
                        onClick = { viewModel.rotateOotd() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        border = BorderStroke(
                            1.dp,
                            if (primaryText == Color(0xFFE2E2E6)) Color.White.copy(alpha = 0.5f) else Color(0xFF1A1C1E).copy(alpha = 0.4f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (primaryText == Color(0xFFE2E2E6)) Color.White else Color(0xFF1A1C1E)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Cycle Icon",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cycle Look",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Elegant dynamic dot page indicator centered
                val currentOotdIdx = ootdIdx % 3
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.rotateOotd() },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (currentOotdIdx) {
                            0 -> "●   ○   ○"
                            1 -> "○   ●   ○"
                            else -> "○   ○   ●"
                        },
                        fontSize = 16.sp,
                        color = if (primaryText == Color(0xFFE2E2E6)) Color(0xFFA8D1C2) else Color(0xFF2E4E42),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}



@Composable
fun ColourPaletteBlock(
    viewModel: MareViewModel,
    palette: com.example.ui.viewmodel.ColourPalette,
    cardBg: Color,
    borderColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val hasCustomPalette = !userProfile?.customPaletteName.isNullOrEmpty()

    var quizStep by remember { mutableStateOf(0) }
    val selectedAnswers = remember { mutableStateListOf<String>() }

    // Quiz Questions Definition
    val questions = listOf(
        "1. What is your skin's natural undertone?" to listOf(
            "Cool (Pinkish/blue veins, silver looks radiant)" to "Cool",
            "Warm (Golden/peach undertone, gold adds luster)" to "Warm",
            "Neutral (Veins appear blue-green, both metals harmonize)" to "Neutral",
            "Olive (Warm greenish sheen, earthy tones look beautiful)" to "Olive"
        ),
        "2. How does your skin typically react to sun exposure?" to listOf(
            "Sensitive (Burns almost immediately, rarely tans)" to "Sensitive",
            "Fair-Medium (Burns first, then tans gradually)" to "Fair-Medium",
            "Warm-Tanned (Tans very easily, rarely experiences sunburn)" to "Warm-Tanned",
            "Deep-Melanin (Tans deeply, never experiences burns)" to "Rich-Deep"
        ),
        "3. What best describes your natural eye & hair contrast?" to listOf(
            "Soft & Muted (Low contrast, hazel/light-brown eyes, soft amber hair)" to "Muted",
            "High Contrast (Dark hair paired with striking fair eyes or light features)" to "Contrast",
            "Earthy & Warm (Auburn/coppery highlights, warm amber/brown eyes)" to "Earthy",
            "Rich & Intense (Deep obsidian/dark eyes, dark hair, high-density pigment)" to "Deep"
        ),
        "4. Which seasonal natural landscape inspires you most?" to listOf(
            "Nordic Frost (Frosty mint green, ice blue, cool slate grey)" to "Nordic",
            "Tuscan Clay (Warm terracotta, sun-baked sand gold, soft almond)" to "Tuscan",
            "Velvet Orchid (Deep midnight orchid, royal burgundy, warm rose cream)" to "Velvet",
            "Forest Dew (Organic sage green, wet moss stone, ivory bone)" to "Forest"
        )
    )

    if (quizStep == 5) {
        // Immersive analysis transition
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            val essence = selectedAnswers.getOrNull(3) ?: "Forest"
            
            val (name, colorsCsv, justification) = when (essence) {
                "Nordic" -> Triple(
                    "Nordic Frost Palette",
                    "#4A5B5C,#87A797,#F4F7F6,#1E2526",
                    "A crisp, high-fashion winter-inspired palette reflecting cool silver undertones and nordic simplicity."
                )
                "Tuscan" -> Triple(
                    "Tuscan Terra Palette",
                    "#C27D38,#E3C498,#FAF5EF,#382A1B",
                    "A warm, radiant Mediterranean palette highlighting golden sunlit reflections and rich clay undertones."
                )
                "Velvet" -> Triple(
                    "Imperial Velvet Palette",
                    "#6B1D2F,#A37B85,#FAF6F7,#201115",
                    "A high-contrast, regal palette combining deep burgundy and rich dark shades that elevate your strong natural features."
                )
                else -> Triple(
                    "Forest Sage Palette",
                    "#42655F,#94B0A7,#FAF9F7,#1D2623",
                    "An earthy, harmonious palette that beautifully balances soft forest greens and warm matte sand tones."
                )
            }
            viewModel.saveCustomPalette(name, colorsCsv, justification)
            quizStep = 0
            selectedAnswers.clear()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personalised Colour Space",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = secondaryText,
                    letterSpacing = 0.5.sp
                )
                
                if (hasCustomPalette && quizStep == 0) {
                    Text(
                        text = "CUSTOM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                } else if (quizStep in 1..4) {
                    Text(
                        text = "STEP $quizStep OF 4",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when {
                quizStep == 0 && !hasCustomPalette -> {
                    // Quiz Intro State
                    Text(
                        text = "Discover Your True Colors",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Take our 4-step color analysis quiz to discover the custom color season that highlights your natural skin undertone. Once completed, your app's theme will automatically morph into your unique color aura!",
                        fontSize = 12.sp,
                        color = secondaryText,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            selectedAnswers.clear()
                            quizStep = 1
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Begin Color Analysis", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                quizStep in 1..4 -> {
                    // Quiz Active Steps
                    val currentQuestionPair = questions[quizStep - 1]
                    val questionText = currentQuestionPair.first
                    val optionsList = currentQuestionPair.second

                    Text(
                        text = questionText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        optionsList.forEach { (optionLabel, optionVal) ->
                            Button(
                                onClick = {
                                    selectedAnswers.add(optionVal)
                                    quizStep += 1
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Text(
                                    text = optionLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    // Linear progress indicator
                    LinearProgressIndicator(
                        progress = { quizStep / 4f },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = borderColor.copy(alpha = 0.3f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }

                quizStep == 5 -> {
                    // Analysis loader state
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Analyzing undertone profiles...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = primaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Harmonizing with seasonal color dimensions",
                            fontSize = 11.sp,
                            color = secondaryText
                        )
                    }
                }

                else -> {
                    // Completed / Custom Palette displayed
                    Text(
                        text = palette.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Colored Palette Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        palette.colors.forEach { (name, hex) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(45.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(hex)))
                                        .border(1.dp, borderColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = name,
                                    fontSize = 10.sp,
                                    color = primaryText,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = palette.justification,
                        fontSize = 12.sp,
                        color = secondaryText,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedAnswers.clear()
                                quizStep = 1
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retake Quiz", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.clearCustomPalette()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reset Theme", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchSortShortcut(
    cardBg: Color,
    borderColor: Color,
    primaryText: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(cardBg, RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = primaryText.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Search high-end aggregator & filter...",
                fontSize = 13.sp,
                color = primaryText.copy(alpha = 0.6f)
            )
        }
        Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = "Sort Options",
            tint = primaryText.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ChatbotWidget(
    viewModel: MareViewModel,
    cardBg: Color,
    borderColor: Color,
    primaryText: Color,
    secondaryText: Color,
    isDark: Boolean
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val isHighThinking by viewModel.isHighThinking.collectAsState()
    val isLowLatency by viewModel.isLowLatency.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF54C3A3))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Móda - AI Styling & Skincare Expert",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                }
                IconButton(onClick = { viewModel.clearChat() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Clear Chat",
                        tint = secondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = "Maintain styling and skincare context with our high thinking neural intelligence.",
                fontSize = 11.sp,
                color = secondaryText,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Config Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // High Thinking Toggle
                FilterChip(
                    selected = isHighThinking,
                    onClick = { viewModel.toggleHighThinking(!isHighThinking) },
                    label = {
                        Text(
                            text = "High Thinking",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2C4A4A),
                        selectedLabelColor = Color(0xFFD6F3F3)
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Thinking Mode",
                            modifier = Modifier.size(12.dp)
                        )
                    }
                )

                // Low Latency Toggle
                FilterChip(
                    selected = isLowLatency,
                    onClick = { viewModel.toggleLowLatency(!isLowLatency) },
                    label = {
                        Text(
                            text = "Low Latency",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4C402E),
                        selectedLabelColor = Color(0xFFFBE7CE)
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Low Latency",
                            modifier = Modifier.size(12.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chats Thread (Limited window inside home widget)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(if (isDark) Color(0xFF0F1515) else Color(0xFFFFFFFF), RoundedCornerShape(12.dp))
                    .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Empty",
                                tint = secondaryText.copy(alpha = 0.4f),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ask anything about styling, drapes,\nor skincare barrier routines...",
                                fontSize = 12.sp,
                                color = secondaryText.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val listState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(listState)
                    ) {
                        messages.forEach { msg ->
                            val align = if (msg.role == "user") Alignment.End else Alignment.Start
                            val bubbleColor = if (msg.role == "user") {
                                if (isDark) Color(0xFF233B3B) else Color(0xFFDCEFEF)
                            } else {
                                if (isDark) Color(0xFF1E2424) else Color(0xFFF1EDE9)
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = align
                            ) {
                                Box(
                                    modifier = Modifier
                                        .widthIn(max = 240.dp)
                                        .background(bubbleColor, RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = msg.text,
                                            fontSize = 12.sp,
                                            color = primaryText
                                        )
                                        if (msg.role == "assistant") {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Model: ${msg.modelUsed}",
                                                fontSize = 8.sp,
                                                color = secondaryText,
                                                fontWeight = FontWeight.Light
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        LaunchedEffect(messages.size) {
                            listState.animateScrollTo(listState.maxValue)
                        }
                    }
                }

                if (isChatLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x70000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF54C3A3),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick preset chips to fast tap questions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PresetQueryChip("Suggest OOTD drape", isDark) {
                    viewModel.sendChatMessage("Suggest a custom outfit drape combination based on my body shape profile.")
                }
                PresetQueryChip("How is humidity today?", isDark) {
                    viewModel.sendChatMessage("How should my skincare formulation respond to today's moisture levels?")
                }
                PresetQueryChip("What matches Sage colors?", isDark) {
                    viewModel.sendChatMessage("Which color drapes blend perfectly with Sage and Slate for my olive skin-tone?")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Text box & speech input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Consult Móda...", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF54C3A3),
                        unfocusedBorderColor = borderColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        // Simulated speech recording mic that triggers transcription!
                        IconButton(onClick = {
                            coroutineScope.launch {
                                // Simulate sending standard recorded speech bytes (encoded wav) to gemini-3.5-flash
                                val mockAudioBase64 = "UklGRiQAAABXQVZFZm10IBAAAAABAAEARKwAAIhYAQACABAAZGF0YQAAAAA=" // Mock header
                                viewModel.transcribeAndSendAudio(mockAudioBase64)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = if (viewModel.isAudioInputTranscribing.collectAsState().value) Color.Red else secondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = {
                        if (textInput.isNotEmpty()) {
                            viewModel.sendChatMessage(textInput)
                            textInput = ""
                        }
                    },
                    containerColor = Color(0xFF2F4F4F),
                    contentColor = Color.White,
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PresetQueryChip(text: String, isDark: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDark) Color(0xFF142020) else Color(0xFFE9E4DE))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = text, fontSize = 10.sp, color = if (isDark) Color(0xFFA5B8B8) else Color(0xFF3F4E4E))
    }
}

@Composable
fun AiAdaptiveRecommendationsCard(
    viewModel: MareViewModel,
    cardBg: Color,
    borderColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    val stylingTip by viewModel.aiStylingRecommendation.collectAsState()
    val skincareTip by viewModel.aiSkincareRecommendation.collectAsState()
    val isLoading by viewModel.isAiLoading.collectAsState()
    val weatherState by viewModel.weather.collectAsState()
    val isDark by viewModel.isThemeDark.collectAsState()

    var activeTab by remember { mutableStateOf("styling") } // "styling" or "skincare"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .fadeInUpOnMount(duration = 600, delayMillis = 200)
            .hoverScaleAndShadow(shape = RoundedCornerShape(24.dp))
            .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header with AI indicator & Refresh Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI Powered",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Real-Time AI Recommendations",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = secondaryText,
                        letterSpacing = 0.5.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.fetchAiStylingAndSkincareRecommendations(weatherState.city) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh weather and AI recommendations",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Info bar showing what weather metrics were used
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Environment: ${weatherState.city}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${weatherState.temperature}°C | ${weatherState.humidity}% Humidity",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Tab Bar (M3 custom pill tabs)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(borderColor.copy(alpha = 0.2f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Styling Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (activeTab == "styling") MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeTab = "styling" },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Styling tab icon",
                            tint = if (activeTab == "styling") Color.White else secondaryText,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Luxury Styling",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (activeTab == "styling") Color.White else secondaryText
                        )
                    }
                }

                // Skincare Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (activeTab == "skincare") MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeTab = "skincare" },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Skincare tab icon",
                            tint = if (activeTab == "skincare") Color.White else secondaryText,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Active Skincare",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (activeTab == "skincare") Color.White else secondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body content area
            if (isLoading) {
                com.example.ui.components.LoadingRecommendationsSkeleton(isDark = isDark)
            } else {
                val currentText = if (activeTab == "styling") stylingTip else skincareTip
                val currentIcon = if (activeTab == "styling") Icons.Default.Palette else Icons.Default.Face

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = currentIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (activeTab == "styling") "Móda's Seasonal Styling Guide" else "Móda's Adaptive Skincare Formulation",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentText,
                            fontSize = 12.sp,
                            color = secondaryText,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

data class LayerItem(
    val level: String,
    val title: String,
    val fabric: String,
    val description: String,
    val colorName: String,
    val colorHex: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun WeatherAdaptiveLayeringGrid(
    weatherState: com.example.ui.viewmodel.WeatherState,
    palette: com.example.ui.viewmodel.ColourPalette,
    cardBg: Color,
    borderColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    val colors = palette.colors
    val baseColor = colors.getOrNull(1) ?: ("Subtle Accent" to "#87A797")
    val midColor = colors.getOrNull(0) ?: ("Primary Accent" to "#C8E3D4")
    val outerColor = colors.getOrNull(3) ?: ("Deep Shade" to "#4A5B5C")
    val accentColor = colors.getOrNull(2) ?: ("Light Tint" to "#F9F8F6")

    val items = remember(weatherState, palette) {
        when {
            weatherState.temperature < 16.0 -> listOf(
                LayerItem("BASE LAYER", "Thermal Silk Slip", "Mulberry silk & fine thermal-knit", "Insulates core body temperature beautifully.", baseColor.first, baseColor.second, Icons.Default.FilterList),
                LayerItem("MID LAYER", "Cashmere Rib Knit", "100% fine grade Mongolian cashmere", "Maintains warm air pockets around the chest.", midColor.first, midColor.second, Icons.Default.Layers),
                LayerItem("OUTER LAYER", "Double-Faced Coat", "Heavyweight organic virgin wool weave", "Blocks cold winds while draping fluidly.", outerColor.first, outerColor.second, Icons.Default.Thermostat),
                LayerItem("ACCENT PIECE", "Chunky Alpaca Scarf", "Handwoven soft Peruvian baby alpaca", "Adds luxurious warmth without any bulk.", accentColor.first, accentColor.second, Icons.Default.AcUnit)
            )
            weatherState.humidity > 75 -> listOf(
                LayerItem("BASE LAYER", "Luxe Silk-Cotton Slip", "Mulberry silk & long-staple cotton", "Ultra-soft and 100% breathable against skin.", baseColor.first, baseColor.second, Icons.Default.FilterList),
                LayerItem("MID LAYER", "Asymmetric Linen Wrap", "Organic Belgian flax linen-cotton weave", "Encourages moisture evaporation and air-flow.", midColor.first, midColor.second, Icons.Default.Layers),
                LayerItem("OUTER LAYER", "Sheer Bamboo Duster", "Raw textured sustainable bamboo yarn", "Drapes like a dream while shielding solar rays.", outerColor.first, outerColor.second, Icons.Default.Waves),
                LayerItem("ACCENT PIECE", "Silk Square Twilly", "Ethereal featherweight pure twill weave", "Moisture-resistant chic accent for humidity.", accentColor.first, accentColor.second, Icons.Default.Spa)
            )
            else -> listOf(
                LayerItem("BASE LAYER", "Tencel Modal Tank", "High-density fine breathable knit", "Maintains silky second-skin dryness and ease.", baseColor.first, baseColor.second, Icons.Default.FilterList),
                LayerItem("MID LAYER", "Crisp Cotton Poplin", "Egyptian long-staple organic poplin", "Structure-focused comfort, easily styled open.", midColor.first, midColor.second, Icons.Default.Layers),
                LayerItem("OUTER LAYER", "Fluid Tencel Trench", "Regenerated wood pulp silky tencel", "Super lightweight breeze shield with fluid drape.", outerColor.first, outerColor.second, Icons.Default.Star),
                LayerItem("ACCENT PIECE", "Lightweight Flax Wrap", "Premium fine-gauge woven pure flax", "Protects shoulders on sunset walks comfortably.", accentColor.first, accentColor.second, Icons.Default.Spa)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "WEATHER-ADAPTIVE LAYERING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Suggested Outfit System",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (weatherState.temperature < 16.0) "COZY SHIELD" else if (weatherState.humidity > 75) "HUMIDITY DRAPE" else "TEMPERATE MODAL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2x2 Grid Layout constructed using Column and Rows
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // First Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    LayerGridCard(item = items[0], index = 0, cardBg = cardBg, borderColor = borderColor, primaryText = primaryText, secondaryText = secondaryText)
                }
                Box(modifier = Modifier.weight(1f)) {
                    LayerGridCard(item = items[1], index = 1, cardBg = cardBg, borderColor = borderColor, primaryText = primaryText, secondaryText = secondaryText)
                }
            }

            // Second Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    LayerGridCard(item = items[2], index = 2, cardBg = cardBg, borderColor = borderColor, primaryText = primaryText, secondaryText = secondaryText)
                }
                Box(modifier = Modifier.weight(1f)) {
                    LayerGridCard(item = items[3], index = 3, cardBg = cardBg, borderColor = borderColor, primaryText = primaryText, secondaryText = secondaryText)
                }
            }
        }
    }
}

@Composable
fun LayerGridCard(
    item: LayerItem,
    index: Int,
    cardBg: Color,
    borderColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    val animatedAlpha = remember { androidx.compose.animation.core.Animatable(0f) }
    val animatedOffsetY = remember { androidx.compose.animation.core.Animatable(40f) }

    LaunchedEffect(key1 = item) {
        animatedAlpha.snapTo(0f)
        animatedOffsetY.snapTo(40f)
        
        kotlinx.coroutines.delay((index * 120).toLong())
        
        launch {
            animatedAlpha.animateTo(
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 400,
                    easing = androidx.compose.animation.core.LinearOutSlowInEasing
                )
            )
        }
        launch {
            animatedOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 400,
                    easing = androidx.compose.animation.core.LinearOutSlowInEasing
                )
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = animatedAlpha.value
                translationY = animatedOffsetY.value
            }
            .border(1.dp, borderColor.copy(alpha = 0.7f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Level Pill & Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.level,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )

                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = item.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = primaryText,
                maxLines = 1
            )

            // Fabric
            Text(
                text = item.fabric,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Description
            Text(
                text = item.description,
                fontSize = 11.sp,
                color = secondaryText,
                lineHeight = 15.sp,
                minLines = 2,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Harmonizing Color Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.03f))
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(item.colorHex)))
                        .border(1.dp, borderColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.colorName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = primaryText,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun WeatherRoutineAdjustmentsWidget(
    weatherState: com.example.ui.viewmodel.WeatherState,
    isDark: Boolean,
    cardBg: Color,
    borderColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    val temp = weatherState.temperature
    val hum = weatherState.humidity
    
    val fashionAdjustment = when {
        temp < 15.0 -> "The chilly climate (${temp.toInt()}°C) requires insulated layers. Swap thin cottons for a structured merino knit, wool-blend trousers, and a cashmere wrap to shield against cold winds."
        temp > 25.0 -> "Under the warm sky (${temp.toInt()}°C), swap heavy denim and synthetic fabrics for fluid silk-linen, airy tencel drapes, or unstructured organic cotton pieces."
        else -> "Moderate temperatures (${temp.toInt()}°C) call for transitional layering. Style with a lightweight cotton canvas trench or an unlined relaxed blazer over a fine silk slip."
    }
    
    val skincareAdjustment = when {
        hum > 75 -> "High moisture levels (${hum}%) elevate trans-epidermal hydration. Swap rich lipid creams for a lightweight water-gel or hyaluronic acid serum, sealed with oil-regulating niacinamide."
        hum < 45 -> "Dry atmospheric air (${hum}% humidity) risks surface dehydration. Integrate barrier-supporting ceramides and lipid-dense squalane oil, finishing with a high-protection mineral SPF."
        else -> "Balanced humidity (${hum}%) is ideal. Maintain skin barrier equilibrium using a classic panthenol emulsion and daily zinc oxide SPF to defend against UV and environmental particulates."
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .fadeInUpOnMount(duration = 600, delayMillis = 150)
            .hoverScaleAndShadow(shape = RoundedCornerShape(24.dp))
            .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = "Atmospheric Adjustments Guide",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Atmospheric Routine Adjustments",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = secondaryText,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Style Adjustment Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Fashion Routine",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Fashion Adjustments",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = fashionAdjustment,
                            fontSize = 12.sp,
                            color = secondaryText,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Skincare Adjustment Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Skincare Routine",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Skincare Adjustments",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = skincareAdjustment,
                            fontSize = 12.sp,
                            color = secondaryText,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StylingAssistantWidget(
    viewModel: MareViewModel,
    isDark: Boolean,
    cardBg: Color,
    borderColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    val stylingAssistantOutfit by viewModel.stylingAssistantOutfit.collectAsState()
    val isStylingAssistantLoading by viewModel.isStylingAssistantLoading.collectAsState()
    
    var customMood by remember { mutableStateOf("") }
    val preSelectedMoods = listOf("Minimalist Chic", "Avant-Garde", "Cozy Lounge", "Earthy Boho")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .fadeInUpOnMount(duration = 600, delayMillis = 250)
            .hoverScaleAndShadow(shape = RoundedCornerShape(24.dp))
            .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Styling Assistant",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Editorial Styling Assistant",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = secondaryText,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            
            Text(
                text = "Mood-Curated Combinations",
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = primaryText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Enter or select a fashion mood. Móda will combine it with today's real-time atmospheric conditions to design a tailored luxury look.",
                fontSize = 12.sp,
                color = secondaryText,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                preSelectedMoods.forEach { mood ->
                    val isSelected = customMood.lowercase() == mood.lowercase()
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else borderColor.copy(alpha = 0.2f)
                            )
                            .clickable {
                                customMood = mood
                                viewModel.generateStylingAssistantOutfit(mood)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = mood,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) Color.White else secondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = customMood,
                onValueChange = { customMood = it },
                placeholder = { Text("E.g., Retro Riviera, Quiet Luxury, Cyber-punk...", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = borderColor,
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg
                ),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    if (customMood.isNotBlank() && !isStylingAssistantLoading) {
                        IconButton(
                            onClick = { viewModel.generateStylingAssistantOutfit(customMood) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Generate Outfit",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            )

            if (isStylingAssistantLoading) {
                Spacer(modifier = Modifier.height(20.dp))
                com.example.ui.components.LoadingCardSkeleton(isDark = isDark)
            } else {
                stylingAssistantOutfit?.let { outfitText ->
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MÓDA'S CURATION",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.5.sp
                                )
                                
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            val lines = outfitText.split("\n")
                            lines.forEach { line ->
                                if (line.trim().isNotBlank()) {
                                    val isHeaderLine = line.trim().endsWith(":") || 
                                                       line.trim().startsWith("The ") && line.trim().endsWith("Ensemble") ||
                                                       line.trim().lowercase().contains("silhouette") ||
                                                       line.trim().lowercase().contains("palette") ||
                                                       line.trim().lowercase().contains("note")
                                    
                                    if (isHeaderLine) {
                                        Text(
                                            text = line.replace(":", "").trim(),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Serif,
                                            color = primaryText,
                                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                                        )
                                    } else {
                                        Text(
                                            text = line.trim(),
                                            fontSize = 12.sp,
                                            color = secondaryText,
                                            lineHeight = 18.sp,
                                            modifier = Modifier.padding(bottom = 4.dp)
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

