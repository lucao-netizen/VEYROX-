package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.MessageRole
import com.example.domain.model.AiEngineType
import com.example.domain.model.AiMode
import com.example.ui.components.AiModePill
import com.example.ui.components.ConfirmDestructiveDialog
import com.example.ui.components.EngineStatusPill
import com.example.ui.components.MarkdownMessageView
import com.example.ui.components.RenameConversationDialog
import com.example.ui.components.VeyroxGeometricLogo
import com.example.ui.components.formatTimestamp
import com.example.ui.components.icon
import com.example.ui.theme.VeyroxError
import com.example.ui.theme.VeyroxTheme
import com.example.ui.theme.VeyroxWarning
import com.example.ui.viewmodel.ChatUiState
import kotlinx.coroutines.delay

@Composable
fun ChatScreen(
    uiState: ChatUiState,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onStopGeneration: () -> Unit,
    onRetryLast: () -> Unit,
    onDismissError: () -> Unit,
    onSwitchMode: (AiMode) -> Unit,
    onRenameConversation: (String) -> Unit,
    onClearConversation: () -> Unit,
    onDeleteConversation: () -> Unit,
    onStartNewChat: (AiMode) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = VeyroxTheme.colors
    var inputText by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    BackHandler(onBack = onBack)

    LaunchedEffect(uiState.messages.size, uiState.streamingContent.length, uiState.isGenerating) {
        val totalItems = uiState.messages.size + if (uiState.isGenerating) 1 else 0
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    if (showRenameDialog) {
        RenameConversationDialog(
            currentTitle = uiState.conversation?.title ?: "New Chat",
            onDismiss = { showRenameDialog = false },
            onConfirm = { newTitle ->
                showRenameDialog = false
                onRenameConversation(newTitle)
            }
        )
    }

    if (showClearDialog) {
        ConfirmDestructiveDialog(
            title = "Clear Conversation",
            message = "Remove all messages in this conversation? This cannot be undone.",
            confirmLabel = "Clear",
            onDismiss = { showClearDialog = false },
            onConfirm = {
                showClearDialog = false
                onClearConversation()
            }
        )
    }

    if (showDeleteDialog) {
        ConfirmDestructiveDialog(
            title = "Delete Conversation",
            message = "Permanently delete this conversation and its message history?",
            confirmLabel = "Delete",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDeleteConversation()
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("chat_screen")
    ) {
        // Top App Bar
        Surface(
            color = colors.secondaryBackground,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showRenameDialog = true }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = uiState.conversation?.title ?: "New ${uiState.activeMode.displayName}",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = uiState.activeMode.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.brightBlue
                            )
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                            Text(
                                text = uiState.preferences.aiEngineType.badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = { onStartNewChat(uiState.activeMode) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("chat_new_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "New Conversation",
                            tint = colors.textPrimary
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("chat_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "Conversation Options",
                                tint = colors.textPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(colors.cardBackground)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Rename", color = colors.textPrimary) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Edit, null, tint = colors.textSecondary)
                                },
                                onClick = {
                                    menuExpanded = false
                                    showRenameDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear messages", color = colors.textPrimary) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.CleaningServices, null, tint = colors.textSecondary)
                                },
                                onClick = {
                                    menuExpanded = false
                                    showClearDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings", color = colors.textPrimary) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Settings, null, tint = colors.textSecondary)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onOpenSettings()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete chat", color = VeyroxError) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.DeleteOutline, null, tint = VeyroxError)
                                },
                                onClick = {
                                    menuExpanded = false
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
                HorizontalDivider(color = colors.borderSubtle)
            }
        }

        // Transparent Demo Mode Banner
        AnimatedVisibility(visible = uiState.preferences.aiEngineType == AiEngineType.DEMO_MODE) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VeyroxWarning.copy(alpha = 0.14f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DEMO MODE: Simulated offline responses. Not live AI.",
                    style = MaterialTheme.typography.labelSmall,
                    color = VeyroxWarning,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onOpenSettings,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Switch", color = VeyroxWarning, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Messages Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (uiState.messages.isEmpty() && !uiState.isGenerating) {
                ChatEmptyState(
                    activeMode = uiState.activeMode,
                    engineType = uiState.preferences.aiEngineType,
                    onSelectMode = onSwitchMode,
                    onSelectStarter = { prompt ->
                        onSendMessage(prompt)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 760.dp)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 760.dp)
                        .testTag("chat_message_list"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(
                        items = uiState.messages,
                        key = { it.id }
                    ) { msg ->
                        ChatMessageBubble(message = msg)
                    }

                    if (uiState.isGenerating) {
                        item(key = "streaming_bubble") {
                            StreamingAssistantBubble(
                                streamingContent = uiState.streamingContent,
                                isDemo = uiState.preferences.aiEngineType == AiEngineType.DEMO_MODE
                            )
                        }
                    }
                }
            }
        }

        // Recoverable Error Banner
        AnimatedVisibility(visible = uiState.errorMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                color = VeyroxError.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, VeyroxError.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ErrorOutline,
                        contentDescription = "Error",
                        tint = VeyroxError,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    if (uiState.lastFailedPrompt != null) {
                        TextButton(
                            onClick = onRetryLast,
                            modifier = Modifier.testTag("chat_retry_button")
                        ) {
                            Icon(Icons.Rounded.Refresh, null, tint = colors.brightBlue, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Retry", color = colors.brightBlue)
                        }
                    } else {
                        TextButton(onClick = onDismissError) {
                            Text("Dismiss", color = colors.textSecondary)
                        }
                    }
                }
            }
        }

        // Bottom Composer
        Surface(
            color = colors.secondaryBackground,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                HorizontalDivider(color = colors.borderSubtle)
                Row(
                    modifier = Modifier
                        .widthIn(max = 760.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Message ${uiState.activeMode.displayName}...",
                                color = colors.textSecondary
                            )
                        },
                        maxLines = 5,
                        shape = RoundedCornerShape(22.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.cardBackground,
                            unfocusedContainerColor = colors.cardBackground,
                            focusedBorderColor = colors.primaryBlue,
                            unfocusedBorderColor = colors.borderSubtle,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (!uiState.isGenerating && inputText.isNotBlank()) {
                                    val msg = inputText
                                    inputText = ""
                                    onSendMessage(msg)
                                }
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field")
                    )

                    if (uiState.isGenerating) {
                        IconButton(
                            onClick = onStopGeneration,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(VeyroxError)
                                .testTag("chat_stop_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Stop,
                                contentDescription = "Stop generation",
                                tint = Color.White
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                val msg = inputText
                                if (msg.isNotBlank()) {
                                    inputText = ""
                                }
                                onSendMessage(msg)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (inputText.isNotBlank()) colors.primaryBlue
                                    else colors.elevatedSurface
                                )
                                .testTag("chat_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Send,
                                contentDescription = "Send message",
                                tint = if (inputText.isNotBlank()) Color.White else colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatEmptyState(
    activeMode: AiMode,
    engineType: AiEngineType,
    onSelectMode: (AiMode) -> Unit,
    onSelectStarter: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = VeyroxTheme.colors
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            VeyroxGeometricLogo(size = 56.dp)
        }
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EngineStatusPill(engineType = engineType)
                Text(
                    text = activeMode.displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.textPrimary
                )
                Text(
                    text = activeMode.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
        }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AiMode.entries.forEach { mode ->
                    val selected = mode == activeMode
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectMode(mode) },
                        label = { Text(mode.shortLabel) },
                        leadingIcon = {
                            Icon(mode.icon(), null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.primaryBlue,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = colors.cardBackground,
                            labelColor = colors.textSecondary
                        )
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "TRY A PROMPT",
                style = MaterialTheme.typography.labelMedium,
                color = colors.brightBlue,
                letterSpacing = 1.4.sp
            )
        }
        items(activeMode.starterPrompts) { prompt ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelectStarter(prompt) }
                    .testTag("starter_prompt_card"),
                shape = RoundedCornerShape(16.dp),
                color = colors.cardBackground,
                border = BorderStroke(1.dp, colors.borderSubtle)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = activeMode.icon(),
                        contentDescription = null,
                        tint = colors.brightBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(message: MessageEntity) {
    val colors = VeyroxTheme.colors
    val isUser = message.messageRole == MessageRole.USER
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1600L)
            copied = false
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp, end = 10.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(colors.blueTint),
                contentAlignment = Alignment.Center
            ) {
                VeyroxGeometricLogo(size = 20.dp, showGlow = false)
            }
        }

        Column(
            modifier = Modifier.widthIn(max = 560.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                ),
                color = if (isUser) colors.userBubble else colors.assistantBubble,
                border = if (isUser) null else BorderStroke(1.dp, colors.borderSubtle)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    if (isUser) {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    } else {
                        if (message.isDemoResponse) {
                            Text(
                                text = "DEMO RESPONSE",
                                style = MaterialTheme.typography.labelSmall,
                                color = VeyroxWarning,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        MarkdownMessageView(
                            markdown = message.content,
                            textColor = colors.textPrimary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
                IconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(message.content))
                        copied = true
                    },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("copy_message_${message.id}")
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                        contentDescription = if (copied) "Copied" else "Copy message",
                        tint = if (copied) colors.brightBlue else colors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StreamingAssistantBubble(
    streamingContent: String,
    isDemo: Boolean
) {
    val colors = VeyroxTheme.colors
    val pulse = rememberInfiniteTransition(label = "typing_pulse")
    val alpha by pulse.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp, end = 10.dp)
                .size(30.dp)
                .clip(CircleShape)
                .background(colors.blueTint),
            contentAlignment = Alignment.Center
        ) {
            VeyroxGeometricLogo(size = 20.dp, showGlow = false)
        }

        Surface(
            modifier = Modifier.widthIn(max = 560.dp),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = 4.dp,
                bottomEnd = 18.dp
            ),
            color = colors.assistantBubble,
            border = BorderStroke(1.dp, colors.primaryBlue.copy(alpha = 0.45f))
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                if (streamingContent.isBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.alpha(alpha)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colors.brightBlue)
                        )
                        Text(
                            text = if (isDemo) "Generating demo response..." else "VEYROX is thinking...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                } else {
                    MarkdownMessageView(
                        markdown = streamingContent,
                        textColor = colors.textPrimary
                    )
                }
            }
        }
    }
}
