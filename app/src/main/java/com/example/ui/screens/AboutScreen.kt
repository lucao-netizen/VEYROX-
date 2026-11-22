package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.config.VeyroxConfig
import com.example.domain.model.AiMode
import com.example.ui.components.DiscordCommunityCard
import com.example.ui.components.VeyroxGeometricLogo
import com.example.ui.components.icon
import com.example.ui.theme.VeyroxTheme

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = VeyroxTheme.colors
    BackHandler(onBack = onBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .testTag("about_screen")
    ) {
        Surface(color = colors.secondaryBackground, modifier = Modifier.fillMaxWidth()) {
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
                            .testTag("about_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                    Text(
                        text = "About VEYROX",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary
                    )
                }
                HorizontalDivider(color = colors.borderSubtle)
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 820.dp),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = colors.cardBackground,
                        border = BorderStroke(1.dp, colors.borderSubtle)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            VeyroxGeometricLogo(size = 64.dp)
                            Text(
                                text = VeyroxConfig.APP_NAME,
                                style = MaterialTheme.typography.headlineLarge,
                                color = colors.textPrimary,
                                letterSpacing = 3.sp
                            )
                            Text(
                                text = VeyroxConfig.BRAND_SLOGAN,
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.brightBlue,
                                letterSpacing = 1.6.sp
                            )
                            Text(
                                text = "VEYROX is a modern AI platform engineered around simplicity, focus, creativity, and reliable intelligence—without clutter or exposed client credentials.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = colors.cardBackground,
                        border = BorderStroke(1.dp, colors.borderSubtle)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "SPECIALIZED AI MODES",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.brightBlue,
                                letterSpacing = 1.4.sp
                            )
                            AiMode.entries.forEach { mode ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = mode.icon(),
                                        contentDescription = null,
                                        tint = colors.brightBlue,
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = mode.displayName,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = mode.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    DiscordCommunityCard(testTag = "about_discord_button")
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = colors.cardBackground,
                        border = BorderStroke(1.dp, colors.borderSubtle)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "PLATFORM & VERSION",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.brightBlue,
                                letterSpacing = 1.4.sp
                            )
                            Text(
                                text = "Version ${VeyroxConfig.APP_VERSION} (${VeyroxConfig.BUILD_CHANNEL})",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Architecture: Kotlin, Jetpack Compose, Material 3, Room Persistence, DataStore, Firebase AI Logic (Gemini 3.8 Flash) & App Check.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Legal & Privacy Notice Placeholder: VEYROX v1.0 demonstration platform. Configure your own privacy policy and terms URL before public store distribution.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
