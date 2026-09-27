package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ApkProjectEntity
import com.example.engine.OfflineLlamaEngine
import com.example.ui.theme.ApkEmerald
import com.example.ui.theme.CompilerAmber
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.StudioSurfaceElevated

private val IDE_FILES = listOf(
    "MainActivity.kt",
    "AndroidManifest.xml",
    "build.gradle.kts",
    "strings.xml",
    "Theme.kt"
)

@Composable
fun CodeAndPreviewScreen(
    activeProject: ApkProjectEntity?,
    onSaveFileContent: (fileType: String, content: String) -> Unit,
    onBuildApkClick: () -> Unit
) {
    if (activeProject == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active APK project. Create one in Prompt Forge.")
        }
        return
    }

    var selectedMode by remember { mutableIntStateOf(0) } // 0 = Live App Preview, 1 = Code Editor
    var selectedFile by remember { mutableStateOf(IDE_FILES[0]) }

    val currentFileSource = remember(activeProject, selectedFile) {
        when (selectedFile) {
            "MainActivity.kt" -> activeProject.mainActivityKt
            "AndroidManifest.xml" -> activeProject.manifestXml
            "build.gradle.kts" -> activeProject.buildGradleKts
            "strings.xml" -> activeProject.stringsXml
            "Theme.kt" -> activeProject.themeKt
            else -> activeProject.mainActivityKt
        }
    }

    var editorText by remember(currentFileSource) { mutableStateOf(currentFileSource) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header & Mode Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activeProject.appName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${activeProject.packageName} • SDK ${activeProject.targetSdk}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricCyan
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedMode == 0,
                    onClick = { selectedMode = 0 },
                    label = { Text("Live App Simulator") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("mode_live_simulator")
                )
                FilterChip(
                    selected = selectedMode == 1,
                    onClick = { selectedMode = 1 },
                    label = { Text("Code IDE") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Code,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("mode_code_ide")
                )
            }
        }

        if (selectedMode == 0) {
            InteractiveDeviceSimulator(
                project = activeProject,
                onSwitchToCode = { selectedMode = 1 },
                onCompileApk = onBuildApkClick,
                modifier = Modifier.weight(1f)
            )
        } else {
            // File Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IDE_FILES.forEach { fileName ->
                    val isSelected = selectedFile == fileName
                    AssistChip(
                        onClick = { selectedFile = fileName },
                        label = {
                            Text(
                                text = fileName,
                                color = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("file_tab_$fileName")
                    )
                }
            }

            // Monospace Code Editor Box
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070A12)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceElevated),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StudioSurfaceElevated)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$selectedFile • ${editorText.lines().size} lines • ${editorText.length} chars",
                            style = MaterialTheme.typography.labelSmall,
                            color = CompilerAmber
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { onSaveFileContent(selectedFile, editorText) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("save_code_button")
                            ) {
                                Icon(
                                    Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save File", style = MaterialTheme.typography.labelSmall)
                            }
                            Button(
                                onClick = {
                                    onSaveFileContent(selectedFile, editorText)
                                    onBuildApkClick()
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("rebuild_from_ide_button")
                            ) {
                                Icon(
                                    Icons.Default.Build,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Build APK", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editorText,
                        onValueChange = { editorText = it },
                        textStyle = TextStyle(
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("code_editor_input")
                    )
                }
            }
        }
    }
}

@Composable
private fun InteractiveDeviceSimulator(
    project: ApkProjectEntity,
    onSwitchToCode: () -> Unit,
    onCompileApk: () -> Unit,
    modifier: Modifier = Modifier
) {
    val analysis = remember(project.prompt, project.appName) {
        OfflineLlamaEngine.analyzePrompt(project.prompt, project.appName)
    }
    val accentColor = remember(project.accentHex) {
        parseHexColorSafe(project.accentHex)
    }

    var simItems by remember(project.id, project.prompt) {
        mutableStateOf(analysis.defaultItems)
    }
    var newEntryTitle by remember { mutableStateOf("") }
    var newEntryMetric by remember { mutableStateOf("") }
    var searchFilter by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Phone Device Frame
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1322)),
            border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFF334155)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Virtual Android Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioObsidian)
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "09:41 • LIVE APK SANDBOX",
                        style = MaterialTheme.typography.labelSmall,
                        color = ApkEmerald
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 48.dp, height = 6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF334155))
                    )
                    Text(
                        text = "API ${project.targetSdk} • 100%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Simulated App TopBar
                Surface(
                    color = accentColor.copy(alpha = 0.16f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = project.appName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = analysis.secondaryFeature,
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor
                            )
                        }
                        Surface(
                            color = accentColor.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "${simItems.size} ${analysis.primaryEntityName}s",
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Simulated App Interactive Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = analysis.summaryDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Interactive Add Row inside Virtual Device
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newEntryTitle,
                            onValueChange = { newEntryTitle = it },
                            label = { Text(analysis.primaryActionVerb) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sim_entry_title_input")
                        )
                        OutlinedTextField(
                            value = newEntryMetric,
                            onValueChange = { newEntryMetric = it },
                            label = { Text(analysis.metricUnit) },
                            singleLine = true,
                            modifier = Modifier.width(100.dp)
                        )
                        FilledIconButton(
                            onClick = {
                                if (newEntryTitle.isNotBlank()) {
                                    val badge = newEntryMetric.ifBlank { "Verified" }
                                    simItems = listOf(newEntryTitle.trim() to badge) + simItems
                                    newEntryTitle = ""
                                    newEntryMetric = ""
                                }
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = accentColor,
                                contentColor = StudioObsidian
                            ),
                            modifier = Modifier.testTag("sim_add_item_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Item in Simulator")
                        }
                    }

                    OutlinedTextField(
                        value = searchFilter,
                        onValueChange = { searchFilter = it },
                        label = { Text("Search ${analysis.primaryEntityName}s...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        val filtered = simItems.filter {
                            searchFilter.isBlank() ||
                                it.first.contains(searchFilter, ignoreCase = true) ||
                                it.second.contains(searchFilter, ignoreCase = true)
                        }
                        items(filtered) { item ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = StudioSurfaceElevated
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column {
                                            Text(
                                                text = item.first,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = item.second,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = accentColor
                                            )
                                        }
                                    }
                                    IconButton(onClick = { simItems = simItems - item }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove Item",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Quick Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onSwitchToCode,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.EditNote, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Edit Source Code")
            }
            Button(
                onClick = onCompileApk,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ApkEmerald,
                    contentColor = StudioObsidian
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Android, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Package Signed APK", fontWeight = FontWeight.Bold)
            }
        }
    }
}
