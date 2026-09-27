package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.LlamaModelEntity
import com.example.engine.DeviceHardwareTelemetry
import com.example.ui.theme.ApkEmerald
import com.example.ui.theme.CompilerAmber
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.NeuralViolet
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.StudioSurfaceElevated

private val CONTEXT_WINDOW_OPTIONS = listOf(2048, 4096, 8192, 16384)

@Composable
fun LlamaOfflineHubScreen(
    models: List<LlamaModelEntity>,
    hardwareTelemetry: DeviceHardwareTelemetry,
    temperature: Float,
    contextWindowSize: Int,
    useLocalhostDaemon: Boolean,
    localhostEndpoint: String,
    localhostPingStatus: String,
    onSelectActiveModel: (LlamaModelEntity) -> Unit,
    onImportGgufUri: (android.net.Uri) -> Unit,
    onRunBenchmark: (LlamaModelEntity) -> Unit,
    onDeleteCustomModel: (LlamaModelEntity) -> Unit,
    onTemperatureChange: (Float) -> Unit,
    onContextWindowChange: (Int) -> Unit,
    onToggleLocalhostDaemon: (Boolean) -> Unit,
    onLocalhostEndpointChange: (String) -> Unit,
    onPingLocalhost: () -> Unit
) {
    val openGgufLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportGgufUri(uri)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("llama_offline_hub_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Real Device Hardware & RAM Telemetry Card
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Memory,
                                contentDescription = null,
                                tint = ElectricCyan
                            )
                            Column {
                                Text(
                                    text = "Device Neural & Memory Telemetry",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${hardwareTelemetry.deviceModel} • ${hardwareTelemetry.primaryAbi}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            color = ApkEmerald.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "${hardwareTelemetry.cpuCores} CPU Cores",
                                style = MaterialTheme.typography.labelSmall,
                                color = ApkEmerald,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // RAM Usage Bar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Available RAM for GGUF KV Cache: ${hardwareTelemetry.availableRamMb} MB",
                                style = MaterialTheme.typography.labelMedium,
                                color = ElectricCyan
                            )
                            Text(
                                text = "Total: ${hardwareTelemetry.totalRamMb} MB (${hardwareTelemetry.usedRamPercent}% used)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        LinearProgressIndicator(
                            progress = { hardwareTelemetry.usedRamPercent / 100f },
                            color = if (hardwareTelemetry.usedRamPercent > 85) CompilerAmber else ElectricCyan,
                            trackColor = StudioSurfaceElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }

                    Button(
                        onClick = { openGgufLauncher.launch(arrayOf("*/*")) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeuralViolet,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("import_gguf_button")
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "IMPORT LOCAL .GGUF MODEL FROM DEVICE",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Registered Offline Llama Models
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OFFLINE LLAMA GGUF REGISTRY (${models.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = ElectricCyan
                )
                Text(
                    text = "Binary Header Verified",
                    style = MaterialTheme.typography.labelSmall,
                    color = ApkEmerald
                )
            }
        }

        items(models, key = { it.id }) { model ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (model.isActive) StudioSurfaceElevated
                    else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (model.isActive) 1.5.dp else 1.dp,
                    color = if (model.isActive) ElectricCyan
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("llama_model_card_${model.id}")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = model.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (model.isActive) {
                                    Surface(
                                        color = ApkEmerald.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ApkEmerald,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${model.family} • ${model.parametersBillions}B Params • ${model.quantization}",
                                style = MaterialTheme.typography.labelMedium,
                                color = ElectricCyan
                            )
                        }

                        if (!model.isEmbeddedEngine) {
                            IconButton(onClick = { onDeleteCustomModel(model) }) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "Remove Custom Model"
                                )
                            }
                        }
                    }

                    // Binary GGUF Metadata Badges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StudioObsidian, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "GGUF HEADER",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (model.ggufMagicVerified) "GGUF v${model.ggufVersion} OK" else "RAW BIN",
                                fontFamily = JetBrainsMonoFamily,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (model.ggufMagicVerified) ApkEmerald else CompilerAmber
                            )
                        }
                        Column {
                            Text(
                                text = "TENSORS / KV",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${model.tensorCount} / ${model.kvMetadataCount}",
                                fontFamily = JetBrainsMonoFamily,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = "SPEED",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${"%.1f".format(model.tokensPerSecBenchmark)} tok/s",
                                fontFamily = JetBrainsMonoFamily,
                                style = MaterialTheme.typography.labelMedium,
                                color = ElectricCyan
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onRunBenchmark(model) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Benchmark CPU")
                        }

                        Button(
                            onClick = { onSelectActiveModel(model) },
                            enabled = !model.isActive,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = StudioObsidian
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (model.isActive) "Active Engine" else "Use Model")
                        }
                    }
                }
            }
        }

        // 3. Sampling Hyperparameters & Localhost Loopback Bridge
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
                    Text(
                        text = "Llama Inference Hyperparameters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Sampling Temperature",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "%.2f".format(temperature),
                            fontFamily = JetBrainsMonoFamily,
                            color = ElectricCyan
                        )
                    }
                    Slider(
                        value = temperature,
                        onValueChange = onTemperatureChange,
                        valueRange = 0.05f..1.0f
                    )

                    Text(
                        text = "Context Window Size (Tokens)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CONTEXT_WINDOW_OPTIONS.forEach { ctxSize ->
                            FilterChip(
                                selected = contextWindowSize == ctxSize,
                                onClick = { onContextWindowChange(ctxSize) },
                                label = { Text("$ctxSize") }
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Localhost llama.cpp / Termux Bridge",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Optional loopback bridge to 127.0.0.1 llama-server on device",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = useLocalhostDaemon,
                            onCheckedChange = onToggleLocalhostDaemon
                        )
                    }

                    OutlinedTextField(
                        value = localhostEndpoint,
                        onValueChange = onLocalhostEndpointChange,
                        label = { Text("Loopback OpenAI-Compatible Endpoint") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = localhostPingStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = CompilerAmber,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalButton(onClick = onPingLocalhost) {
                            Icon(
                                Icons.Default.NetworkPing,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ping 127.0.0.1")
                        }
                    }
                }
            }
        }
    }
}
