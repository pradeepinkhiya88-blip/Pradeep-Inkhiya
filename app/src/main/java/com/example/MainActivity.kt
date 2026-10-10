package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.GuideScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SavedProjectsScreen
import com.example.ui.theme.AutoClipPrimary
import com.example.ui.theme.AutoClipSecondary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ClipGeneratorViewModel
import com.example.ui.viewmodel.NavTab

class MainActivity : ComponentActivity() {

    private val viewModel: ClipGeneratorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Pre-create WebView cache directory structure to prevent Chromium simple_backend directory creation failure
        try {
            val webViewJsDir = java.io.File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/js")
            if (!webViewJsDir.exists()) {
                webViewJsDir.mkdirs()
            }
        } catch (_: Exception) {}

        handleIntent(intent)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                viewModel.onUrlInputChanged(sharedText)
                viewModel.setTab(NavTab.GENERATE)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: ClipGeneratorViewModel) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val savedProjects by viewModel.savedProjects.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AutoClipPrimary,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MovieCreation,
                                    contentDescription = "Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "AutoClip AI",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AutoClipSecondary.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "PRO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AutoClipSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .border(
                        BorderStroke(1.dp, Brush.horizontalGradient(listOf(Color(0xFF222533), Color(0xFF222533))))
                    )
            ) {
                NavigationBarItem(
                    selected = activeTab == NavTab.GENERATE,
                    onClick = { viewModel.setTab(NavTab.GENERATE) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Generate Clips"
                        )
                    },
                    label = { Text("Generate", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = AutoClipPrimary,
                        indicatorColor = AutoClipPrimary,
                        unselectedIconColor = Color(0xFF7E849E),
                        unselectedTextColor = Color(0xFF7E849E)
                    ),
                    modifier = Modifier.testTag("nav_tab_generate")
                )

                NavigationBarItem(
                    selected = activeTab == NavTab.PROJECTS,
                    onClick = { viewModel.setTab(NavTab.PROJECTS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (savedProjects.isNotEmpty()) {
                                    Badge(
                                        containerColor = AutoClipSecondary,
                                        contentColor = Color.White
                                    ) {
                                        Text("${savedProjects.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = "History"
                            )
                        }
                    },
                    label = { Text("History", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = AutoClipSecondary,
                        indicatorColor = AutoClipSecondary,
                        unselectedIconColor = Color(0xFF7E849E),
                        unselectedTextColor = Color(0xFF7E849E)
                    ),
                    modifier = Modifier.testTag("nav_tab_projects")
                )

                NavigationBarItem(
                    selected = activeTab == NavTab.GUIDE,
                    onClick = { viewModel.setTab(NavTab.GUIDE) },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Playbook"
                        )
                    },
                    label = { Text("Playbook", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = AutoClipPrimary,
                        indicatorColor = AutoClipPrimary,
                        unselectedIconColor = Color(0xFF7E849E),
                        unselectedTextColor = Color(0xFF7E849E)
                    ),
                    modifier = Modifier.testTag("nav_tab_guide")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                NavTab.GENERATE -> HomeScreen(viewModel = viewModel)
                NavTab.PROJECTS -> SavedProjectsScreen(viewModel = viewModel)
                NavTab.GUIDE -> GuideScreen()
            }
        }
    }
}
