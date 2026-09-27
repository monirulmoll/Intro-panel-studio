package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudioDao {
    // Projects
    @Query("SELECT * FROM apk_projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ApkProjectEntity>>

    @Query("SELECT * FROM apk_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): ApkProjectEntity?

    @Query("SELECT COUNT(*) FROM apk_projects")
    suspend fun getProjectCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ApkProjectEntity): Long

    @Update
    suspend fun updateProject(project: ApkProjectEntity)

    @Query("DELETE FROM apk_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    // Offline Llama Models
    @Query("SELECT * FROM llama_models ORDER BY isActive DESC, addedAt DESC")
    fun getAllModels(): Flow<List<LlamaModelEntity>>

    @Query("SELECT COUNT(*) FROM llama_models")
    suspend fun getModelCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(model: LlamaModelEntity): Long

    @Query("UPDATE llama_models SET isActive = CASE WHEN id = :modelId THEN 1 ELSE 0 END")
    suspend fun setActiveModel(modelId: Long)

    @Query("UPDATE llama_models SET tokensPerSecBenchmark = :tps WHERE id = :modelId")
    suspend fun updateModelBenchmark(modelId: Long, tps: Float)

    @Query("DELETE FROM llama_models WHERE id = :modelId AND isEmbeddedEngine = 0")
    suspend fun deleteCustomModel(modelId: Long)

    // Build Logs
    @Query("SELECT * FROM build_logs WHERE projectId = :projectId ORDER BY timestamp ASC")
    fun getLogsForProject(projectId: Long): Flow<List<BuildLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildLog(log: BuildLogEntity)

    @Query("DELETE FROM build_logs WHERE projectId = :projectId")
    suspend fun clearLogsForProject(projectId: Long)
}
