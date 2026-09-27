package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apk_projects")
data class ApkProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appName: String,
    val packageName: String,
    val versionName: String = "1.0.0",
    val versionCode: Int = 1,
    val minSdk: Int = 24,
    val targetSdk: Int = 35,
    val prompt: String,
    val templateCategory: String,
    val accentHex: String,
    val permissionsCsv: String,
    val manifestXml: String,
    val mainActivityKt: String,
    val buildGradleKts: String,
    val stringsXml: String,
    val themeKt: String,
    val llamaReasoning: String,
    val modelUsed: String,
    val apkFilePath: String = "",
    val apkSizeBytes: Long = 0L,
    val apkSha256: String = "",
    val dexChecksum: String = "",
    val lastBuiltAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "llama_models")
data class LlamaModelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val family: String,
    val quantization: String,
    val parametersBillions: Float,
    val contextLength: Int,
    val filePath: String,
    val fileSizeBytes: Long,
    val ggufMagicVerified: Boolean,
    val ggufVersion: Int,
    val tensorCount: Long,
    val kvMetadataCount: Long,
    val sha256Prefix: String,
    val isEmbeddedEngine: Boolean,
    val isActive: Boolean,
    val tokensPerSecBenchmark: Float,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "build_logs")
data class BuildLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val stage: String,
    val message: String,
    val level: String, // INFO, SUCCESS, WARN, ERROR
    val timestamp: Long = System.currentTimeMillis()
)
