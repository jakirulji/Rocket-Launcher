/*
 * Copyright 2026, Rocket Launcher contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package app.lawnchair.ui.preferences.destinations

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.StatFs
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.lawnchair.preferences.getAdapter
import app.lawnchair.preferences2.preferenceManager2
import app.lawnchair.ui.preferences.components.controls.SwitchPreference
import app.lawnchair.ui.preferences.components.layout.PreferenceGroup
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.LogSeverity
import com.patrykmichalik.opto.core.setBlocking
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private data class SoulSpaceModel(
    val id: String,
    val name: String,
    val description: String,
    val fileName: String,
    val url: String,
    val expectedBytes: Long,
    val minRamMb: Long,
    val minAvailableMb: Long,
    val minStorageMb: Long,
)

private val soulSpaceModels = listOf(
    SoulSpaceModel(
        "qwen3_0_6b_q4_0", "Qwen3 0.6B · Lite",
        "Smallest download; best starting point for modest phones.",
        "qwen3_0_6b_mixed_int4.litertlm",
        "https://huggingface.co/litert-community/Qwen3-0.6B/resolve/main/qwen3_0_6b_mixed_int4.litertlm",
        497_700_000L, 3_000L, 1_000L, 900L,
    ),
    SoulSpaceModel(
        "qwen3_1_7b_q4", "Qwen3 1.7B · Balanced",
        "Stronger language ability; needs more available memory.",
        "Qwen3-1.7B_dynamic_wi4b32_afp32.litertlm",
        "https://huggingface.co/litert-community/Qwen3-1.7B/resolve/main/Qwen3-1.7B_dynamic_wi4b32_afp32.litertlm",
        1_024_000_000L, 6_000L, 2_000L, 1_500L,
    ),
    SoulSpaceModel(
        "qwen3_4b_q4", "Qwen3 4B · Advanced",
        "Large model; can cause heavy memory use, heat and battery drain.",
        "qwen3_4b_mixed_int4.litertlm",
        "https://huggingface.co/litert-community/Qwen3-4B/resolve/main/qwen3_4b_mixed_int4.litertlm",
        2_700_000_000L, 12_000L, 5_000L, 3_200L,
    ),
    SoulSpaceModel(
        "gemma_mobile", "Gemma 4 E2B · Mobile candidate",
        "Google Gemma model; large download and memory demand.",
        "gemma-4-E2B-it.litertlm",
        "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm",
        2_780_000_000L, 8_000L, 3_000L, 3_000L,
    ),
)

private data class AiDeviceReport(
    val totalRamMb: Long,
    val availableRamMb: Long,
    val freeStorageMb: Long,
    val cpuCores: Int,
    val is64Bit: Boolean,
    val androidVersion: Int,
    val recommendedModelId: String,
    val reason: String,
)

private fun inspectAiDevice(context: Context): AiDeviceReport {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memory = ActivityManager.MemoryInfo().also(activityManager::getMemoryInfo)
    val storage = StatFs(context.filesDir.absolutePath)
    val total = memory.totalMem / (1024L * 1024L)
    val available = memory.availMem / (1024L * 1024L)
    val free = storage.availableBytes / (1024L * 1024L)
    val is64Bit = Build.SUPPORTED_64_BIT_ABIS.isNotEmpty()
    val recommended = when {
        is64Bit && total >= 12_000L && available >= 5_000L && free >= 3_200L -> "qwen3_4b_q4"
        is64Bit && total >= 8_000L && available >= 3_000L && free >= 3_000L -> "gemma_mobile"
        is64Bit && total >= 6_000L && available >= 2_000L && free >= 1_500L -> "qwen3_1_7b_q4"
        else -> "qwen3_0_6b_q4_0"
    }
    val chosen = soulSpaceModels.first { it.id == recommended }
    val enough = total >= chosen.minRamMb && available >= chosen.minAvailableMb &&
        free >= chosen.minStorageMb && is64Bit
    return AiDeviceReport(
        total, available, free, Runtime.getRuntime().availableProcessors(),
        is64Bit, Build.VERSION.SDK_INT,
        if (enough) recommended else "qwen3_0_6b_q4_0",
        if (enough) {
            "This recommendation matches the RAM, currently available memory, free storage and 64-bit checks on this device. Actual speed still depends on chipset and thermal conditions."
        } else {
            "The device did not meet every minimum check for the larger candidate, so the Lite model is the safer starting point. Actual performance must be tested on-device."
        },
    )
}

private fun modelFile(context: Context, model: SoulSpaceModel) =
    File(context.filesDir, "soul-space/models/${model.fileName}")

@Composable
fun SoulSpaceAiSettings(onBack: () -> Unit, autoStartDownload: Boolean = false) {
    val context = LocalContext.current
    val prefs = preferenceManager2()
    val scope = rememberCoroutineScope()
    val report = remember(context) { inspectAiDevice(context) }
    val modelKey = stringPreferencesKey("soul_space_ai_model")
    var selectedModelId by remember {
        mutableStateOf(prefs.getCachedPreferences()[modelKey] ?: "auto")
    }
    val recommended = soulSpaceModels.first { it.id == report.recommendedModelId }
    val selected = if (selectedModelId == "auto") recommended
        else soulSpaceModels.firstOrNull { it.id == selectedModelId } ?: recommended
    val target = modelFile(context, selected)
    var modelReady by remember(selected.id) { mutableStateOf(target.isFile && target.length() > 0L) }
    var downloading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var status by remember {
        mutableStateOf(
            if (modelReady) "Selected model file is stored on this device."
            else "No selected model downloaded yet. Regular wisdom cards continue to work.",
        )
    }
    var downloadJob by remember { mutableStateOf<Job?>(null) }
    var testing by remember { mutableStateOf(false) }
    var testResponse by remember { mutableStateOf("") }

    val startDownload: () -> Unit = {
                                downloadJob = scope.launch {
                                    downloading = true
                                    progress = 0f
                                    status = "Connecting to the model host…"
                                    val partial = File(target.parentFile, target.name + ".part")
                                    try {
                                        withContext(Dispatchers.IO) {
                                            target.parentFile?.mkdirs()
                                            partial.delete()
                                            val connection = (URL(selected.url).openConnection() as HttpURLConnection).apply {
                                                connectTimeout = 20_000
                                                readTimeout = 45_000
                                                instanceFollowRedirects = true
                                                setRequestProperty("User-Agent", "Rocket-Launcher-Soul-Space")
                                            }
                                            try {
                                                if (connection.responseCode !in 200..299) {
                                                    throw IOException("Model host returned HTTP ${connection.responseCode}. Some model repositories may require license acceptance.")
                                                }
                                                val total = connection.contentLengthLong
                                                if (total > 0L && total < selected.expectedBytes * 0.75) {
                                                    throw IOException("The model host returned a file smaller than expected.")
                                                }
                                                var downloaded = 0L
                                                connection.inputStream.use { input ->
                                                    FileOutputStream(partial).use { output ->
                                                        val buffer = ByteArray(128 * 1024)
                                                        while (true) {
                                                            currentCoroutineContext().ensureActive()
                                                            val count = input.read(buffer)
                                                            if (count < 0) break
                                                            output.write(buffer, 0, count)
                                                            downloaded += count
                                                            if (total > 0L) {
                                                                withContext(Dispatchers.Main) {
                                                                    progress = downloaded.toFloat() / total.toFloat()
                                                                }
                                                            }
                                                        }
                                                        output.fd.sync()
                                                    }
                                                }
                                                if (downloaded < selected.expectedBytes * 0.75) {
                                                    throw IOException("Download was incomplete; the partial file was rejected.")
                                                }
                                                if (target.exists() && !target.delete()) throw IOException("Could not replace existing model.")
                                                if (!partial.renameTo(target)) throw IOException("Could not save downloaded model.")
                                            } finally {
                                                connection.disconnect()
                                            }
                                        }
                                        modelReady = true
                                        progress = 1f
                                        status = "Model download completed and size checked. The runtime will now be tested locally."
                                    } catch (cancelled: CancellationException) {
                                        status = "Download cancelled."
                                        throw cancelled
                                    } catch (error: Exception) {
                                        status = "Download failed: ${error.message ?: "unknown error"}"
                                    } finally {
                                        withContext(NonCancellable + Dispatchers.IO) { partial.delete() }
                                        downloading = false
                                        downloadJob = null
                                    }
                                }
    }
    LaunchedEffect(autoStartDownload) {
        if (autoStartDownload && !modelReady) startDownload()
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PreferenceGroup(heading = "AI Settings") {
            TextButton(onClick = onBack, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text("← Back to Soul Space settings")
            }
            SwitchPreference(
                adapter = prefs.soulSpaceAiEnabled.getAdapter(),
                label = "Enable on-device AI",
            )
            Text(
                "Your phone is checked locally. Android does not require a dangerous runtime permission to read RAM, CPU ABI, Android version or available storage, so Soul Space will not ask for unrelated personal permissions. Internet access is used only when you choose to download a model.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Text(
                "Device check: RAM ${report.totalRamMb / 1024} GB total · ${report.availableRamMb} MB currently available\n" +
                    "Free storage: ${report.freeStorageMb} MB · CPU cores: ${report.cpuCores}\n" +
                    "64-bit ABI: ${if (report.is64Bit) "supported" else "not detected"} · Android API ${report.androidVersion}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            Text(
                "Best match for this device: ${recommended.name}\n${report.reason}",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Text(
                "Choose an AI model",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            soulSpaceModels.forEach { model ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = selected.id == model.id,
                        onClick = {
                            selectedModelId = model.id
                            prefs.soulSpaceAiModel.setBlocking(model.id)
                            testResponse = ""
                            status = "Selected ${model.name}. Download it below to use local inference."
                        },
                    )
                    Column(modifier = Modifier.weight(1f).padding(vertical = 6.dp)) {
                        Text(model.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            model.description + if (model.id == report.recommendedModelId) " · Recommended for this device" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "Download: ${if (model.expectedBytes >= 1_000_000_000L) "${model.expectedBytes / 1_000_000_000L} GB+" else "${model.expectedBytes / 1_000_000L} MB+"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        PreferenceGroup(heading = "Download and run AI") {
            Text(
                "The model is the AI's local knowledge and language engine. Downloading it enables offline inference after the file is stored; the model is not bundled in the APK. Downloads come from public LiteRT Community model repositories. Model files remain in private app storage.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            if (downloading) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                )
                Text(
                    "Downloading ${(progress * 100).toInt()}%…",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Button(onClick = { downloadJob?.cancel() }, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Cancel download")
                }
            } else if (!modelReady) {
                Button(
                    onClick = startDownload,
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) { Text("Download ${selected.name}") }
            } else {
                Text(
                    "✓ ${selected.name} is downloaded",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                Button(
                    enabled = !testing,
                    onClick = {
                        scope.launch {
                            testing = true
                            testResponse = ""
                            status = "Loading model and running a real local inference test…"
                            try {
                                testResponse = withContext(Dispatchers.IO) {
                                    Engine.setNativeMinLogSeverity(LogSeverity.ERROR)
                                    Engine(EngineConfig(
                                        modelPath = target.absolutePath,
                                        backend = Backend.CPU(),
                                        cacheDir = context.cacheDir.absolutePath,
                                    )).use { engine ->
                                        engine.initialize()
                                        engine.createConversation().use { conversation ->
                                            var answer = ""
                                            conversation.sendMessageAsync(
                                                "Reply in Bengali with one short sentence: say that Soul Space local AI is working."
                                            ).collect { message ->
                                                answer = message.toString()
                                            }
                                            answer
                                        }
                                    }
                                }
                                status = "Real on-device inference completed. This test ran locally."
                            } catch (error: Exception) {
                                status = "Runtime test failed: ${error.message ?: error.javaClass.simpleName}"
                            } finally {
                                testing = false
                            }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) { Text(if (testing) "Testing local AI…" else "Test real on-device AI") }
                TextButton(
                    onClick = {
                        if (target.delete()) {
                            modelReady = false
                            testResponse = ""
                            status = "Model deleted. Wisdom cards still work without AI."
                        } else status = "Could not delete model file."
                    },
                    modifier = Modifier.padding(horizontal = 8.dp),
                ) { Text("Delete downloaded model") }
            }
            if (testing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            }
            if (testResponse.isNotBlank()) {
                Text(
                    testResponse,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            Text(
                status,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Text(
                "Privacy: device capability checks and inference run locally. No mood, quote history or personal text is uploaded. Downloading sends a request to the selected public model host. Larger models can use substantial RAM, heat the phone and drain battery; use the recommendation as a guide, not a guarantee.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}
