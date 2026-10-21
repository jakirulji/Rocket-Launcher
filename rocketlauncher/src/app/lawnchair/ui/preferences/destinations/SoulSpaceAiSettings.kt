/*
 * Copyright 2026, Rocket Launcher contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package app.lawnchair.ui.preferences.destinations

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.lawnchair.preferences.getAdapter
import app.lawnchair.preferences2.preferenceManager2
import app.lawnchair.ui.preferences.components.controls.ListPreference
import app.lawnchair.ui.preferences.components.controls.ListPreferenceEntry
import app.lawnchair.ui.preferences.components.layout.PreferenceGroup
import app.lawnchair.ui.preferences.components.controls.SwitchPreference
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

private const val MODEL_ID = "qwen3_0_6b_q4_0"
private const val MODEL_FILE = "Qwen3-0.6B-Q4_0.gguf"
private const val MODEL_URL =
    "https://huggingface.co/ggml-org/Qwen3-0.6B-GGUF/resolve/main/Qwen3-0.6B-Q4_0.gguf"
private const val MODEL_SHA256 =
    "da2572f16c06133561ce56accaa822216f2391ef4d37fba427801cd6736417d4"

private fun modelFile(context: Context) =
    File(context.filesDir, "soul-space/models/$MODEL_FILE")

@Composable
fun SoulSpaceAiSettings(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = preferenceManager2()
    val scope = rememberCoroutineScope()
    val target = remember { modelFile(context) }
    var modelReady by remember { mutableStateOf(target.isFile && target.length() > 0L) }
    var downloading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var status by remember {
        mutableStateOf(
            if (modelReady) "Model file is stored on this device."
            else "No AI model downloaded yet. Wisdom cards continue to work without it.",
        )
    }
    var downloadJob by remember { mutableStateOf<Job?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PreferenceGroup(heading = "AI Settings") {
            TextButton(
                onClick = onBack,
                modifier = Modifier.padding(horizontal = 8.dp),
            ) { Text("← Back to Soul Space settings") }

            SwitchPreference(
                adapter = prefs.soulSpaceAiEnabled.getAdapter(),
                label = "Enable on-device AI",
            )

            Text(
                text = "AI is intended to analyse mood/context locally and help select relevant wisdom. Personal text, mood and analysis are never uploaded. The switch defaults ON, but AI inference is not active until a supported local runtime and model are installed.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            val modelEntries = remember {
                listOf(
                    ListPreferenceEntry(MODEL_ID) { "Qwen3 0.6B · Q4_0 · 429 MB (Lite)" },
                    ListPreferenceEntry("qwen3_1_7b_q4", label = { "Qwen3 1.7B · not available yet" }, enabled = false),
                    ListPreferenceEntry("qwen3_4b_q4", label = { "Qwen3 4B · heavy · not available yet" }, enabled = false),
                    ListPreferenceEntry("gemma_mobile", label = { "Mobile Gemma candidate · not available yet" }, enabled = false),
                )
            }
            ListPreference(
                adapter = prefs.soulSpaceAiModel.getAdapter(),
                entries = modelEntries,
                label = "AI model",
            )
        }

        PreferenceGroup(heading = "Model Manager") {
            Text(
                text = "Qwen3 0.6B Q4_0 · approximately 429 MB download. The model file is saved in app-private storage. A checksum is checked after download.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (downloading) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                )
                Text(
                    text = "Downloading ${(progress * 100).toInt()}%… Keep the app open until it finishes.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Button(
                    onClick = { downloadJob?.cancel() },
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) { Text("Cancel download") }
            } else if (!modelReady) {
                Button(
                    onClick = {
                        downloadJob = scope.launch {
                            downloading = true
                            progress = 0f
                            status = "Connecting to the model host…"
                            val partial = File(target.parentFile, target.name + ".part")
                            try {
                                withContext(Dispatchers.IO) {
                                    target.parentFile?.mkdirs()
                                    partial.delete()
                                    val connection = (URL(MODEL_URL).openConnection() as HttpURLConnection).apply {
                                        connectTimeout = 20_000
                                        readTimeout = 30_000
                                        instanceFollowRedirects = true
                                        setRequestProperty("User-Agent", "Rocket-Launcher-Soul-Space")
                                    }
                                    try {
                                        if (connection.responseCode !in 200..299) {
                                            throw IOException("Model host returned HTTP ${connection.responseCode}")
                                        }
                                        val total = connection.contentLengthLong
                                        val digest = MessageDigest.getInstance("SHA-256")
                                        connection.inputStream.use { input ->
                                            FileOutputStream(partial).use { output ->
                                                val buffer = ByteArray(64 * 1024)
                                                var downloaded = 0L
                                                while (true) {
                                                    currentCoroutineContext().ensureActive()
                                                    val count = input.read(buffer)
                                                    if (count < 0) break
                                                    output.write(buffer, 0, count)
                                                    digest.update(buffer, 0, count)
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
                                        val actualHash = digest.digest().joinToString("") {
                                            "%02x".format(it.toInt() and 0xff)
                                        }
                                        if (!actualHash.equals(MODEL_SHA256, ignoreCase = true)) {
                                            throw IOException("Checksum mismatch. The incomplete model was rejected.")
                                        }
                                        if (target.exists() && !target.delete()) {
                                            throw IOException("Could not replace the existing model file.")
                                        }
                                        if (!partial.renameTo(target)) {
                                            throw IOException("Could not save the downloaded model.")
                                        }
                                    } finally {
                                        connection.disconnect()
                                    }
                                }
                                modelReady = true
                                progress = 1f
                                status = "Model downloaded and checksum verified. Local inference is not enabled yet; the on-device runtime integration is the next step."
                            } catch (cancelled: CancellationException) {
                                status = "Download cancelled. Partial data will be removed."
                                throw cancelled
                            } catch (error: Exception) {
                                status = "Download failed: ${error.message ?: "unknown error"}. You can try again."
                            } finally {
                                withContext(NonCancellable + Dispatchers.IO) { partial.delete() }
                                downloading = false
                                downloadJob = null
                            }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) { Text("Download model · 429 MB") }
            } else {
                Text(
                    text = "✓ Model downloaded and verified",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                TextButton(
                    onClick = {
                        if (target.delete()) {
                            modelReady = false
                            progress = 0f
                            status = "Model deleted. Wisdom cards still work without AI."
                        } else {
                            status = "Could not delete the model file."
                        }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp),
                ) { Text("Delete downloaded model") }
            }

            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Text(
                text = "Privacy: downloads fetch only public model weights. No mood, quote history, or personal text is sent to the model host.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}
