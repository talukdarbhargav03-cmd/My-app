package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClosetItemEntity
import com.example.data.UserProfileEntity
import com.example.ui.viewmodel.MareViewModel

@Composable
fun StyleProfileQuizWidget(
    viewModel: MareViewModel,
    isDark: Boolean,
    cardBg: Color,
    borderColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    val userProfileState by viewModel.userProfile.collectAsState()
    val userProfile = userProfileState ?: UserProfileEntity()
    val hasCompletedQuiz = userProfile.hasCompletedQuiz

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
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Style Quiz",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MARE STYLE PROFILE CURATOR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (hasCompletedQuiz) {
                // Quiz completed view
                Text(
                    text = "Your Curated Aesthetic",
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = primaryText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Móda AI suggestions are now dynamically personalized with your quiz parameters.",
                    fontSize = 12.sp,
                    color = secondaryText,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StyleProfileAttributeItem(
                        icon = Icons.Default.Palette,
                        title = "Atmospheric Tones",
                        value = userProfile.favoriteColors,
                        primaryText = primaryText,
                        secondaryText = secondaryText
                    )
                    StyleProfileAttributeItem(
                        icon = Icons.Default.TrendingUp,
                        title = "Silhouette Preference",
                        value = userProfile.preferredSilhouettes,
                        primaryText = primaryText,
                        secondaryText = secondaryText
                    )
                    StyleProfileAttributeItem(
                        icon = Icons.Default.Face,
                        title = "Primary Skin Goals",
                        value = userProfile.skinConcerns,
                        primaryText = primaryText,
                        secondaryText = secondaryText
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = { viewModel.resetStyleQuiz() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, borderColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryText)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retake Curated Quiz", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                // Quiz interactive steps
                var currentStep by remember { mutableStateOf(1) }
                
                // Temp step values
                var selectedColors by remember { mutableStateOf("Warm Neutrals (Oatmeal, Cream, Taupe)") }
                var selectedSilhouette by remember { mutableStateOf("Fluid & Relaxed (wide-leg trousers, loose tunics)") }
                var selectedSkinConcerns by remember { mutableStateOf("Dehydration & Flakiness") }

                val colorOptions = listOf(
                    "Warm Neutrals (Oatmeal, Cream, Taupe)",
                    "Cold Minimal (Slate, Charcoal, White)",
                    "Rich Earthy (Terracotta, Sage, Olive)",
                    "Monochrome Noir (Jet Black, Chalk White)"
                )

                val silhouetteOptions = listOf(
                    "Fluid & Relaxed (wide-leg trousers, loose tunics)",
                    "Tailored & Structured (blazers, pleated pants)",
                    "A-line & Voluminous (midi skirts, cocoon coats)",
                    "Minimal & Form-fitting"
                )

                val skinConcernsOptions = listOf(
                    "Dehydration & Flakiness",
                    "Sensitivity & Redness",
                    "Oiliness & Large Pores",
                    "Dullness & Hyperpigmentation"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Curate Style Profile",
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    Text(
                        text = "Step $currentStep of 3",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { currentStep / 3f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = borderColor.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(18.dp))

                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        slideInHorizontally { width -> if (targetState > initialState) width else -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> if (targetState > initialState) -width else width } + fadeOut()
                    },
                    label = "quizStepAnimation"
                ) { step ->
                    Column {
                        when (step) {
                            1 -> {
                                Text(
                                    text = "Which palette resonates with your styling vision?",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = primaryText,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                colorOptions.forEach { option ->
                                    val isSelected = selectedColors == option
                                    QuizOptionRow(
                                        text = option,
                                        isSelected = isSelected,
                                        borderColor = borderColor,
                                        primaryText = primaryText,
                                        onClick = { selectedColors = option }
                                    )
                                }
                            }
                            2 -> {
                                Text(
                                    text = "What silhouette contours make you feel most confident?",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = primaryText,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                silhouetteOptions.forEach { option ->
                                    val isSelected = selectedSilhouette == option
                                    QuizOptionRow(
                                        text = option,
                                        isSelected = isSelected,
                                        borderColor = borderColor,
                                        primaryText = primaryText,
                                        onClick = { selectedSilhouette = option }
                                    )
                                }
                            }
                            3 -> {
                                Text(
                                    text = "What is your primary atmospheric skin barrier goal?",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = primaryText,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                skinConcernsOptions.forEach { option ->
                                    val isSelected = selectedSkinConcerns == option
                                    QuizOptionRow(
                                        text = option,
                                        isSelected = isSelected,
                                        borderColor = borderColor,
                                        primaryText = primaryText,
                                        onClick = { selectedSkinConcerns = option }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, borderColor),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryText)
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Back", fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep < 3) {
                                currentStep++
                            } else {
                                // Save & Complete quiz
                                viewModel.saveStyleQuiz(
                                    name = userProfile.name,
                                    favoriteColors = selectedColors,
                                    stylePreference = userProfile.stylePreference,
                                    bodyShape = userProfile.bodyShape,
                                    clothingSize = userProfile.clothingSize,
                                    preferredSilhouettes = selectedSilhouette,
                                    skinConcerns = selectedSkinConcerns,
                                    skinTone = userProfile.skinTone
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (currentStep == 3) "Finish & Save" else "Continue",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentStep < 3) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizOptionRow(
    text: String,
    isSelected: Boolean,
    borderColor: Color,
    primaryText: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                else Color.Transparent
            )
            .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary
                else borderColor,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = borderColor
                ),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = primaryText
            )
        }
    }
}

@Composable
fun StyleProfileAttributeItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    primaryText: Color,
    secondaryText: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(primaryText.copy(alpha = 0.03f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Column {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = secondaryText,
                letterSpacing = 0.5.sp
            )
            Text(
                text = value,
                fontSize = 13.sp,
                color = primaryText,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalClosetWidget(
    viewModel: MareViewModel,
    isDark: Boolean,
    cardBg: Color,
    borderColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    val closetItems by viewModel.closetItems.collectAsState()
    
    var showAddForm by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val categories = listOf("All", "Outerwear", "Tops", "Bottoms", "Shoes", "Accessories")

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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = "Digital Closet",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "YOUR DIGITAL CLOSET",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }
                
                Text(
                    text = "${closetItems.size} ${if (closetItems.size == 1) "Item" else "Items"}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = secondaryText
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Archived Wardrobe",
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = primaryText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Hang items from your real-world closet. Móda AI will seamlessly integrate them into future outfit recommendations.",
                fontSize = 12.sp,
                color = secondaryText,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Categories horizontal scroller
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else borderColor.copy(alpha = 0.2f)
                            )
                            .clickable { selectedCategoryFilter = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) Color.White else secondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Form Toggle button / Form Area
            AnimatedVisibility(
                visible = showAddForm,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(primaryText.copy(alpha = 0.02f))
                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "HANG A NEW PIECE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    var itemName by remember { mutableStateOf("") }
                    var itemCategory by remember { mutableStateOf("Tops") }
                    var itemColor by remember { mutableStateOf("") }
                    var itemMaterial by remember { mutableStateOf("") }
                    var itemNotes by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Item Name (e.g. Silk Drape Trench)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category radio pills
                    Text("Category", fontSize = 11.sp, color = secondaryText, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.filter { it != "All" }.forEach { cat ->
                            val isSelected = itemCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        else Color.Transparent
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else borderColor,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { itemCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else secondaryText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = itemColor,
                            onValueChange = { itemColor = it },
                            label = { Text("Color (e.g. Oatmeal)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                        )
                        OutlinedTextField(
                            value = itemMaterial,
                            onValueChange = { itemMaterial = it },
                            label = { Text("Material (e.g. Linen)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = itemNotes,
                        onValueChange = { itemNotes = it },
                        label = { Text("Optional Notes (e.g. oversized fit)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddForm = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, borderColor),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryText)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (itemName.isNotBlank() && itemColor.isNotBlank()) {
                                    viewModel.addClosetItem(
                                        name = itemName,
                                        category = itemCategory,
                                        color = itemColor,
                                        material = itemMaterial,
                                        notes = itemNotes
                                    )
                                    showAddForm = false
                                }
                            },
                            enabled = itemName.isNotBlank() && itemColor.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Hang Piece", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (!showAddForm) {
                Button(
                    onClick = { showAddForm = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Clothing Piece", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // List of items
            val filteredItems = if (selectedCategoryFilter == "All") closetItems 
                                else closetItems.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DryCleaning,
                            contentDescription = null,
                            tint = secondaryText.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedCategoryFilter == "All") "Your closet is currently empty." else "No $selectedCategoryFilter pieces found.",
                            fontSize = 12.sp,
                            color = secondaryText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    filteredItems.forEach { item ->
                        ClosetItemRow(
                            item = item,
                            borderColor = borderColor,
                            primaryText = primaryText,
                            secondaryText = secondaryText,
                            onDelete = { viewModel.deleteClosetItem(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClosetItemRow(
    item: ClosetItemEntity,
    borderColor: Color,
    primaryText: Color,
    secondaryText: Color,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(primaryText.copy(alpha = 0.03f))
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (item.category.lowercase()) {
                    "outerwear" -> Icons.Default.Checkroom
                    "tops" -> Icons.Default.DryCleaning
                    "bottoms" -> Icons.Default.LocalMall
                    "shoes" -> Icons.Default.DirectionsWalk
                    else -> Icons.Default.Style
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Column {
                Text(
                    text = item.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.category,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Text(
                        text = "${item.color} • ${if (item.material.isNotBlank()) item.material else "Organic Fabric"}",
                        fontSize = 11.sp,
                        color = secondaryText
                    )
                }
            }
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Remove item",
                tint = Color(0xFFCC5C5C),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
