package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.config.VeyroxConfig
import com.example.data.local.entity.ConversationEntity
import com.example.domain.model.AiMode
import com.example.ui.components.AiModePill
import com.example.ui.components.DiscordCommunityCard
import com.example.ui.components.EngineStatusPill
import com.example.ui.components.VeyroxBrandHeader
import com.example.ui.components.formatConversationDate
import com.example.ui.components.icon
import com.example.ui.theme.VeyroxTheme
import com.example.ui.viewmodel.HomeUiState

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onStartNewChat: (mode: AiMode, initialPrompt: String?) -> Unit,
    onOpenConversation: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = VeyroxTheme.colors
    var selectedMode by remember(uiState.preferences.defaultAiMode) {
        mutableStateOf(uiState.preferences.defaultAiMode)
    }
    var quickPrompt by remember { mutableStateOf("") }

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
                .testTag("home_screen"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Top Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    VeyroxBrandHeader()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        EngineStatusPill(engineType = uiState.preferences.aiEngineType)
                        IconButton(
                            onClick = onNavigateToAbout,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("home_about_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "About VEYROX",
                                tint = colors.textSecondary
                            )
                        }
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("home_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                tint = colors.textSecondary
                            )
                        }
                    }
                }
            }

            // Hero Greeting & Main Quick Composer
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = colors.cardBackground,
                    border = BorderStroke(1.dp, colors.borderSubtle)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        colors.primaryBlue.copy(alpha = 0.14f),
                                        Color.Transparent
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = VeyroxConfig.HERO_SLOGAN,
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.brightBlue,
                                letterSpacing = 1.6.sp
                            )
                            Text(
                                text = "Welcome to VEYROX. What can I help you with today?",
                                style = MaterialTheme.typography.headlineMedium,
                                color = colors.textPrimary
                            )

                            // Mode Selector Chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AiMode.entries.forEach { mode ->
                                    val isSelected = selectedMode == mode
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedMode = mode },
                                        label = {
                                            Text(
                                                text = mode.shortLabel,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = mode.icon(),
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = colors.primaryBlue,
                                            selectedLabelColor = Color.White,
                                            selectedLeadingIconColor = Color.White,
                                            containerColor = colors.secondaryBackground,
                                            labelColor = colors.textSecondary,
                                            iconColor = colors.textSecondary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = colors.borderSubtle,
                                            selectedBorderColor = colors.primaryBlue
                                        ),
                                        modifier = Modifier.testTag("home_mode_chip_${mode.name.lowercase()}")
                                    )
                                }
                            }

                            // Main Chat Input
                            OutlinedTextField(
                                value = quickPrompt,
                                onValueChange = { quickPrompt = it },
                                placeholder = {
                                    Text(
                                        text = "Ask ${selectedMode.displayName} anything...",
                                        color = colors.textSecondary
                                    )
                                },
                                maxLines = 4,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = colors.secondaryBackground,
                                    unfocusedContainerColor = colors.secondaryBackground,
                                    focusedBorderColor = colors.primaryBlue,
                                    unfocusedBorderColor = colors.borderSubtle,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                ),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            val prompt = quickPrompt.trim()
                                            if (prompt.isNotEmpty()) {
                                                quickPrompt = ""
                                                onStartNewChat(selectedMode, prompt)
                                            }
                                        },
                                        enabled = quickPrompt.isNotBlank(),
                                        modifier = Modifier
                                            .padding(end = 4.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (quickPrompt.isNotBlank()) colors.primaryBlue
                                                else colors.elevatedSurface
                                            )
                                            .testTag("home_send_prompt_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Send,
                                            contentDescription = "Send prompt",
                                            tint = if (quickPrompt.isNotBlank()) Color.White else colors.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        val prompt = quickPrompt.trim()
                                        if (prompt.isNotEmpty()) {
                                            quickPrompt = ""
                                            onStartNewChat(selectedMode, prompt)
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("home_chat_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { onStartNewChat(selectedMode, null) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("home_new_chat_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.primaryBlue,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "New Chat",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // AI Mode Feature Shortcuts (2x2 Grid)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "AI Modes",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary
                    )
                    val modeRows = AiMode.entries.chunked(2)
                    modeRows.forEach { rowModes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowModes.forEach { mode ->
                                AiModeShortcutCard(
                                    mode = mode,
                                    onClick = { onStartNewChat(mode, null) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Recent Conversations Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Recent Conversations",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary
                    )
                    if (uiState.recentConversations.isNotEmpty()) {
                        TextButton(
                            onClick = onNavigateToHistory,
                            modifier = Modifier.testTag("home_view_all_history_button")
                        ) {
                            Text("View All", color = colors.brightBlue)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = colors.brightBlue
                            )
                        }
                    }
                }
            }

            if (uiState.recentConversations.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = colors.cardBackground,
                        border = BorderStroke(1.dp, colors.borderSubtle)
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.blueTint),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = colors.brightBlue
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "No conversations yet",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Choose any AI mode or type a prompt above to begin.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            } else {
                items(
                    items = uiState.recentConversations,
                    key = { it.id }
                ) { conversation ->
                    RecentConversationRow(
                        conversation = conversation,
                        onClick = { onOpenConversation(conversation.id) }
                    )
                }
            }

            // Community / Official Discord Section
            item {
                DiscordCommunityCard(testTag = "home_discord_button")
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun AiModeShortcutCard(
    mode: AiMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = VeyroxTheme.colors
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("home_mode_card_${mode.name.lowercase()}"),
        shape = RoundedCornerShape(18.dp),
        color = colors.cardBackground,
        border = BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.blueTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = mode.icon(),
                    contentDescription = mode.displayName,
                    tint = colors.brightBlue,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = mode.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = mode.tagline,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RecentConversationRow(
    conversation: ConversationEntity,
    onClick: () -> Unit
) {
    val colors = VeyroxTheme.colors
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("recent_conversation_${conversation.id}"),
        shape = RoundedCornerShape(16.dp),
        color = colors.cardBackground,
        border = BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.blueTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = conversation.aiMode.icon(),
                    contentDescription = conversation.aiMode.displayName,
                    tint = colors.brightBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = conversation.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = conversation.lastMessagePreview.ifBlank { "Empty conversation" },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AiModePill(mode = conversation.aiMode)
                Text(
                    text = formatConversationDate(conversation.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}
