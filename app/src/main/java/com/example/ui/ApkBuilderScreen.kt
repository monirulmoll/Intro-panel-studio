package com.example.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ApkProjectEntity
import com.example.data.BuildLogEntity
import com.example.engine.ApkBuildResult
import com.example.ui.theme.ApkEmerald
import com.example.ui.theme.CompilerAmber
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.NeuralViolet
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.StudioSurfaceElevated
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PIPELINE_STAGES = listOf(
    "LLAMA-AST" to "Kotlin AST",
    "AAPT2" to "Resources",
    "D8-DEX" to "DEX Code",
    "APK-PACKAGER" to "Zip Align",
    "APK-SIGNER" to "SHA-256 Sign"
)

@Composable
fun ApkBuilderScreen(
    activeProject: ApkProjectEntity?,
    isBuildingApk: Boolean,
    buildProgress: Float,
    lastBuildResult: ApkBuildResult?,
    buildLogs: List<BuildLogEntity>,
    onTriggerBuild: () -> Unit,
    onExportApkUri: (android.net.Uri) -> Unit,
    onExportSourceZipUri: (android.net.Uri) -> Unit
) {
    val context = LocalContext.current

    val createApkDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.android.package-archive")
    ) { uri ->
        if (uri != null) {
            onExportApkUri(uri)
        }
    }

    val createZipDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            onExportSourceZipUri(uri)
        }
    }

    if (activeProject == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active project selected.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("apk_builder_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Project & APK Pipeline Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${activeProject.appName} APK Packager",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${activeProject.packageName} • minSdk ${activeProject.minSdk} • targetSdk ${activeProject.targetSdk}",
                                style = MaterialTheme.typography.labelMedium,
                                color = ElectricCyan
                            )
                        }

                        Surface(
                            color = if (activeProject.apkSizeBytes > 0) ApkEmerald.copy(alpha = 0.2f)
                            else CompilerAmber.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = if (activeProject.apkSizeBytes > 0) "SIGNED APK READY" else "NEEDS BUILD",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (activeProject.apkSizeBytes > 0) ApkEmerald else CompilerAmber,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // 5-Stage Pipeline Stepper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PIPELINE_STAGES.forEachIndexed { index, (_, label) ->
                            val stageThreshold = (index + 1) * 0.19f
                            val isDone = (!isBuildingApk && activeProject.apkSizeBytes > 0) ||
                                (isBuildingApk && buildProgress >= stageThreshold)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isDone) ApkEmerald else StudioSurfaceElevated
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isDone) Icons.Default.Check else Icons.Default.Memory,
                                        contentDescription = null,
                                        tint = if (isDone) StudioObsidian else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDone) ApkEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (isBuildingApk) {
                        LinearProgressIndicator(
                            progress = { buildProgress },
                            color = ElectricCyan,
                            trackColor = StudioSurfaceElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }

                    // Primary Compile & Export Buttons
                    Button(
                        onClick = onTriggerBuild,
                        enabled = !isBuildingApk,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = StudioObsidian
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("compile_apk_button")
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBuildingApk) "COMPILING DEX & SIGNING APK..."
                            else "COMPILE & SIGN .APK NOW",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val slug = activeProject.packageName.replace('.', '_')
                                createApkDocumentLauncher.launch("${slug}-v${activeProject.versionName}.apk")
                            },
                            enabled = activeProject.apkSizeBytes > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ApkEmerald,
                                contentColor = StudioObsidian
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_apk_button")
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save .APK", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val slug = activeProject.packageName.replace('.', '_')
                                createZipDocumentLauncher.launch("${slug}-android-studio-src.zip")
                            },
                            enabled = activeProject.apkSizeBytes > 0,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_zip_button")
                        ) {
                            Icon(
                                Icons.Default.FolderZip,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Source .ZIP")
                        }

                        IconButton(
                            onClick = {
                                val summary = buildString {
                                    appendLine("Studio Pro V1 — Signed APK Package Report")
                                    appendLine("App: ${activeProject.appName} (${activeProject.packageName})")
                                    appendLine("Model: ${activeProject.modelUsed}")
                                    appendLine("APK Size: ${activeProject.apkSizeBytes} bytes")
                                    appendLine("DEX Checksum: ${activeProject.dexChecksum}")
                                    appendLine("SHA-256: ${activeProject.apkSha256}")
                                }
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, summary)
                                }
                                context.startActivity(
                                    Intent.createChooser(sendIntent, "Share APK Build Report")
                                )
                            }
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share APK Build Info",
                                tint = ElectricCyan
                            )
                        }
                    }
                }
            }
        }

        // 2. Binary APK & DEX Header Telemetry Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = ApkEmerald
                        )
                        Text(
                            text = "APK Binary & Dalvik DEX Inspector",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BinaryMetricTile(
                            label = "APK SIZE",
                            value = "${activeProject.apkSizeBytes} B",
                            accent = ElectricCyan,
                            modifier = Modifier.weight(1f)
                        )
                        BinaryMetricTile(
                            label = "DEX ADLER32",
                            value = activeProject.dexChecksum.ifBlank { "Pending" },
                            accent = CompilerAmber,
                            modifier = Modifier.weight(1f)
                        )
                        BinaryMetricTile(
                            label = "DEX HEADER",
                            value = "dex\\n035\\0",
                            accent = NeuralViolet,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = "APK SHA-256 Digest: ${activeProject.apkSha256.ifBlank { "Run compiler to generate SHA-256 signature" }}",
                        fontFamily = JetBrainsMonoFamily,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (lastBuildResult != null && lastBuildResult.entries.isNotEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Text(
                            text = "PACKAGED APK ZIP ENTRIES (${lastBuildResult.entries.size} files)",
                            style = MaterialTheme.typography.labelLarge,
                            color = ElectricCyan
                        )
                        lastBuildResult.entries.forEach { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.path,
                                        fontFamily = JetBrainsMonoFamily,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${entry.category} • SHA256: ${entry.sha256DigestBase64.take(20)}...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${entry.uncompressedBytes} B",
                                    fontFamily = JetBrainsMonoFamily,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ApkEmerald
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Compiler Console Output Logs
        item {
            Text(
                text = "COMPILER & SIGNER TERMINAL LOGS (${buildLogs.size})",
                style = MaterialTheme.typography.labelLarge,
                color = ElectricCyan
            )
        }

        items(buildLogs, key = { it.id }) { log ->
            val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
            val badgeColor = when (log.level) {
                "SUCCESS" -> ApkEmerald
                "WARN" -> CompilerAmber
                else -> ElectricCyan
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070A12)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "[${timeFormat.format(Date(log.timestamp))}]",
                        fontFamily = JetBrainsMonoFamily,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "[${log.stage}]",
                        fontFamily = JetBrainsMonoFamily,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = log.message,
                        fontFamily = JetBrainsMonoFamily,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE2E8F0),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BinaryMetricTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = StudioObsidian,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontFamily = JetBrainsMonoFamily,
                style = MaterialTheme.typography.labelLarge,
                color = accent,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
