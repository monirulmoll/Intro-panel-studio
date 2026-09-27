package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ApkProjectEntity
import com.example.data.LlamaModelEntity
import com.example.engine.DeviceHardwareTelemetry
import com.example.ui.theme.ApkEmerald
import com.example.ui.theme.CompilerAmber
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.NeuralViolet
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.StudioSurfaceElevated

private val ACCENT_SWATCHES = listOf(
    "#00E5FF" to "Cyan",
    "#10B981" to "Emerald",
    "#8B5CF6" to "Violet",
    "#FFB300" to "Amber",
    "#F43F5E" to "Rose",
    "#3B82F6" to "Cobalt"
)

private val AVAILABLE_PERMISSIONS = listOf(
    "INTERNET",
    "VIBRATE",
    "CAMERA",
    "POST_NOTIFICATIONS",
    "ACCESS_FINE_LOCATION"
)

@Composable
fun PromptForgeScreen(
    promptInput: String,
    appNameInput: String,
    packageNameInput: String,
    selectedAccentHex: String,
    selectedPermissions: List<String>,
    activeModel: LlamaModelEntity?,
    hardwareTelemetry: DeviceHardwareTelemetry,
    isSynthesizing: Boolean,
    streamingReasoningText: String,
    synthesisStage: String,
    liveTokensPerSec: Float,
    projects: List<ApkProjectEntity>,
    activeProject: ApkProjectEntity?,
    onPromptChange: (String) -> Unit,
    onAppNameChange: (String) -> Unit,
    onPackageNameChange: (String) -> Unit,
    onAccentSelect: (String) -> Unit,
    onPermissionToggle: (String) -> Unit,
    onApplyPreset: (PromptBlueprintPreset) -> Unit,
    onSynthesizeClick: () -> Unit,
    onSelectProject: (ApkProjectEntity) -> Unit,
    onDeleteProject: (Long) -> Unit,
    onNavigateToIde: () -> Unit,
    onNavigateToApkBuilder: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("prompt_forge_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Banner with Offline Llama Telemetry
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(176.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_llama_engine_banner_1790514370993),
                        contentDescription = "Studio Pro V1 Offline Llama Engine Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        StudioObsidian.copy(alpha = 0.45f),
                                        StudioObsidian.copy(alpha = 0.92f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = ApkEmerald.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ApkEmerald)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(ApkEmerald)
                                    )
                                    Text(
                                        text = "LLAMA OFFLINE • READY",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ApkEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                color = StudioObsidian.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "${"%.1f".format(liveTokensPerSec)} tok/s • ${hardwareTelemetry.primaryAbi}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricCyan,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "STUDIO PRO V1",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Active Model: ${activeModel?.name ?: "Llama-3.2-3B-Instruct-Q4_K_M.gguf"} (${activeModel?.quantization ?: "Q4_K_M"})",
                                style = MaterialTheme.typography.labelMedium,
                                color = ElectricCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Prompt -> Kotlin Compose AST -> DEX Bytecode -> Signed .APK",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 2. Quick Blueprint Presets
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QUICK APK BLUEPRINTS",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan
                    )
                    Text(
                        text = "Tap to load prompt",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DEFAULT_PROMPT_PRESETS.forEach { preset ->
                        AssistChip(
                            onClick = { onApplyPreset(preset) },
                            label = { Text(preset.title) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.testTag("preset_${preset.appName}")
                        )
                    }
                }
            }
        }

        // 3. Prompt & Package Configuration Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Terminal,
                            contentDescription = null,
                            tint = ElectricCyan
                        )
                        Text(
                            text = "Prompt-to-APK Specification",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = onPromptChange,
                        label = { Text("Describe your Android App Prompt") },
                        placeholder = { Text("e.g., Build an offline expense tracker with category filters...") },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prompt_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = appNameInput,
                            onValueChange = onAppNameChange,
                            label = { Text("APK App Name") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("app_name_input")
                        )
                        OutlinedTextField(
                            value = packageNameInput,
                            onValueChange = onPackageNameChange,
                            label = { Text("Package ID") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("package_name_input")
                        )
                    }

                    // Theme Accent Swatches
                    Text(
                        text = "APK Primary Theme Accent",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ACCENT_SWATCHES.forEach { (hex, label) ->
                            val selected = selectedAccentHex.equals(hex, ignoreCase = true)
                            val parsedColor = parseHexColorSafe(hex)
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (selected) parsedColor.copy(alpha = 0.22f)
                                else StudioSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) parsedColor else Color.Transparent
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onAccentSelect(hex) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(parsedColor)
                                    )
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }

                    // Manifest Permissions
                    Text(
                        text = "AndroidManifest.xml Permissions",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AVAILABLE_PERMISSIONS.forEach { perm ->
                            val isSelected = selectedPermissions.contains(perm)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onPermissionToggle(perm) },
                                label = { Text(perm) },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }

                    Button(
                        onClick = onSynthesizeClick,
                        enabled = !isSynthesizing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("synthesize_apk_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = StudioObsidian
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSynthesizing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = StudioObsidian
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = synthesisStage,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(Icons.Default.Bolt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SYNTHESIZE CODE & BUILD APK (OFFLINE LLAMA)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 4. Offline Llama Live Reasoning & Token Stream Output
        item {
            AnimatedVisibility(visible = streamingReasoningText.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF070A12)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        NeuralViolet.copy(alpha = 0.55f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = NeuralViolet,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "OFFLINE LLAMA TOKEN STREAM",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = NeuralViolet
                                )
                            }
                            Text(
                                text = synthesisStage,
                                style = MaterialTheme.typography.labelSmall,
                                color = CompilerAmber
                            )
                        }

                        Text(
                            text = streamingReasoningText,
                            fontFamily = JetBrainsMonoFamily,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFE2E8F0)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = onNavigateToIde) {
                                Icon(
                                    Icons.Default.Code,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open Code & Live Preview")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = onNavigateToApkBuilder) {
                                Icon(
                                    Icons.Default.Android,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Inspect Signed APK")
                            }
                        }
                    }
                }
            }
        }

        // 5. Workspace APK Projects List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WORKSPACE APK PROJECTS (${projects.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = ElectricCyan
                )
                Text(
                    text = "Stored in Room DB",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(projects, key = { it.id }) { project ->
            val isSelected = activeProject?.id == project.id
            val accentColor = parseHexColorSafe(project.accentHex)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) StudioSurfaceElevated else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectProject(project) }
                    .testTag("project_card_${project.id}")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(accentColor.copy(alpha = 0.2f))
                                    .border(1.dp, accentColor, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Android,
                                    contentDescription = null,
                                    tint = accentColor
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = project.appName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isSelected) {
                                        Surface(
                                            color = ElectricCyan.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ElectricCyan,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${project.packageName} • v${project.versionName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (projects.size > 1) {
                            IconButton(onClick = { onDeleteProject(project.id) }) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Project",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Text(
                        text = project.prompt,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (project.apkSizeBytes > 0) {
                                "Signed APK: ${project.apkSizeBytes} B • DEX ${project.dexChecksum}"
                            } else {
                                "Source Ready • Tap to Compile APK"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (project.apkSizeBytes > 0) ApkEmerald else CompilerAmber
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AssistChip(
                                onClick = {
                                    onSelectProject(project)
                                    onNavigateToIde()
                                },
                                label = { Text("Code & Preview") }
                            )
                            AssistChip(
                                onClick = {
                                    onSelectProject(project)
                                    onNavigateToApkBuilder()
                                },
                                label = { Text("APK") }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun parseHexColorSafe(hex: String): Color {
    return try {
        val clean = hex.trim().removePrefix("#")
        val longVal = clean.toLong(16)
        Color(0xFF000000 or longVal)
    } catch (_: Exception) {
        ElectricCyan
    }
}
