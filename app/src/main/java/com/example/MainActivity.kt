package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.ArticlesTab
import com.example.ui.screens.CollectionTab
import com.example.ui.screens.HomeTab
import com.example.ui.screens.ProfileTab
import com.example.ui.screens.SavedTab
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MareViewModel

import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MareViewModel = viewModel()
            val isDark by viewModel.isThemeDark.collectAsState()
            val userProfile by viewModel.userProfile.collectAsState()

            var showSplashScreen by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(3500) // Appears for 3.5 seconds as requested (3 to 4 sec)
                showSplashScreen = false
            }

            val customColors = remember(userProfile, isDark) {
                val colorsStr = userProfile?.customPaletteColors
                if (!colorsStr.isNullOrEmpty()) {
                    val colorsList = colorsStr.split(",")
                    if (colorsList.size >= 4) {
                        try {
                            val prim = Color(android.graphics.Color.parseColor(colorsList[0]))
                            val bgHex = if (isDark) colorsList[3] else colorsList[2]
                            val bg = Color(android.graphics.Color.parseColor(bgHex))
                            Pair(prim, bg)
                        } catch (e: Exception) {
                            Pair(null, null)
                        }
                    } else {
                        Pair(null, null)
                    }
                } else {
                    Pair(null, null)
                }
            }

            MyApplicationTheme(
                darkTheme = isDark,
                dynamicColor = false,
                customPrimary = customColors.first,
                customBackground = customColors.second
            ) {
                if (showSplashScreen) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF151D1A)) // Matching the deep dark sage-forest aesthetic background
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_splash_mare),
                            contentDescription = "MARE - Splash Screen",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Subtle minimalist loading indicator at the bottom to represent high-end magazine flow
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(androidx.compose.ui.Alignment.BottomCenter)
                                .padding(bottom = 64.dp)
                                .size(28.dp),
                            color = Color(0xFFC5DCD6),
                            strokeWidth = 2.dp
                        )
                    }
                } else {
                    var currentTab by remember { mutableStateOf("Home") }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            val navBarColor = MaterialTheme.colorScheme.surface
                            val indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            val iconActiveColor = MaterialTheme.colorScheme.primary
                            val iconInactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

                            NavigationBar(
                                containerColor = navBarColor,
                                tonalElevation = 8.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == "Home",
                                    onClick = { currentTab = "Home" },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Home,
                                            contentDescription = "Home"
                                        )
                                    },
                                    label = { Text("Home", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = iconActiveColor,
                                        unselectedIconColor = iconInactiveColor,
                                        selectedTextColor = iconActiveColor,
                                        unselectedTextColor = iconInactiveColor,
                                        indicatorColor = indicatorColor
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentTab == "Collection",
                                    onClick = { currentTab = "Collection" },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingBag,
                                            contentDescription = "Collection"
                                        )
                                    },
                                    label = { Text("Collection", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = iconActiveColor,
                                        unselectedIconColor = iconInactiveColor,
                                        selectedTextColor = iconActiveColor,
                                        unselectedTextColor = iconInactiveColor,
                                        indicatorColor = indicatorColor
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentTab == "Articles",
                                    onClick = { currentTab = "Articles" },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = "Articles"
                                        )
                                    },
                                    label = { Text("Articles", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = iconActiveColor,
                                        unselectedIconColor = iconInactiveColor,
                                        selectedTextColor = iconActiveColor,
                                        unselectedTextColor = iconInactiveColor,
                                        indicatorColor = indicatorColor
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentTab == "Saved",
                                    onClick = { currentTab = "Saved" },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = "Saved"
                                        )
                                    },
                                    label = { Text("Saved", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = iconActiveColor,
                                        unselectedIconColor = iconInactiveColor,
                                        selectedTextColor = iconActiveColor,
                                        unselectedTextColor = iconInactiveColor,
                                        indicatorColor = indicatorColor
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentTab == "Profile",
                                    onClick = { currentTab = "Profile" },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Profile"
                                        )
                                    },
                                    label = { Text("Profile", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = iconActiveColor,
                                        unselectedIconColor = iconInactiveColor,
                                        selectedTextColor = iconActiveColor,
                                        unselectedTextColor = iconInactiveColor,
                                        indicatorColor = indicatorColor
                                    )
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            when (currentTab) {
                                "Home" -> HomeTab(
                                    viewModel = viewModel,
                                    onNavigateToArticles = { currentTab = "Articles" },
                                    onNavigateToCollection = { currentTab = "Collection" }
                                )
                                "Collection" -> CollectionTab(viewModel = viewModel)
                                "Articles" -> ArticlesTab(viewModel = viewModel)
                                "Saved" -> SavedTab(viewModel = viewModel)
                                "Profile" -> ProfileTab(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
