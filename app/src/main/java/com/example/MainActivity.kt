package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ApkBuilderScreen
import com.example.ui.CodeAndPreviewScreen
import com.example.ui.LlamaOfflineHubScreen
import com.example.ui.PromptForgeScreen
import com.example.ui.StudioViewModel
import com.example.ui.theme.ApkEmerald
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.StudioSurfaceElevated

enum class StudioDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    FORGE("forge", "Prompt Forge", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    IDE("ide", "Code & Preview", Icons.Filled.Code, Icons.Outlined.Code),
    APK("apk", "APK Builder", Icons.Filled.Android, Icons.Outlined.Android),
    LLAMA("llama", "Llama Offline", Icons.Filled.Memory, Icons.Outlined.Memory)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                val studioViewModel: StudioViewModel = viewModel(
                    factory = StudioViewModel.Factory(application)
                )
                StudioProApp(viewModel = studioViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioProApp(viewModel: StudioViewModel) {
    var currentTab by remember { mutableStateOf(StudioDestination.FORGE) }

    // Mandatory BackHandler for secondary tabs to return to FORGE home tab
    BackHandler(enabled = currentTab != StudioDestination.FORGE) {
        currentTab = StudioDestination.FORGE
    }

    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val models by viewModel.models.collectAsStateWithLifecycle()
    val activeModel by viewModel.activeModel.collectAsStateWithLifecycle()
    val buildLogs by viewModel.activeProjectLogs.collectAsStateWithLifecycle()
    val hardwareTelemetry by viewModel.hardwareTelemetry.collectAsStateWithLifecycle()

    val promptInput by viewModel.promptInput.collectAsStateWithLifecycle()
    val appNameInput by viewModel.appNameInput.collectAsStateWithLifecycle()
    val packageNameInput by viewModel.packageNameInput.collectAsStateWithLifecycle()
    val selectedAccentHex by viewModel.selectedAccentHex.collectAsStateWithLifecycle()
    val selectedPermissions by viewModel.selectedPermissions.collectAsStateWithLifecycle()

    val isSynthesizing by viewModel.isSynthesizing.collectAsStateWithLifecycle()
    val streamingReasoningText by viewModel.streamingReasoningText.collectAsStateWithLifecycle()
    val synthesisStage by viewModel.synthesisStage.collectAsStateWithLifecycle()
    val liveTokensPerSec by viewModel.liveTokensPerSec.collectAsStateWithLifecycle()

    val isBuildingApk by viewModel.isBuildingApk.collectAsStateWithLifecycle()
    val buildProgress by viewModel.buildProgress.collectAsStateWithLifecycle()
    val lastBuildResult by viewModel.lastBuildResult.collectAsStateWithLifecycle()
    val statusBannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()

    val temperature by viewModel.temperature.collectAsStateWithLifecycle()
    val contextWindowSize by viewModel.contextWindowSize.collectAsStateWithLifecycle()
    val useLocalhostDaemon by viewModel.useLocalhostDaemon.collectAsStateWithLifecycle()
    val localhostEndpoint by viewModel.localhostEndpoint.collectAsStateWithLifecycle()
    val localhostPingStatus by viewModel.localhostPingStatus.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudioObsidian,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(ElectricCyan.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.DeveloperMode,
                                contentDescription = "Studio Pro V1 Icon",
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "STUDIO PRO V1",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = activeModel?.name ?: "Llama-3.2-3B-Instruct-Q4_K_M.gguf",
                                style = MaterialTheme.typography.labelSmall,
                                color = ApkEmerald
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        color = StudioSurfaceElevated,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable { currentTab = StudioDestination.LLAMA }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(ApkEmerald)
                            )
                            Text(
                                text = "OFFLINE",
                                style = MaterialTheme.typography.labelSmall,
                                color = ApkEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = StudioObsidian,
                tonalElevation = 8.dp
            ) {
                StudioDestination.entries.forEach { dest ->
                    val selected = currentTab == dest
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = dest },
                        icon = {
                            Icon(
                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                contentDescription = dest.label
                            )
                        },
                        label = {
                            Text(
                                text = dest.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = StudioObsidian,
                            selectedTextColor = ElectricCyan,
                            indicatorColor = ElectricCyan
                        ),
                        modifier = Modifier.testTag("nav_tab_${dest.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedVisibility(visible = statusBannerMessage != null) {
                statusBannerMessage?.let { message ->
                    Surface(
                        color = StudioSurfaceElevated,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = ApkEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearStatusBanner() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Dismiss status banner",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (currentTab) {
                    StudioDestination.FORGE -> PromptForgeScreen(
                        promptInput = promptInput,
                        appNameInput = appNameInput,
                        packageNameInput = packageNameInput,
                        selectedAccentHex = selectedAccentHex,
                        selectedPermissions = selectedPermissions,
                        activeModel = activeModel,
                        hardwareTelemetry = hardwareTelemetry,
                        isSynthesizing = isSynthesizing,
                        streamingReasoningText = streamingReasoningText,
                        synthesisStage = synthesisStage,
                        liveTokensPerSec = liveTokensPerSec,
                        projects = projects,
                        activeProject = activeProject,
                        onPromptChange = viewModel::updatePromptInput,
                        onAppNameChange = viewModel::updateAppNameInput,
                        onPackageNameChange = viewModel::updatePackageNameInput,
                        onAccentSelect = viewModel::selectAccentHex,
                        onPermissionToggle = viewModel::togglePermission,
                        onApplyPreset = viewModel::applyPreset,
                        onSynthesizeClick = {
                            viewModel.synthesizeAndBuildFromPrompt()
                        },
                        onSelectProject = viewModel::selectProject,
                        onDeleteProject = viewModel::deleteProject,
                        onNavigateToIde = { currentTab = StudioDestination.IDE },
                        onNavigateToApkBuilder = { currentTab = StudioDestination.APK }
                    )

                    StudioDestination.IDE -> CodeAndPreviewScreen(
                        activeProject = activeProject,
                        onSaveFileContent = viewModel::saveEditedFileContent,
                        onBuildApkClick = {
                            viewModel.triggerApkBuildForActiveProject()
                            currentTab = StudioDestination.APK
                        }
                    )

                    StudioDestination.APK -> ApkBuilderScreen(
                        activeProject = activeProject,
                        isBuildingApk = isBuildingApk,
                        buildProgress = buildProgress,
                        lastBuildResult = lastBuildResult,
                        buildLogs = buildLogs,
                        onTriggerBuild = viewModel::triggerApkBuildForActiveProject,
                        onExportApkUri = viewModel::exportApkToUri,
                        onExportSourceZipUri = viewModel::exportSourceZipToUri
                    )

                    StudioDestination.LLAMA -> LlamaOfflineHubScreen(
                        models = models,
                        hardwareTelemetry = hardwareTelemetry,
                        temperature = temperature,
                        contextWindowSize = contextWindowSize,
                        useLocalhostDaemon = useLocalhostDaemon,
                        localhostEndpoint = localhostEndpoint,
                        localhostPingStatus = localhostPingStatus,
                        onSelectActiveModel = viewModel::setActiveLlamaModel,
                        onImportGgufUri = viewModel::importGgufModelFromUri,
                        onRunBenchmark = viewModel::runModelSpeedBenchmark,
                        onDeleteCustomModel = viewModel::deleteCustomModel,
                        onTemperatureChange = viewModel::updateTemperature,
                        onContextWindowChange = viewModel::updateContextWindow,
                        onToggleLocalhostDaemon = viewModel::toggleLocalhostDaemon,
                        onLocalhostEndpointChange = viewModel::updateLocalhostEndpoint,
                        onPingLocalhost = viewModel::testLocalhostLlamaConnection
                    )
                }
            }
        }
    }
}
