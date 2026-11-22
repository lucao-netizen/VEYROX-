package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.config.VeyroxConfig
import com.example.domain.model.AiEngineType
import com.example.domain.model.AiMode
import com.example.domain.model.ThemePreference
import com.example.ui.components.ConfirmDestructiveDialog
import com.example.ui.components.DiscordCtaButton
import com.example.ui.components.icon
import com.example.ui.theme.VeyroxError
import com.example.ui.theme.VeyroxSuccess
import com.example.ui.theme.VeyroxTheme
import com.example.ui.viewmodel.BackendCheckStatus
import com.example.ui.viewmodel.SettingsUiState

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onSelectTheme: (ThemePreference) -> Unit,
    onSelectAiEngine: (AiEngineType) -> Unit,
    onToggleStreaming: (Boolean) -> Unit,
    onSelectDefaultMode: (AiMode) -> Unit,
    onSaveBackendUrl: (String) -> Unit,
    onTestBackendHealth: (String) -> Unit,
    onClearAllHistory: () -> Unit,
    onDismissBanner: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = VeyroxTheme.colors
    var showClearAllDialog by remember { mutableStateOf(false) }
    var backendUrlDraft by remember(uiState.preferences.customBackendUrl) {
        mutableStateOf(uiState.preferences.customBackendUrl)
    }

    if (showClearAllDialog) {
        ConfirmDestructiveDialog(
            title = "Clear All Conversations",
            message = "Delete all ${uiState.conversationCount} local conversations from this device? This action cannot be undone.",
            confirmLabel = "Delete All",
            onDismiss = { showClearAllDialog = false },
            onConfirm = {
                showClearAllDialog = false
                onClearAllHistory()
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 820.dp)
                .testTag("settings_screen"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.textPrimary
                )
            }

            uiState.statusBanner?.let { banner ->
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colors.blueTint,
                        border = BorderStroke(1.dp, colors.primaryBlue.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = banner,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onDismissBanner) {
                                Text("OK", color = colors.brightBlue)
                            }
                        }
                    }
                }
            }

            // Appearance Section
            item {
                SettingsSectionCard(title = "APPEARANCE") {
                    ThemePreference.entries.forEachIndexed { idx, theme ->
                        SelectableOptionRow(
                            title = theme.label,
                            subtitle = when (theme) {
                                ThemePreference.DARK -> "Deep obsidian & electric blue default"
                                ThemePreference.LIGHT -> "High-clarity daylight palette"
                                ThemePreference.SYSTEM -> "Follow Android system appearance"
                            },
                            selected = uiState.preferences.themePreference == theme,
                            onClick = { onSelectTheme(theme) },
                            testTag = "settings_theme_${theme.name.lowercase()}"
                        )
                        if (idx < ThemePreference.entries.lastIndex) {
                            HorizontalDivider(color = colors.borderSubtle)
                        }
                    }
                }
            }

            // AI Provider / Engine Section
            item {
                SettingsSectionCard(title = "AI ENGINE & PROVIDER") {
                    AiEngineType.entries.forEachIndexed { idx, engine ->
                        SelectableOptionRow(
                            title = engine.label,
                            subtitle = when (engine) {
                                AiEngineType.FIREBASE_AI_LOGIC ->
                                    "Live Gemini 3.8 Flash via Firebase AI Logic & App Check (no API key stored on device)"
                                AiEngineType.DEMO_MODE ->
                                    "Offline simulated responses clearly labeled as Demo Mode"
                                AiEngineType.CUSTOM_BACKEND ->
                                    "Connect to your self-hosted VEYROX REST backend"
                            },
                            selected = uiState.preferences.aiEngineType == engine,
                            onClick = { onSelectAiEngine(engine) },
                            testTag = "settings_engine_${engine.name.lowercase()}"
                        )
                        if (idx < AiEngineType.entries.lastIndex) {
                            HorizontalDivider(color = colors.borderSubtle)
                        }
                    }

                    if (uiState.preferences.aiEngineType == AiEngineType.CUSTOM_BACKEND) {
                        HorizontalDivider(color = colors.borderSubtle)
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Self-Hosted Backend URL",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textPrimary
                            )
                            OutlinedTextField(
                                value = backendUrlDraft,
                                onValueChange = { backendUrlDraft = it },
                                placeholder = {
                                    Text("https://your-veyrox-api.example.com", color = colors.textSecondary)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = colors.secondaryBackground,
                                    unfocusedContainerColor = colors.secondaryBackground,
                                    focusedBorderColor = colors.primaryBlue,
                                    unfocusedBorderColor = colors.borderSubtle,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_backend_url_input")
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onSaveBackendUrl(backendUrlDraft) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("settings_save_backend_url"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Save URL")
                                }
                                Button(
                                    onClick = {
                                        onSaveBackendUrl(backendUrlDraft)
                                        onTestBackendHealth(backendUrlDraft)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("settings_test_backend_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.primaryBlue,
                                        contentColor = Color.White
                                    )
                                ) {
                                    if (uiState.backendCheckStatus is BackendCheckStatus.Checking) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                    } else {
                                        Text("Test /api/health")
                                    }
                                }
                            }
                            when (val status = uiState.backendCheckStatus) {
                                is BackendCheckStatus.Healthy -> {
                                    Text(
                                        text = status.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = VeyroxSuccess
                                    )
                                }

                                is BackendCheckStatus.Failed -> {
                                    Text(
                                        text = status.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = VeyroxError
                                    )
                                }

                                else -> Unit
                            }
                        }
                    }
                }
            }

            // Chat Settings Section
            item {
                SettingsSectionCard(title = "CHAT SETTINGS") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Stream AI Responses",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Display tokens incrementally as they arrive",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        Switch(
                            checked = uiState.preferences.streamingEnabled,
                            onCheckedChange = onToggleStreaming,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.primaryBlue
                            ),
                            modifier = Modifier.testTag("settings_streaming_switch")
                        )
                    }
                    HorizontalDivider(color = colors.borderSubtle)
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Default AI Mode",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                        AiMode.entries.forEach { mode ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelectDefaultMode(mode) }
                                    .padding(horizontal = 10.dp, vertical = 10.dp)
                                    .testTag("settings_default_mode_${mode.name.lowercase()}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = mode.icon(),
                                    contentDescription = null,
                                    tint = colors.brightBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = mode.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (uiState.preferences.defaultAiMode == mode)
                                        Icons.Rounded.CheckCircle
                                    else Icons.Rounded.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (uiState.preferences.defaultAiMode == mode)
                                        colors.primaryBlue
                                    else colors.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Conversation Management
            item {
                SettingsSectionCard(title = "CONVERSATION MANAGEMENT") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Local Room Storage",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "${uiState.conversationCount} conversations stored on device",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        OutlinedButton(
                            onClick = { showClearAllDialog = true },
                            enabled = uiState.conversationCount > 0,
                            border = BorderStroke(1.dp, VeyroxError.copy(alpha = 0.55f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VeyroxError),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("settings_clear_all_button")
                        ) {
                            Icon(Icons.Rounded.DeleteSweep, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Clear All")
                        }
                    }
                }
            }

            // Privacy & Security Transparency
            item {
                SettingsSectionCard(title = "PRIVACY & SECURITY") {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = colors.brightBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Data Storage & Network Transparency",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textPrimary
                            )
                        }
                        Text(
                            text = "• Local Storage: Conversation IDs, titles, messages, timestamps, and preferences are stored strictly on your device in Room & DataStore.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "• Live Gemini AI: Prompts and recent thread turns are transmitted securely over HTTPS via Firebase AI Logic with App Check attestation. No API keys are embedded in this APK.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "• Demo Mode: Generates responses offline with zero external requests.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            // Community & App Info
            item {
                SettingsSectionCard(title = "COMMUNITY & APP INFO") {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DiscordCtaButton(
                            label = "Discord Community",
                            testTag = "settings_discord_button",
                            modifier = Modifier.fillMaxWidth()
                        )
                        HorizontalDivider(color = colors.borderSubtle)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onOpenAbout)
                                .padding(vertical = 8.dp)
                                .testTag("settings_about_row"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Rounded.Info, null, tint = colors.brightBlue)
                                Column {
                                    Text(
                                        text = "About VEYROX",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Version ${VeyroxConfig.APP_VERSION} (${VeyroxConfig.BUILD_CHANNEL})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "Open About VEYROX",
                                tint = colors.textSecondary
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    val colors = VeyroxTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = colors.brightBlue,
            letterSpacing = 1.4.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = colors.cardBackground,
            border = BorderStroke(1.dp, colors.borderSubtle)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SelectableOptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val colors = VeyroxTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
        Icon(
            imageVector = if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
            contentDescription = if (selected) "Selected" else "Not selected",
            tint = if (selected) colors.primaryBlue else colors.textSecondary,
            modifier = Modifier.size(22.dp)
        )
    }
}
