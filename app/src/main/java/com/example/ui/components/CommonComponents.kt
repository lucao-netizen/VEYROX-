package com.example.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Summarize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.config.VeyroxConfig
import com.example.domain.model.AiEngineType
import com.example.domain.model.AiMode
import com.example.ui.theme.VeyroxDiscordBlurple
import com.example.ui.theme.VeyroxError
import com.example.ui.theme.VeyroxSuccess
import com.example.ui.theme.VeyroxTheme
import com.example.ui.theme.VeyroxWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun AiMode.icon(): ImageVector = when (this) {
    AiMode.GENERAL -> Icons.Rounded.AutoAwesome
    AiMode.WRITING -> Icons.Rounded.EditNote
    AiMode.CODING -> Icons.Rounded.Code
    AiMode.SUMMARIZER -> Icons.Rounded.Summarize
}

fun openVeyroxDiscord(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(VeyroxConfig.VEYROX_DISCORD_URL)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "No browser or Discord app available to open invite link.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
fun DiscordGlyph(
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val body = Path().apply {
            moveTo(w * 0.22f, h * 0.22f)
            lineTo(w * 0.38f, h * 0.18f)
            lineTo(w * 0.43f, h * 0.25f)
            lineTo(w * 0.57f, h * 0.25f)
            lineTo(w * 0.62f, h * 0.18f)
            lineTo(w * 0.78f, h * 0.22f)
            lineTo(w * 0.90f, h * 0.72f)
            lineTo(w * 0.72f, h * 0.82f)
            lineTo(w * 0.64f, h * 0.72f)
            lineTo(w * 0.36f, h * 0.72f)
            lineTo(w * 0.28f, h * 0.82f)
            lineTo(w * 0.10f, h * 0.72f)
            close()
        }
        drawPath(body, color = tint)
        drawCircle(
            color = Color(0xFF151A24),
            radius = w * 0.085f,
            center = androidx.compose.ui.geometry.Offset(w * 0.38f, h * 0.52f)
        )
        drawCircle(
            color = Color(0xFF151A24),
            radius = w * 0.085f,
            center = androidx.compose.ui.geometry.Offset(w * 0.62f, h * 0.52f)
        )
    }
}

@Composable
fun AiModePill(
    mode: AiMode,
    modifier: Modifier = Modifier
) {
    val colors = VeyroxTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(colors.blueTint)
            .border(1.dp, colors.primaryBlue.copy(alpha = 0.35f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = mode.icon(),
            contentDescription = null,
            tint = colors.brightBlue,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = mode.shortLabel,
            style = MaterialTheme.typography.labelMedium,
            color = colors.brightBlue
        )
    }
}

@Composable
fun EngineStatusPill(
    engineType: AiEngineType,
    modifier: Modifier = Modifier
) {
    val (accent, label) = when (engineType) {
        AiEngineType.FIREBASE_AI_LOGIC -> VeyroxSuccess to engineType.badge
        AiEngineType.DEMO_MODE -> VeyroxWarning to engineType.badge
        AiEngineType.CUSTOM_BACKEND -> VeyroxTheme.colors.brightBlue to engineType.badge
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(accent.copy(alpha = 0.14f))
            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(50))
                .background(accent)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun DiscordCtaButton(
    label: String = "Join VEYROX Discord",
    testTag: String = "discord_button",
    modifier: Modifier = Modifier,
    outlined: Boolean = false
) {
    val context = LocalContext.current
    val colors = VeyroxTheme.colors

    if (outlined) {
        OutlinedButton(
            onClick = { openVeyroxDiscord(context) },
            modifier = modifier
                .height(48.dp)
                .testTag(testTag),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, VeyroxDiscordBlurple.copy(alpha = 0.7f)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.textPrimary
            ),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            DiscordGlyph(tint = VeyroxDiscordBlurple)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = "Opens external Discord invite",
                modifier = Modifier.size(16.dp),
                tint = colors.textSecondary
            )
        }
    } else {
        Button(
            onClick = { openVeyroxDiscord(context) },
            modifier = modifier
                .height(48.dp)
                .testTag(testTag),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VeyroxDiscordBlurple,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            DiscordGlyph(tint = Color.White)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = "Opens external Discord invite",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun DiscordCommunityCard(
    testTag: String = "discord_community_card",
    modifier: Modifier = Modifier
) {
    val colors = VeyroxTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBackground,
        border = BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "COMMUNITY",
                style = MaterialTheme.typography.labelMedium,
                color = colors.brightBlue,
                letterSpacing = 1.4.sp
            )
            Text(
                text = "BUILD THE FUTURE WITH VEYROX.",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary
            )
            Text(
                text = "Join the VEYROX community, share ideas, discover updates and connect with other users.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            DiscordCtaButton(
                label = "Join VEYROX Discord",
                testTag = testTag,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun RenameConversationDialog(
    currentTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var draft by remember(currentTitle) { mutableStateOf(currentTitle) }
    val colors = VeyroxTheme.colors

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.secondaryBackground,
        titleContentColor = colors.textPrimary,
        textContentColor = colors.textSecondary,
        title = { Text("Rename Conversation") },
        text = {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                singleLine = true,
                label = { Text("Conversation Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rename_title_input")
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (draft.isNotBlank()) {
                        onConfirm(draft.trim())
                    }
                },
                enabled = draft.isNotBlank(),
                modifier = Modifier.testTag("rename_confirm_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ConfirmDestructiveDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val colors = VeyroxTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.secondaryBackground,
        titleContentColor = colors.textPrimary,
        textContentColor = colors.textSecondary,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VeyroxError,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("confirm_destructive_button")
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

fun formatConversationDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
