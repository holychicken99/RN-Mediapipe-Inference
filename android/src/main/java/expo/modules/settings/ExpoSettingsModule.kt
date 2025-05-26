package expo.modules.settings

// Standard Java/Kotlin imports
// MediaPipe imports

import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.Backend
import expo.modules.kotlin.Promise
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import java.io.File
import kotlinx.coroutines.*

class ExpoSettingsModule : Module() {
    // instance of llm inference
    private var llmInference: LlmInference? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun definition() = ModuleDefinition {
        Name("ExpoSettings")

        Function("hello") { "Hello world! 👋" }

        Events("onLLMResponse", "onLLMError")

        AsyncFunction("initialize") { promise: Promise ->
            scope.launch {
                try {
                    // Add detailed logging for debugging
                    Log.d("LLMModule", "Starting initialization...")

                    // Check context availability
                    val context = appContext.reactContext
                    if (context == null) {
                        Log.e("LLMModule", "React context is null")
                        promise.reject("CONTEXT_ERROR", "React context not available", null)
                        return@launch
                    }
                    Log.d("LLMModule", "Context obtained successfully")

                    // Check if model file exists and is readable
                    val modelPath =
                            "/data/local/tmp/llm/Gemma3-1B-IT_multi-prefill-seq_q8_ekv2048.task"
                    val modelFile = File(modelPath)

                    Log.d("LLMModule", "Checking model file at: $modelPath")
                    Log.d("LLMModule", "File exists: ${modelFile.exists()}")
                    Log.d("LLMModule", "File readable: ${modelFile.canRead()}")
                    Log.d(
                            "LLMModule",
                            "File size: ${if (modelFile.exists()) modelFile.length() else "N/A"} bytes"
                    )

                    if (!modelFile.exists()) {
                        promise.reject(
                                "MODEL_NOT_FOUND",
                                "Model file not found. Did you run: adb push your_model.task $modelPath",
                                null
                        )
                        return@launch
                    }

                    if (!modelFile.canRead()) {
                        promise.reject(
                                "MODEL_NOT_READABLE",
                                "Model file exists but cannot be read. Check permissions.",
                                null
                        )
                        return@launch
                    }

                    // Release existing instance safely
                    try {
                        llmInference?.close()
                        llmInference = null
                        Log.d("LLMModule", "Previous instance released")
                    } catch (e: Exception) {
                        Log.w("LLMModule", "Warning releasing previous instance: ${e.message}")
                    }

                    // Build options step by step with proper MediaPipe format
                    Log.d("LLMModule", "Building LLM options...")

                    val taskOptions =
                            LlmInference.LlmInferenceOptions.builder()
                                    .setModelPath(modelPath)
                                    .setPreferredBackend(Backend.GPU)
                                    .setMaxTokens(512)
                                    .setMaxTopK(64)
                                    .build()

                    Log.d("LLMModule", "Options built successfully")

                    // Create inference instance
                    Log.d("LLMModule", "Creating LlmInference instance...")
                    llmInference = LlmInference.createFromOptions(context, taskOptions)
                    Log.d("LLMModule", "LlmInference created successfully")

                    promise.resolve(true)
                    Log.d("LLMModule", "Initialization completed successfully")
                } catch (e: SecurityException) {
                    Log.e("LLMModule", "Security/Permission error: ${e.message}", e)
                    promise.reject(
                            "SECURITY_ERROR",
                            "Permission denied accessing model file: ${e.message}",
                            e
                    )
                } catch (e: IllegalArgumentException) {
                    Log.e("LLMModule", "Invalid argument error: ${e.message}", e)
                    promise.reject(
                            "INVALID_ARGUMENT",
                            "Invalid model or configuration: ${e.message}",
                            e
                    )
                } catch (e: UnsatisfiedLinkError) {
                    Log.e("LLMModule", "Native library error: ${e.message}", e)
                    promise.reject(
                            "NATIVE_ERROR",
                            "MediaPipe native library issue: ${e.message}",
                            e
                    )
                } catch (e: OutOfMemoryError) {
                    Log.e("LLMModule", "Out of memory: ${e.message}", e)
                    promise.reject("MEMORY_ERROR", "Insufficient memory for model: ${e.message}", e)
                } catch (e: RuntimeException) {
                    Log.e("LLMModule", "Runtime error: ${e.message}", e)
                    promise.reject("RUNTIME_ERROR", "MediaPipe runtime error: ${e.message}", e)
                } catch (e: Exception) {
                    Log.e("LLMModule", "Initialization failed: ${e.message}", e)
                    e.printStackTrace()
                    promise.reject("INIT_ERROR", "Failed to initialize: ${e.message}", e)
                }
            }
        }

        AsyncFunction("generate") { prompt: String, promise: Promise ->
            scope.launch {
                try {
                    val inference = llmInference ?: throw Exception("LLM not initialized")

                    val response = inference.generateResponse(prompt)
                    promise.resolve(response)
                } catch (e: Exception) {
                    promise.reject("GENERATION_ERROR", "Failed to generate text: ${e.message}", e)
                }
            }
        }
    }
}

// private fun getModelFile(context: Context, modelPath: String): File {
//     // Check if it's already a file path
//     val file = File(modelPath)
//     if (file.exists()) {
//         return file
//     }

//     // Try to extract from assets
//     val assetManager = context.assets
//     val tempFile = File(context.cacheDir, "temp_model_${System.currentTimeMillis()}.bin")

//     try {
//         assetManager.open(modelPath).use { inputStream ->
//             tempFile.outputStream().use { outputStream -> inputStream.copyTo(outputStream) }
//         }
//         return tempFile
//     } catch (e: IOException) {
//         throw Exception("Model file not found: $modelPath")
//     }
// }
