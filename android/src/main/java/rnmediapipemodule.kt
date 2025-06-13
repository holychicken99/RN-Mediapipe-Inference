package com.rnmediapipe

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.Backend
import expo.modules.kotlin.Promise
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import java.io.File
import java.io.IOException
import kotlinx.coroutines.*

class ReactNativeMediapipe : Module() {
    private var llmInference: LlmInference? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    @Volatile // Ensure visibility across threads
    private var isInitialized = false

    private val modelName = "Gemma3-1B-IT_multi-prefill-seq_q8_ekv2048.task"

    override fun definition() = ModuleDefinition {
        Name("ReactNativeMediapipe")

        // Define events for status updates and responses
        Events("onLLMStatus", "onLLMResponse", "onLLMError", "onLLMReady")

        // This function will kick off the initialization and report progress via events.
        // It resolves its promise immediately so the JS thread isn't blocked.
        // AsyncFunction("initialize") { promise: Promise ->
        //     if (isInitialized) {
        //         sendEvent("onLLMStatus", mapOf("status" to "ready", "message" to "Already initialized"))
        //         promise.resolve(true)
        //         return@AsyncFunction
        //     }

        //     scope.launch {
        //         try {
        //             val context = appContext.reactContext ?: throw Exception("React context is null")

        //             // 1. Get model file from assets (production-ready approach)
        //             sendEvent("onLLMStatus", mapOf("status" to "loading_model", "message" to "Copying model from assets..."))
        //             val modelPath = "/data/local/tmp/llm/Gemma3-1B-IT_multi-prefill-seq_q8_ekv2048.task"
        //             // Log.d("LLMModule", "Model file ready at: ${modelFile.absolutePath}")

        //             // 2. Release any old instance
        //             llmInference?.close()
        //             llmInference = null

        //             // 3. Build options
        //             val taskOptions = LlmInference.LlmInferenceOptions.builder()
        //                 .setModelPath(modelPath)
        //                 .setPreferredBackend(Backend.GPU)
        //                 .setMaxTokens(512)
        //                 .build()

        //             // 4. Create the instance (the heavy part)
        //             sendEvent("onLLMStatus", mapOf("status" to "initializing_engine", "message" to "This may take a while..."))
        //             Log.d("LLMModule", "Creating LlmInference instance...")
        //             llmInference = LlmInference.createFromOptions(context, taskOptions)

        //             val result = llmInference!!.generateResponse("hey man hows it going ")
        //             Log.d("LLMModule", "$result")


        //             isInitialized = true
        //             Log.d("LLMModule", "Initialization successful!")
        //             sendEvent("onLLMReady", null)

        //         } catch (e: Exception) {
        //             isInitialized = false
        //             val errorMessage = "Initialization failed: ${e.message}"
        //             Log.e("LLMModule", errorMessage, e)
        //             sendEvent("onLLMStatus", mapOf("status" to "error", "message" to errorMessage))
        //         }
        //     }

        //     // Resolve the promise immediately to unblock the JS thread.
        //     // The actual result will be communicated via events.
        //     promise.resolve(true)

        // }
        AsyncFunction("initialize") { promise: Promise ->
            scope.launch {
                try {
                    Log.d("LLMModule", "Starting initialization...")

                    val context = appContext.reactContext ?: throw Exception("Context not available")

                    llmInference?.close()
                    llmInference = null

                    val modelPath = "/data/local/tmp/llm/Gemma3-1B-IT_multi-prefill-seq_q8_ekv2048.task"

                    val taskOptions = LlmInference.LlmInferenceOptions.builder()
                        .setModelPath(modelPath)
                        .setPreferredBackend(Backend.GPU)
                        .setMaxTokens(512)
                        .build()

                    llmInference = LlmInference.createFromOptions(context, taskOptions)

                    Log.d("LLMModule", "LlmInference created successfully")

                    isInitialized = true
                    sendEvent("onLLMReady", mapOf("status" to "ready"))

        //             sendEvent("onLLMStatus", mapOf("status" to "error", "message" to errorMessage))
                    // CRITICAL: Use simple data types for promise resolution
                    promise.resolve(true)

                } catch (e: Exception) {
                    Log.e("LLMModule", "Initialization failed: ${e.message}")
                    e.printStackTrace()

                    // CRITICAL: Don't pass the exception object directly to reject
                    // This causes the MapIteratorHelper serialization error

                    // BAD - causes serialization error:
                    // promise.reject("INIT_ERROR", "Failed to initialize: ${e.message}", e)

                    // GOOD - pass null instead of exception object:
                    promise.reject("INIT_ERROR", "Failed to initialize: ${e.message}", null)

                    // OR even better - create a simple error message:
                    // promise.reject("INIT_ERROR", e.message ?: "Unknown initialization error", null)
                }
            }
        }


        AsyncFunction("generate") { prompt: String, promise: Promise ->
            if (!isInitialized || llmInference == null) {
                promise.reject("NOT_INITIALIZED", "LLM is not initialized. Call initialize() first.", null)
                return@AsyncFunction
            }

            scope.launch {
                try {
                    val response = llmInference!!.generateResponse(prompt)
                    promise.resolve(response)
                } catch (e: Exception) {
                    val errorMessage = "Failed to generate text: ${e.message}"
                    Log.e("LLMModule", errorMessage, e)
                    promise.reject("GENERATION_ERROR", errorMessage, e)
                }
            }
        }

        Function("isInitialized") {
            return@Function isInitialized
        }

        // Clean up resources when the module is destroyed (e.g., app closes)
        OnDestroy {
            cleanup()
        }
    }

    private fun cleanup() {
        try {
            llmInference?.close()
            llmInference = null
            isInitialized = false
            scope.cancel() // Cancel all coroutines
            Log.d("LLMModule", "Cleaned up LLM resources.")
        } catch (e: Exception) {
            Log.e("LLMModule", "Error during cleanup: ${e.message}", e)
        }
    }

    /**
     * Copies a model file from the app's assets folder to its private cache directory.
     * Returns the File object of the cached model.
     */
    @Throws(IOException::class)
    private fun getModelFileFromAssets(context: Context, modelName: String): File {
        val cacheFile = File(context.cacheDir, modelName)

        // If the file already exists in cache, no need to copy again.
        if (cacheFile.exists()) {
            Log.d("LLMModule", "Model already in cache.")
            return cacheFile
        }

        Log.d("LLMModule", "Model not in cache, copying from assets...")
        context.assets.open(modelName).use { inputStream ->
            cacheFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return cacheFile
    }
}
