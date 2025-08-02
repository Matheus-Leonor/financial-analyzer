package com.financialanalyzer.app.shared.python

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Bridge para comunicação entre Kotlin e Python via ProcessBuilder
 * Gerencia execução de scripts Python e troca de dados via JSON
 */
class PythonBridge {
    
    private val pythonExecutable = "python" // ou caminho específico se necessário
    private val baseDir = File(System.getProperty("user.dir"))
    private val pythonEngineDir = File(baseDir, "python-engine")
    private val sharedDataDir = File(baseDir, "shared-data")
    private val inputDir = File(sharedDataDir, "input")
    private val outputDir = File(sharedDataDir, "output")
    private val tempDir = File(sharedDataDir, "temp")
    
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    
    init {
        // Criar diretórios se não existirem
        listOf(inputDir, outputDir, tempDir).forEach { dir ->
            if (!dir.exists()) {
                dir.mkdirs()
            }
        }
    }
    
    /**
     * Executa o agente Python e processa mensagem do chat
     */
    suspend fun processChatMessage(
        message: String,
        fileData: FileData? = null
    ): PythonResponse = withContext(Dispatchers.IO) {
        
        val requestId = UUID.randomUUID().toString()
        val request = PythonRequest(
            id = requestId,
            type = "chat",
            message = message,
            fileData = fileData
        )
        
        try {
            // 1. Salvar request como JSON
            val requestFile = File(inputDir, "request_$requestId.json")
            requestFile.writeText(json.encodeToString(request))
            
            // 2. Executar script Python
            val responseFile = File(outputDir, "response_$requestId.json")
            val result = executePythonScript(
                script = "api_bridge.py",
                args = listOf(requestFile.absolutePath, responseFile.absolutePath)
            )
            
            // 3. Ler resposta
            if (responseFile.exists()) {
                val responseJson = responseFile.readText()
                val response = json.decodeFromString<PythonResponse>(responseJson)
                
                // Limpar arquivos temporários
                requestFile.delete()
                responseFile.delete()
                
                return@withContext response
            } else {
                return@withContext PythonResponse(
                    id = requestId,
                    status = "error",
                    message = "Python script execution failed",
                    error = result.error
                )
            }
            
        } catch (e: Exception) {
            return@withContext PythonResponse(
                id = requestId,
                status = "error", 
                message = "Bridge communication error: ${e.message}",
                error = e.stackTraceToString()
            )
        }
    }
    
    /**
     * Carrega dados de arquivo (CSV/PDF) via Python
     */
    suspend fun loadDataFile(filePath: String): PythonResponse = withContext(Dispatchers.IO) {
        val requestId = UUID.randomUUID().toString()
        val request = PythonRequest(
            id = requestId,
            type = "load_data",
            message = "Load file: $filePath",
            fileData = FileData(
                name = File(filePath).name,
                path = filePath,
                type = when (File(filePath).extension.lowercase()) {
                    "csv" -> "csv"
                    "pdf" -> "pdf" 
                    "xlsx", "xls" -> "excel"
                    else -> "unknown"
                }
            )
        )
        
        return@withContext processChatMessage("Load data from file", request.fileData)
    }
    
    /**
     * Executa script Python usando ProcessBuilder
     */
    private suspend fun executePythonScript(
        script: String,
        args: List<String> = emptyList()
    ): ProcessResult = withContext(Dispatchers.IO) {
        
        val scriptFile = File(pythonEngineDir, script)
        if (!scriptFile.exists()) {
            return@withContext ProcessResult(
                exitCode = -1,
                output = "",
                error = "Python script not found: ${scriptFile.absolutePath}"
            )
        }
        
        val command = mutableListOf<String>().apply {
            add(pythonExecutable)
            add(scriptFile.absolutePath)
            addAll(args)
        }
        
        try {
            val processBuilder = ProcessBuilder(command)
                .directory(pythonEngineDir)
                .redirectErrorStream(false)
            
            // Configurar ambiente (PATH pode ser necessário)
            val env = processBuilder.environment()
            
            val process = processBuilder.start()
            
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            
            return@withContext ProcessResult(
                exitCode = exitCode,
                output = output,
                error = error
            )
            
        } catch (e: IOException) {
            return@withContext ProcessResult(
                exitCode = -1,
                output = "",
                error = "Failed to execute Python: ${e.message}"
            )
        }
    }
    
    /**
     * Verifica se Python e dependências estão disponíveis
     */
    suspend fun checkPythonSetup(): SetupStatus = withContext(Dispatchers.IO) {
        try {
            // Testar execução básica do Python
            val result = executePythonScript("test_install.py")
            
            return@withContext if (result.exitCode == 0) {
                SetupStatus(
                    available = true,
                    pythonVersion = result.output.trim(),
                    message = "Python setup OK"
                )
            } else {
                SetupStatus(
                    available = false,
                    pythonVersion = null,
                    message = "Python setup failed: ${result.error}"
                )
            }
        } catch (e: Exception) {
            return@withContext SetupStatus(
                available = false,
                pythonVersion = null,
                message = "Python not found: ${e.message}"
            )
        }
    }
    
    /**
     * Lista arquivos de gráficos gerados
     */
    fun getGeneratedCharts(): List<File> {
        return outputDir.listFiles { file -> 
            file.extension.lowercase() in listOf("png", "jpg", "jpeg", "svg")
        }?.toList() ?: emptyList()
    }
}

// Data classes para comunicação JSON
@Serializable
data class PythonRequest(
    val id: String,
    val type: String, // "chat", "load_data", "generate_chart"
    val message: String,
    val fileData: FileData? = null,
    val parameters: Map<String, String> = emptyMap()
)

@Serializable
data class PythonResponse(
    val id: String,
    val status: String, // "success", "error"
    val message: String,
    val data: String? = null, // JSON data ou markdown table
    val tableData: String? = null, // Formatted table for display
    val charts: List<String> = emptyList(), // Lista de arquivos de gráficos
    val error: String? = null
)

@Serializable
data class FileData(
    val name: String,
    val path: String,
    val type: String, // "csv", "pdf", "excel"
    val size: Long? = null
)

data class ProcessResult(
    val exitCode: Int,
    val output: String,
    val error: String
)

data class SetupStatus(
    val available: Boolean,
    val pythonVersion: String?,
    val message: String
)