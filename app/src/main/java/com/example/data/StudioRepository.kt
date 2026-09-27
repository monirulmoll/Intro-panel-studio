package com.example.data

import kotlinx.coroutines.flow.Flow

class StudioRepository(private val dao: StudioDao) {
    val allProjects: Flow<List<ApkProjectEntity>> = dao.getAllProjects()
    val allModels: Flow<List<LlamaModelEntity>> = dao.getAllModels()

    fun getLogsForProject(projectId: Long): Flow<List<BuildLogEntity>> =
        dao.getLogsForProject(projectId)

    suspend fun getProjectCount(): Int = dao.getProjectCount()
    suspend fun getModelCount(): Int = dao.getModelCount()

    suspend fun insertProject(project: ApkProjectEntity): Long = dao.insertProject(project)
    suspend fun updateProject(project: ApkProjectEntity) = dao.updateProject(project)
    suspend fun deleteProjectById(id: Long) = dao.deleteProjectById(id)

    suspend fun insertModel(model: LlamaModelEntity): Long = dao.insertModel(model)
    suspend fun setActiveModel(modelId: Long) = dao.setActiveModel(modelId)
    suspend fun updateModelBenchmark(modelId: Long, tps: Float) =
        dao.updateModelBenchmark(modelId, tps)
    suspend fun deleteCustomModel(modelId: Long) = dao.deleteCustomModel(modelId)

    suspend fun addBuildLog(projectId: Long, stage: String, message: String, level: String = "INFO") {
        dao.insertBuildLog(
            BuildLogEntity(
                projectId = projectId,
                stage = stage,
                message = message,
                level = level
            )
        )
    }

    suspend fun clearLogsForProject(projectId: Long) = dao.clearLogsForProject(projectId)
}
