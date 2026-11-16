package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.CyberWomenDatabase
import com.example.data.local.PreferencesStore
import com.example.data.remote.FirestoreRepository
import com.example.data.remote.authStateFlow
import com.example.service.ShakeDetector
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MeenaAiScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SafetyHubScreen
import com.example.ui.screens.SafetyMapScreen
import com.example.ui.screens.ShieldSosScreen
import com.example.ui.theme.CommandNavy
import com.example.ui.theme.CriticalRose
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberWomenTheme
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.PanelNavy
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MeenaAiViewModel
import com.example.util.AppStrings
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

const val ROUTE_HOME = "home"
const val ROUTE_SAFETY_MAP = "safety_map"
const val ROUTE_SHIELD_SOS = "shield_sos"
const val ROUTE_SAFETY_HUB = "safety_hub"
const val ROUTE_PROFILE = "profile"
const val ROUTE_MEENA_AI = "meena_ai"

private data class TopNavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            CyberWomenTheme {
                val authUser by Firebase.auth.authStateFlow()
                    .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

                val currentUser = authUser
                if (currentUser == null) {
                    AuthScreen()
                } else {
                    CyberWomenMainShell(currentUser = currentUser)
                }
            }
        }
    }
}

@Composable
private fun CyberWomenMainShell(currentUser: FirebaseUser) {
    val context = LocalContext.current
    val appCtx = context.applicationContext

    val firestoreRepo = remember { FirestoreRepository(appCtx) }
    val dao = remember { CyberWomenDatabase.getInstance(appCtx).dao() }
    val prefs = remember { PreferencesStore(appCtx) }

    val mainViewModel: MainViewModel = viewModel(
        key = "main_vm_${currentUser.uid}",
        factory = MainViewModel.factory(
            firestoreRepo = firestoreRepo,
            dao = dao,
            prefs = prefs,
            userId = currentUser.uid,
            displayName = currentUser.displayName ?: "Guardian User",
            email = currentUser.email
        )
    )
    val meenaViewModel: MeenaAiViewModel = viewModel(key = "meena_vm_${currentUser.uid}")

    val language by mainViewModel.language.collectAsStateWithLifecycle()
    val shakeEnabled by mainViewModel.shakeToSosEnabled.collectAsStateWithLifecycle()
    val toastMsg by mainViewModel.statusToast.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(toastMsg) {
        val msg = toastMsg
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            mainViewModel.showToast(null)
        }
    }

    DisposableEffect(shakeEnabled, currentUser.uid) {
        val detector = if (shakeEnabled) {
            ShakeDetector(context) {
                mainViewModel.startSosCountdown(context, triggerType = "shake")
            }.also { it.start() }
        } else null
        onDispose {
            detector?.stop()
        }
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_HOME

    val tabs = remember(language) {
        listOf(
            TopNavTab(ROUTE_HOME, AppStrings.navHome(language), Icons.Filled.Home, Icons.Outlined.Home),
            TopNavTab(ROUTE_SAFETY_MAP, AppStrings.navMap(language), Icons.Filled.Map, Icons.Outlined.Map),
            TopNavTab(ROUTE_SHIELD_SOS, AppStrings.navSos(language), Icons.Filled.Shield, Icons.Outlined.Shield),
            TopNavTab(ROUTE_SAFETY_HUB, AppStrings.navHub(language), Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook),
            TopNavTab(ROUTE_PROFILE, AppStrings.navProfile(language), Icons.Filled.Person, Icons.Outlined.Person)
        )
    }

    val navigateTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepObsidian)
    ) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = DeepObsidian,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                if (currentRoute != ROUTE_MEENA_AI && currentRoute != ROUTE_SAFETY_MAP) {
                    FloatingActionButton(
                        onClick = { navController.navigate(ROUTE_MEENA_AI) },
                        containerColor = CyberPurple,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_open_meena_ai")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Ask Meena AI"
                        )
                    }
                }
            },
            bottomBar = {
                if (!useRail && currentRoute != ROUTE_MEENA_AI) {
                    NavigationBar(
                        containerColor = CommandNavy,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        tabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            val activeTint = if (tab.route == ROUTE_SHIELD_SOS) CriticalRose else CyberCyan
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = activeTint,
                                    selectedTextColor = activeTint,
                                    indicatorColor = activeTint.copy(alpha = 0.16f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("bottom_nav_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail && currentRoute != ROUTE_MEENA_AI) {
                    NavigationRail(containerColor = CommandNavy) {
                        tabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            val activeTint = if (tab.route == ROUTE_SHIELD_SOS) CriticalRose else CyberCyan
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = activeTint,
                                    selectedTextColor = activeTint,
                                    indicatorColor = activeTint.copy(alpha = 0.16f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("rail_nav_${tab.route}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_HOME
                    ) {
                        composable(ROUTE_HOME) {
                            HomeScreen(
                                viewModel = mainViewModel,
                                onOpenMap = { navigateTab(ROUTE_SAFETY_MAP) },
                                onOpenMapFocus = { lat, lon ->
                                    mainViewModel.focusMapOn(lat, lon)
                                    navigateTab(ROUTE_SAFETY_MAP)
                                },
                                onOpenShieldSos = { navigateTab(ROUTE_SHIELD_SOS) },
                                onOpenMeenaAi = { navController.navigate(ROUTE_MEENA_AI) },
                                onOpenSafetyHub = { navigateTab(ROUTE_SAFETY_HUB) }
                            )
                        }
                        composable(ROUTE_SAFETY_MAP) {
                            SafetyMapScreen(viewModel = mainViewModel)
                        }
                        composable(ROUTE_SHIELD_SOS) {
                            ShieldSosScreen(viewModel = mainViewModel)
                        }
                        composable(ROUTE_SAFETY_HUB) {
                            SafetyHubScreen(viewModel = mainViewModel)
                        }
                        composable(ROUTE_PROFILE) {
                            ProfileScreen(
                                displayName = currentUser.displayName ?: "Guardian User",
                                email = currentUser.email,
                                viewModel = mainViewModel,
                                onOpenShieldSos = { navigateTab(ROUTE_SHIELD_SOS) }
                            )
                        }
                        composable(ROUTE_MEENA_AI) {
                            MeenaAiScreen(
                                mainViewModel = mainViewModel,
                                meenaViewModel = meenaViewModel,
                                onBack = { navController.popBackStack() },
                                onOpenShieldSos = {
                                    navController.popBackStack()
                                    navigateTab(ROUTE_SHIELD_SOS)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
