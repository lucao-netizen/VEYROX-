package com.example.ui.navigation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.di.VeyroxContainer
import com.example.domain.model.AiMode
import com.example.ui.components.VeyroxGeometricLogo
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.VeyroxTheme
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.HistoryViewModel
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

object VeyroxRoutes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val CHAT = "chat/{conversationId}?mode={mode}&prompt={prompt}"

    fun chatRoute(
        conversationId: String,
        mode: AiMode = AiMode.GENERAL,
        initialPrompt: String? = null
    ): String {
        val encodedPrompt = if (!initialPrompt.isNullOrBlank()) {
            Uri.encode(initialPrompt.trim())
        } else {
            ""
        }
        return "chat/$conversationId?mode=${mode.name}&prompt=$encodedPrompt"
    }
}

private data class TopLevelNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val topLevelItems = listOf(
    TopLevelNavItem(
        route = VeyroxRoutes.HOME,
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_home"
    ),
    TopLevelNavItem(
        route = VeyroxRoutes.HISTORY,
        label = "History",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History,
        testTag = "nav_history"
    ),
    TopLevelNavItem(
        route = VeyroxRoutes.SETTINGS,
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_settings"
    )
)

@Composable
fun VeyroxApp(container: VeyroxContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showTopLevelNav = currentRoute in setOf(
        VeyroxRoutes.HOME,
        VeyroxRoutes.HISTORY,
        VeyroxRoutes.SETTINGS
    )
    val coroutineScope = rememberCoroutineScope()
    val colors = VeyroxTheme.colors

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun openNewChat(mode: AiMode, initialPrompt: String? = null) {
        coroutineScope.launch {
            val id = container.chatRepository.createConversation(mode = mode)
            navController.navigate(VeyroxRoutes.chatRoute(id, mode, initialPrompt))
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = colors.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showTopLevelNav && !useRail) {
                    Column {
                        HorizontalDivider(color = colors.borderSubtle)
                        NavigationBar(
                            containerColor = colors.secondaryBackground,
                            tonalElevation = 0.dp
                        ) {
                            topLevelItems.forEach { item ->
                                val selected = currentRoute == item.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTopLevel(item.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.label,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = colors.brightBlue,
                                        selectedTextColor = colors.brightBlue,
                                        indicatorColor = colors.blueTint,
                                        unselectedIconColor = colors.textSecondary,
                                        unselectedTextColor = colors.textSecondary
                                    ),
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
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
                if (showTopLevelNav && useRail) {
                    NavigationRail(
                        containerColor = colors.secondaryBackground,
                        modifier = Modifier
                            .fillMaxHeight()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {
                        Spacer(modifier = Modifier.height(14.dp))
                        VeyroxGeometricLogo(size = 36.dp)
                        Spacer(modifier = Modifier.height(24.dp))
                        topLevelItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = colors.brightBlue,
                                    selectedTextColor = colors.brightBlue,
                                    indicatorColor = colors.blueTint,
                                    unselectedIconColor = colors.textSecondary,
                                    unselectedTextColor = colors.textSecondary
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    NavHost(
                        navController = navController,
                        startDestination = VeyroxRoutes.SPLASH
                    ) {
                        composable(VeyroxRoutes.SPLASH) {
                            SplashScreen(
                                onSplashFinished = {
                                    navController.navigate(VeyroxRoutes.HOME) {
                                        popUpTo(VeyroxRoutes.SPLASH) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(VeyroxRoutes.HOME) {
                            val homeVm: HomeViewModel = viewModel(
                                factory = HomeViewModel.Factory(
                                    chatRepository = container.chatRepository,
                                    preferencesRepository = container.preferencesRepository
                                )
                            )
                            val uiState by homeVm.uiState.collectAsStateWithLifecycle()
                            HomeScreen(
                                uiState = uiState,
                                onStartNewChat = { mode, prompt ->
                                    openNewChat(mode, prompt)
                                },
                                onOpenConversation = { id ->
                                    navController.navigate(VeyroxRoutes.chatRoute(id))
                                },
                                onNavigateToHistory = {
                                    navigateTopLevel(VeyroxRoutes.HISTORY)
                                },
                                onNavigateToSettings = {
                                    navigateTopLevel(VeyroxRoutes.SETTINGS)
                                },
                                onNavigateToAbout = {
                                    navController.navigate(VeyroxRoutes.ABOUT)
                                },
                                modifier = Modifier
                                    .statusBarsPadding()
                                    .let { if (useRail) it.navigationBarsPadding() else it }
                            )
                        }

                        composable(VeyroxRoutes.HISTORY) {
                            val historyVm: HistoryViewModel = viewModel(
                                factory = HistoryViewModel.Factory(
                                    chatRepository = container.chatRepository
                                )
                            )
                            val uiState by historyVm.uiState.collectAsStateWithLifecycle()
                            HistoryScreen(
                                uiState = uiState,
                                onSearchQueryChange = historyVm::updateSearchQuery,
                                onSelectModeFilter = historyVm::selectModeFilter,
                                onOpenConversation = { id ->
                                    navController.navigate(VeyroxRoutes.chatRoute(id))
                                },
                                onRenameConversation = historyVm::renameConversation,
                                onDeleteConversation = historyVm::deleteConversation,
                                onCreateNewChat = {
                                    openNewChat(AiMode.GENERAL)
                                },
                                modifier = Modifier
                                    .statusBarsPadding()
                                    .let { if (useRail) it.navigationBarsPadding() else it }
                            )
                        }

                        composable(VeyroxRoutes.SETTINGS) {
                            val settingsVm: SettingsViewModel = viewModel(
                                factory = SettingsViewModel.Factory(
                                    preferencesRepository = container.preferencesRepository,
                                    chatRepository = container.chatRepository
                                )
                            )
                            val uiState by settingsVm.uiState.collectAsStateWithLifecycle()
                            SettingsScreen(
                                uiState = uiState,
                                onSelectTheme = settingsVm::setThemePreference,
                                onSelectAiEngine = settingsVm::setAiEngineType,
                                onToggleStreaming = settingsVm::setStreamingEnabled,
                                onSelectDefaultMode = settingsVm::setDefaultAiMode,
                                onSaveBackendUrl = settingsVm::saveCustomBackendUrl,
                                onTestBackendHealth = settingsVm::testBackendHealth,
                                onClearAllHistory = settingsVm::clearAllHistory,
                                onDismissBanner = settingsVm::dismissBanner,
                                onOpenAbout = {
                                    navController.navigate(VeyroxRoutes.ABOUT)
                                },
                                modifier = Modifier
                                    .statusBarsPadding()
                                    .let { if (useRail) it.navigationBarsPadding() else it }
                            )
                        }

                        composable(VeyroxRoutes.ABOUT) {
                            AboutScreen(
                                onBack = { navController.popBackStack() },
                                modifier = Modifier.navigationBarsPadding()
                            )
                        }

                        composable(
                            route = VeyroxRoutes.CHAT,
                            arguments = listOf(
                                navArgument("conversationId") { type = NavType.StringType },
                                navArgument("mode") {
                                    type = NavType.StringType
                                    defaultValue = AiMode.GENERAL.name
                                },
                                navArgument("prompt") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                }
                            )
                        ) { entry ->
                            val conversationId = entry.arguments?.getString("conversationId").orEmpty()
                            val mode = AiMode.fromName(entry.arguments?.getString("mode"))
                            val rawPrompt = entry.arguments?.getString("prompt").orEmpty()
                            val decodedPrompt = if (rawPrompt.isNotBlank()) Uri.decode(rawPrompt) else null

                            val chatVm: ChatViewModel = viewModel(
                                key = "chat_$conversationId",
                                factory = ChatViewModel.Factory(
                                    conversationId = conversationId,
                                    initialMode = mode,
                                    initialPrompt = decodedPrompt,
                                    chatRepository = container.chatRepository,
                                    preferencesRepository = container.preferencesRepository
                                )
                            )
                            val uiState by chatVm.uiState.collectAsStateWithLifecycle()

                            ChatScreen(
                                uiState = uiState,
                                onBack = { navController.popBackStack() },
                                onSendMessage = chatVm::sendMessage,
                                onStopGeneration = chatVm::stopGeneration,
                                onRetryLast = chatVm::retryLastPrompt,
                                onDismissError = chatVm::dismissError,
                                onSwitchMode = chatVm::switchMode,
                                onRenameConversation = chatVm::renameConversation,
                                onClearConversation = chatVm::clearConversation,
                                onDeleteConversation = {
                                    chatVm.deleteConversation {
                                        navController.popBackStack()
                                    }
                                },
                                onStartNewChat = { selectedMode ->
                                    openNewChat(selectedMode)
                                },
                                onOpenSettings = {
                                    navigateTopLevel(VeyroxRoutes.SETTINGS)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
