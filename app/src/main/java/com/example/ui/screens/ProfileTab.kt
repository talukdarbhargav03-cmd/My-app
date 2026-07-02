package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfileEntity
import com.example.ui.viewmodel.MareViewModel
import com.example.ui.components.fadeInUpOnMount
import com.example.ui.components.StyleProfileQuizWidget
import com.example.ui.components.DigitalClosetWidget
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.widget.Toast

@Composable
fun ProfileTab(
    viewModel: MareViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isThemeDark.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val weatherState by viewModel.weather.collectAsState()
    val activePlanId by viewModel.activePlanId.collectAsState()

    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBgColor = MaterialTheme.colorScheme.surface
    val cardBorderColor = MaterialTheme.colorScheme.outlineVariant

    // Form inputs state
    var editMode by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(userProfile?.name ?: "Maya") }
    var skinToneInput by remember { mutableStateOf(userProfile?.skinTone ?: "Medium Olive") }
    var bodyShapeInput by remember { mutableStateOf(userProfile?.bodyShape ?: "Hourglass") }
    var stylePreferenceInput by remember { mutableStateOf(userProfile?.stylePreference ?: "Minimal Luxury") }
    var locationInput by remember { mutableStateOf(userProfile?.location ?: "London") }
    var sizeInput by remember { mutableStateOf(userProfile?.clothingSize ?: "M") }
    var genderInput by remember { mutableStateOf(userProfile?.gender ?: "Female") }

    // Dialog state for tech specifications document
    var showTechSpecsDialog by remember { mutableStateOf(false) }

    // Dialog state for photo auth login simulation
    var showPhotoAuthSimulator by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val locationPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.changeLocationByCoordinates(location.latitude, location.longitude)
                        Toast.makeText(context, "Location updated successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 1000
                        ).setMaxUpdates(1).build()
                        
                        fusedLocationClient.requestLocationUpdates(
                            locationRequest,
                            object : com.google.android.gms.location.LocationCallback() {
                                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                                    val loc = result.lastLocation
                                    if (loc != null) {
                                        viewModel.changeLocationByCoordinates(loc.latitude, loc.longitude)
                                        Toast.makeText(context, "Precise location resolved!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Please ensure GPS/Location is enabled in settings.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            android.os.Looper.getMainLooper()
                        )
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(context, "Error detecting location: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (se: SecurityException) {
                Toast.makeText(context, "Permission error: ${se.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Location permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    val requestLocationDetection = {
        val fineCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fineCheck || coarseCheck) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.changeLocationByCoordinates(location.latitude, location.longitude)
                        Toast.makeText(context, "Location updated successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 1000
                        ).setMaxUpdates(1).build()
                        
                        fusedLocationClient.requestLocationUpdates(
                            locationRequest,
                            object : com.google.android.gms.location.LocationCallback() {
                                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                                    val loc = result.lastLocation
                                    if (loc != null) {
                                        viewModel.changeLocationByCoordinates(loc.latitude, loc.longitude)
                                        Toast.makeText(context, "Precise location resolved!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Ensure location service/GPS is active on device.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            android.os.Looper.getMainLooper()
                        )
                    }
                }
            } catch (se: SecurityException) {
                Toast.makeText(context, "Permission error: ${se.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            locationPermissionsLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    LaunchedEffect(userProfile, editMode) {
        if (!editMode && userProfile != null) {
            nameInput = userProfile?.name ?: "Maya"
            skinToneInput = userProfile?.skinTone ?: "Medium Olive"
            bodyShapeInput = userProfile?.bodyShape ?: "Hourglass"
            stylePreferenceInput = userProfile?.stylePreference ?: "Minimal Luxury"
            locationInput = userProfile?.location ?: "London"
            sizeInput = userProfile?.clothingSize ?: "M"
            genderInput = userProfile?.gender ?: "Female"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Profile Banner Header
        Text(
            text = "Your MARE Profile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Serif,
            color = primaryTextColor
        )
        Text(
            text = "Fine-tune measurements, locations, and styling parameters.",
            fontSize = 11.sp,
            color = secondaryTextColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Auth Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fadeInUpOnMount(duration = 500)
                .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (authState.isLoggedIn) Icons.Default.VerifiedUser else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (authState.isLoggedIn) Color(0xFF53A18C) else Color(0xFFCC8C5C),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (authState.isLoggedIn) "Authenticated Vault Active" else "Offline Sandbox Profile",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryTextColor
                        )
                        Text(
                            text = if (authState.isLoggedIn) "Logged in as ${authState.loggedInEmail}" else "No active user. Login with credentials or Photo.",
                            fontSize = 11.sp,
                            color = secondaryTextColor
                        )
                    }
                }

                if (authState.isLoggedIn) {
                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC5C5C)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Logout", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { showPhotoAuthSimulator = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C4A4A)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Login", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Technical Specs & Dev Roadmap CTA Button
        Button(
            onClick = { showTechSpecsDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29), RoundedCornerShape(24.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDark) Color(0xFF202D29) else Color(0xFFE2EBE6),
                contentColor = if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29)
            ),
            shape = RoundedCornerShape(24.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Assignment,
                contentDescription = "Specs Roadmap",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "View Technical Specs & Roadmap",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Theme Switcher Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBgColor, RoundedCornerShape(12.dp))
                .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFFDED0B6) else Color(0xFFBBAB8C),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Bone & Canvas Theme", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primaryTextColor)
                    Text(
                        text = if (isDark) "Active: Charcoal & Bone (Dark)" else "Active: Bone & Canvas (Default)",
                        fontSize = 11.sp,
                        color = secondaryTextColor
                    )
                }
            }
            Switch(
                checked = isDark,
                onCheckedChange = { viewModel.toggleTheme() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFFF5F2ED),
                    checkedTrackColor = Color(0xFFBBAB8C),
                    uncheckedThumbColor = Color(0xFF2C2C2C),
                    uncheckedTrackColor = Color(0xFFDED0B6)
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Subscription Eco Plans Card
        val regionalPlans = remember(userProfile?.location, weatherState) {
            getRegionalPlans(userProfile?.location, weatherState)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fadeInUpOnMount(duration = 550, delayMillis = 100)
                .border(1.dp, cardBorderColor, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Regional Curation Plans",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryTextColor
                        )
                        Text(
                            text = "Optimized for your local economy",
                            fontSize = 11.sp,
                            color = secondaryTextColor
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF54C3A3).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PPP ACTIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF54C3A3)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "To keep access global and fair, MARE automatically adjusts pricing and subscription tiers based on your country's purchasing power parity (PPP).",
                    fontSize = 11.sp,
                    color = secondaryTextColor,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                regionalPlans.forEach { plan ->
                    val isActive = activePlanId == plan.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(
                                if (isActive) Color(0xFF54C3A3).copy(alpha = 0.08f) else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = if (isActive) 1.5.dp else 1.dp,
                                color = if (isActive) Color(0xFF54C3A3) else cardBorderColor.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.selectPlan(plan.id) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = plan.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryTextColor
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (plan.suitableFor == "High-End Luxury") Color(0xFFD4AF37).copy(alpha = 0.15f)
                                                else Color.Gray.copy(alpha = 0.15f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = plan.suitableFor,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (plan.suitableFor == "High-End Luxury") Color(0xFFD4AF37) else secondaryTextColor
                                        )
                                    }
                                }
                                Text(
                                    text = plan.description,
                                    fontSize = 10.sp,
                                    color = secondaryTextColor,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    lineHeight = 13.sp
                                )
                                plan.features.take(2).forEach { feat ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF54C3A3),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = feat, fontSize = 9.sp, color = secondaryTextColor)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = plan.localPriceString,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isActive) Color(0xFF54C3A3) else Color(0xFFC48E5A)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { viewModel.selectPlan(plan.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isActive) Color(0xFF54C3A3) else cardBgColor,
                                        contentColor = if (isActive) Color.White else primaryTextColor
                                    ),
                                    border = BorderStroke(1.dp, if (isActive) Color.Transparent else cardBorderColor),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = if (isActive) "Active" else "Select",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Parameters Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fadeInUpOnMount(duration = 600, delayMillis = 200)
                .border(1.dp, cardBorderColor, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Styling Parameters",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor
                    )
                    IconButton(onClick = {
                        if (editMode) {
                            // Save profile
                            viewModel.updateProfile(
                                name = nameInput,
                                skinTone = skinToneInput,
                                bodyShape = bodyShapeInput,
                                stylePreference = stylePreferenceInput,
                                clothingSize = sizeInput,
                                gender = genderInput,
                                favoriteColors = userProfile?.favoriteColors ?: "Sage, Clay",
                                location = locationInput
                            )
                        }
                        editMode = !editMode
                    }) {
                        Icon(
                            imageVector = if (editMode) Icons.Default.Done else Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (editMode) {
                    // Profile Name
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Profile Name", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29))
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Skin tone selector
                    OutlinedTextField(
                        value = skinToneInput,
                        onValueChange = { skinToneInput = it },
                        label = { Text("Skin-Tone Undertone", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29))
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Body shape selector
                    OutlinedTextField(
                        value = bodyShapeInput,
                        onValueChange = { bodyShapeInput = it },
                        label = { Text("Body Shape Structure", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29))
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Style preference
                    OutlinedTextField(
                        value = stylePreferenceInput,
                        onValueChange = { stylePreferenceInput = it },
                        label = { Text("Style Preference", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29))
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Clothing size
                    OutlinedTextField(
                        value = sizeInput,
                        onValueChange = { sizeInput = it },
                        label = { Text("Clothing Size", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29))
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Gender Silhouette Base Selection
                    Text(
                        text = "Gender Silhouette Base",
                        fontSize = 11.sp,
                        color = secondaryTextColor,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Female", "Male", "Gender-Neutral").forEach { g ->
                            val selected = genderInput.equals(g, ignoreCase = true)
                            val chipBg = if (selected) {
                                if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29)
                            } else {
                                if (isDark) Color(0xFF2C2D30) else Color(0xFFF5F5F7)
                            }
                            val chipTextCol = if (selected) {
                                if (isDark) Color(0xFF121314) else Color.White
                            } else {
                                secondaryTextColor
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(chipBg)
                                    .clickable { genderInput = g }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = g,
                                    color = chipTextCol,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Location / City
                    OutlinedTextField(
                        value = locationInput,
                        onValueChange = { locationInput = it },
                        label = { Text("Active Location", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { requestLocationDetection() }) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Detect Location",
                                    tint = if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = if (isDark) Color(0xFFA8D1C2) else Color(0xFF202D29))
                    )
                } else {
                    ProfileParameterRow("Profile Name", userProfile?.name ?: "Maya", secondaryTextColor, primaryTextColor)
                    ProfileParameterRow("Skin-Tone Detect", userProfile?.skinTone ?: "Medium Olive", secondaryTextColor, primaryTextColor)
                    ProfileParameterRow("Body Shape Class", userProfile?.bodyShape ?: "Hourglass", secondaryTextColor, primaryTextColor)
                    ProfileParameterRow("Style Aesthetic", userProfile?.stylePreference ?: "Minimal Luxury", secondaryTextColor, primaryTextColor)
                    ProfileParameterRow("Clothing Size", userProfile?.clothingSize ?: "M", secondaryTextColor, primaryTextColor)
                    ProfileParameterRow("Gender Base", userProfile?.gender ?: "Female", secondaryTextColor, primaryTextColor)
                    ProfileParameterRow("Active Location", userProfile?.location ?: "London", secondaryTextColor, primaryTextColor) {
                        IconButton(
                            onClick = { requestLocationDetection() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Detect Location",
                                tint = if (isDark) Color(0xFF54C3A3) else Color(0xFF1B3835),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    ProfileParameterRow("Favorite Colors", userProfile?.favoriteColors ?: "Sage, Clay", secondaryTextColor, primaryTextColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        StyleProfileQuizWidget(
            viewModel = viewModel,
            isDark = isDark,
            cardBg = cardBgColor,
            borderColor = cardBorderColor,
            primaryText = primaryTextColor,
            secondaryText = secondaryTextColor
        )

        Spacer(modifier = Modifier.height(20.dp))

        DigitalClosetWidget(
            viewModel = viewModel,
            isDark = isDark,
            cardBg = cardBgColor,
            borderColor = cardBorderColor,
            primaryText = primaryTextColor,
            secondaryText = secondaryTextColor
        )

        Spacer(modifier = Modifier.height(100.dp)) // Offset
    }

    // --- DIALOG 1: Interactive Engineering Specs & Roadmap Document Reader ---
    if (showTechSpecsDialog) {
        AlertDialog(
            onDismissRequest = { showTechSpecsDialog = false },
            title = {
                Text(
                    text = "MARE - Technical Specs & Roadmap",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = primaryTextColor
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = """
                            MARE DEVELOPMENT ARCHITECTURE ROADMAP
                            
                            1. ARCHITECTURAL OVERVIEW
                            MARE is designed as an offline-first luxury skincare and fashion advisory application. It uses a clean, reactive architecture with single-source-of-truth Room Database synchronized with Google Cloud Firestore via Firebase Auth identifiers.
                            
                            - Core Language: Kotlin (Android SDK 36, Jetpack Compose UI)
                            - Cross-Platform Parity: High parity built with Jetpack Compose Multiplatform (Compose for iOS and Android), ensuring exactly the same design system, canvas wave headers, and card components render natively on both Apple UIKit and Google Jetpack surfaces.
                            
                            2. INTERACTIVE TECHNICAL SPECIFICATIONS
                            A. Advanced AI Personalization Engine:
                               - Multimodal Photo Analyser: Accepts face and body profile uploads. Sent via secure HTTPS streaming to Gemini 3.1 Pro API.
                               - Facial Contour and Skin-Tone Matching: Extract color arrays and structural dimensions (height, weight, hourglass geometry), caching in local SQLite SQLite DB.
                               - System Prompt Injection: Automatically includes cached body traits inside every chatbot conversational payload.
                               
                            B. Real-World API Integration:
                               - OpenWeatherMap Geolocation API fetches real-time humidity (%) and temperature (°C).
                               - Interactive "Skincare Formulation": Routine automatically alters based on moisture, introducing lighter fluid hydrators in high moisture and heavier lipid creams in dry environments.
                               - Dynamic Greeting Engine: Formulates conversational headlines depending on hour-blocks and weather metrics.
                               
                            C. E-Commerce Scraper Aggregator:
                               - Index Scraping API: Aggregates active prices across Farfetch, Net-A-Porter, and SSENSE.
                               - Client Side Logic: Instant sortable matrix from low-to-high, showing rating distributions and verified luxury reviews.
                               
                            3. STRATEGIC MILESTONE BREAKDOWNS
                            - MILESTONE 1 (Sprint 1-2): Brand Visual Identity & Dynamic Wave Engine setup (Jetpack Compose, Dynamic theme palettes).
                            - MILESTONE 2 (Sprint 3-4): Security & Photobiometric Analysis implementation. Photo Auth face register and compare vector pipelines.
                            - MILESTONE 3 (Sprint 5-6): Google Maps Grounding & E-commerce scraper API parsing.
                            - MILESTONE 4 (Sprint 7-8): Production deployment, Firebase App Check Recaptcha integration, App Store & Google Play launch.
                            
                            4. DATA STORAGE SCHEMA (FIRESTORE & ROOM)
                            Entity UserProfile:
                            - id: Int (Primary Key)
                            - skinTone: String
                            - bodyShape: String
                            - stylePreference: String
                            - location: String
                            - bioAuthPhotoBase64: String (FACIAL VECTORS)
                            
                            Entity SavedArticles / SavedProducts:
                            - id: String (Primary Key)
                            - title: String
                            - mainPrice: Double
                            - platforms: Map<String, Double>
                        """.trimIndent(),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = secondaryTextColor,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTechSpecsDialog = false }) {
                    Text("Dismiss Specs", color = Color(0xFF54C3A3))
                }
            },
            containerColor = if (isDark) Color(0xFF142020) else Color(0xFFF9F6F0)
        )
    }

    // --- DIALOG 2: User Credentials & "Log in with Photo" facial recognition setup and login simulator ---
    if (showPhotoAuthSimulator) {
        var isSetupMode by remember { mutableStateOf(!(userProfile?.bioAuthSetup ?: false)) }
        var credentialEmail by remember { mutableStateOf("") }
        var credentialPhone by remember { mutableStateOf("") }
        var credentialPass by remember { mutableStateOf("") }

        val isUploading by viewModel.isPhotoUploading.collectAsState()

        AlertDialog(
            onDismissRequest = { showPhotoAuthSimulator = false },
            title = {
                Text(
                    text = if (isSetupMode) "Register Facial Face Profile" else "MARE Advanced Entrance",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = primaryTextColor
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (isSetupMode) {
                        Text(
                            text = "Register your facial keypoints to configure 'Log in with Photo' face analysis.",
                            fontSize = 12.sp,
                            color = secondaryTextColor,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Simulator Camera box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF243030))
                                .drawBehind {
                                    drawCircle(
                                        color = Color(0x1F7FE2C9),
                                        radius = size.height * 0.4f,
                                        center = Offset(size.width / 2, size.height / 2)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(color = Color(0xFF54C3A3))
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = null,
                                        tint = Color(0xFF7FE2C9),
                                        modifier = Modifier.size(42.dp)
                                    )
                                    Text(
                                        text = "Camera Active [Simulator]",
                                        fontSize = 11.sp,
                                        color = Color(0xFF7FE2C9)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                // Simulate capturing a beautiful registration face photo (128x128 pixel bmp)
                                val bmp = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
                                viewModel.savePhotoAuthFace(bmp)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF54C3A3)),
                            enabled = !isUploading
                        ) {
                            Text("Capture Registration Photo", fontSize = 11.sp, color = Color(0xFF1B3835))
                        }

                        if (userProfile?.bioAuthSetup == true) {
                            Text(
                                text = "Facial profile is configured. You can now login using Photo.",
                                fontSize = 11.sp,
                                color = Color(0xFF53A18C),
                                modifier = Modifier.padding(top = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(
                            onClick = { isSetupMode = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Switch to Login Window", fontSize = 11.sp, color = Color(0xFF54C3A3))
                        }

                    } else {
                        // Login Window with Credentials & Photo login fallback
                        Text(
                            text = "Authenticate securely using credentials or Photo biometric analysis.",
                            fontSize = 11.sp,
                            color = secondaryTextColor,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = credentialEmail,
                            onValueChange = { credentialEmail = it },
                            placeholder = { Text("Email", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF54C3A3))
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = credentialPhone,
                            onValueChange = { credentialPhone = it },
                            placeholder = { Text("Phone Number", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF54C3A3))
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = credentialPass,
                            onValueChange = { credentialPass = it },
                            placeholder = { Text("Password (min 6 characters)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF54C3A3))
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val success = viewModel.authenticateWithCredentials(credentialEmail, credentialPhone, credentialPass)
                                if (success) {
                                    showPhotoAuthSimulator = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4F4F))
                        ) {
                            Text("Authenticate with Credentials", fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // PHOTO LOGIN CALL
                        Text(
                            text = "Or Log In with Photo:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryTextColor,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Button(
                            onClick = {
                                val loginBmp = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
                                viewModel.loginWithPhoto(loginBmp)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF54C3A3)),
                            enabled = (userProfile?.bioAuthSetup ?: false) && !isUploading
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Verify Photo Entrance (Bio)", fontSize = 11.sp, color = Color(0xFF1B3835))
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(
                            onClick = { isSetupMode = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Setup/Register Facial Signature", fontSize = 11.sp, color = Color(0xFF54C3A3))
                        }
                    }

                    if (authState.authStatusMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = authState.authStatusMessage,
                            fontSize = 11.sp,
                            color = if (authState.authStatusMessage.contains("Success") || authState.authStatusMessage.contains("Matched")) Color(0xFF53A18C) else Color(0xFFCC5C5C),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPhotoAuthSimulator = false }) {
                    Text("Cancel", color = Color(0xFF54C3A3))
                }
            },
            containerColor = if (isDark) Color(0xFF142020) else Color(0xFFF9F6F0)
        )
    }
}

@Composable
fun ProfileParameterRow(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = labelColor, fontWeight = FontWeight.Normal)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, fontSize = 13.sp, color = valueColor, fontWeight = FontWeight.SemiBold)
            if (action != null) {
                Spacer(modifier = Modifier.width(8.dp))
                action()
            }
        }
    }
    HorizontalDivider(color = labelColor.copy(alpha = 0.2f))
}

data class RegionalPlan(
    val id: String,
    val name: String,
    val localPriceString: String,
    val description: String,
    val features: List<String>,
    val suitableFor: String
)

fun getRegionalPlans(location: String?, weatherState: com.example.ui.viewmodel.WeatherState? = null): List<RegionalPlan> {
    val loc = (location ?: "London").lowercase()
    val country = (weatherState?.country ?: "").lowercase()
    val countryCode = (weatherState?.countryCode ?: "").lowercase()
    val city = (weatherState?.city ?: "").lowercase()

    val isIndia = loc.contains("india") || loc.contains("mumbai") || loc.contains("delhi") || loc.contains("inr") || loc.contains("dibrugarh") || loc.contains("assam")
    val isUk = loc.contains("london") || loc.contains("uk") || loc.contains("united kingdom") || loc.contains("gbp") || country.contains("united kingdom") || countryCode == "gb" || city.contains("london")
    val isJapan = loc.contains("tokyo") || loc.contains("japan") || loc.contains("jpy") || country.contains("japan") || countryCode == "jp" || city.contains("tokyo")
    val isEurope = loc.contains("paris") || loc.contains("milan") || loc.contains("europe") || loc.contains("france") || loc.contains("italy") || loc.contains("spain") || loc.contains("germany") || loc.contains("eur") || country.contains("france") || countryCode == "fr"

    return when {
        isIndia -> listOf(
            RegionalPlan("lite", "Lite Stylist", "Free", "Affordable entry-level local styling tips.", listOf("Weekly Outfit Recommendation", "Basic Skincare Audit", "Standard local store links"), "Budget Conscious"),
            RegionalPlan("elite", "MARE Elite Concierge", "Free", "Highly personalized regional fit advice with weather integrations.", listOf("Daily Weather-Adaptive Outfits", "Advanced AI Skincare Routines", "Local Nykaa Luxe/Ajio Luxe access"), "Daily Active Stylist"),
            RegionalPlan("infinite", "Infinite Wardrobe (Luxury)", "Free", "Premium private stylist & exclusive brand collections.", listOf("Unlimited AI styling requests", "1-on-1 simulated stylist support", "Direct Tata CLiQ Luxury bookings", "Priority product matching"), "High-End Luxury")
        )
        isUk -> listOf(
            RegionalPlan("lite", "Lite Stylist", "Free", "Affordable entry-level local styling tips.", listOf("Weekly Outfit Recommendation", "Basic Skincare Audit", "Standard local store links"), "Budget Conscious"),
            RegionalPlan("elite", "MARE Elite Concierge", "Free", "Highly personalized regional fit advice with weather integrations.", listOf("Daily Weather-Adaptive Outfits", "Advanced AI Skincare Routines", "Local Space NK/Selfridges access"), "Daily Active Stylist"),
            RegionalPlan("infinite", "Infinite Wardrobe (Luxury)", "Free", "Premium private stylist & exclusive brand collections.", listOf("Unlimited AI styling requests", "1-on-1 simulated stylist support", "Direct Farfetch UK bookings", "Priority product matching"), "High-End Luxury")
        )
        isJapan -> listOf(
            RegionalPlan("lite", "Lite Stylist", "Free", "Affordable entry-level local styling tips.", listOf("Weekly Outfit Recommendation", "Basic Skincare Audit", "Standard local store links"), "Budget Conscious"),
            RegionalPlan("elite", "MARE Elite Concierge", "Free", "Highly personalized regional fit advice with weather integrations.", listOf("Daily Weather-Adaptive Outfits", "Advanced AI Skincare Routines", "Local ZozoTown/Beams access"), "Daily Active Stylist"),
            RegionalPlan("infinite", "Infinite Wardrobe (Luxury)", "Free", "Premium private stylist & exclusive brand collections.", listOf("Unlimited AI styling requests", "1-on-1 simulated stylist support", "Direct Shinjuku boutique access", "Priority product matching"), "High-End Luxury")
        )
        isEurope -> listOf(
            RegionalPlan("lite", "Lite Stylist", "Free", "Affordable entry-level local styling tips.", listOf("Weekly Outfit Recommendation", "Basic Skincare Audit", "Standard local store links"), "Budget Conscious"),
            RegionalPlan("elite", "MARE Elite Concierge", "Free", "Highly personalized regional fit advice with weather integrations.", listOf("Daily Weather-Adaptive Outfits", "Advanced AI Skincare Routines", "Local Sephora Paris/24S access"), "Daily Active Stylist"),
            RegionalPlan("infinite", "Infinite Wardrobe (Luxury)", "Free", "Premium private stylist & exclusive brand collections.", listOf("Unlimited AI styling requests", "1-on-1 simulated stylist support", "Direct Mytheresa/LuisaViaRoma bookings", "Priority product matching"), "High-End Luxury")
        )
        else -> listOf(
            RegionalPlan("lite", "Lite Stylist", "Free", "Affordable entry-level local styling tips.", listOf("Weekly Outfit Recommendation", "Basic Skincare Audit", "Standard local store links"), "Budget Conscious"),
            RegionalPlan("elite", "MARE Elite Concierge", "Free", "Highly personalized regional fit advice with weather integrations.", listOf("Daily Weather-Adaptive Outfits", "Advanced AI Skincare Routines", "Local Nordstrom/Ulta access"), "Daily Active Stylist"),
            RegionalPlan("infinite", "Infinite Wardrobe (Luxury)", "Free", "Premium private stylist & exclusive brand collections.", listOf("Unlimited AI styling requests", "1-on-1 simulated stylist support", "Direct Saks/Net-A-Porter bookings", "Priority product matching"), "High-End Luxury")
        )
    }
}
