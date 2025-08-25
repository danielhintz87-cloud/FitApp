package com.example.fitapp.ui.food

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.fitapp.ai.AiProvider
import com.example.fitapp.ai.AppAi
import kotlinx.coroutines.launch

@Composable
fun FoodScanScreen(contentPadding: PaddingValues) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var imageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var result by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var provider by remember { mutableStateOf(AiProvider.OPENAI) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Food Scan", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = provider == AiProvider.OPENAI, onClick = { provider = AiProvider.OPENAI }, label = { Text("OpenAI") })
            FilterChip(selected = provider == AiProvider.GEMINI, onClick = { provider = AiProvider.GEMINI }, label = { Text("Gemini") })
            FilterChip(selected = provider == AiProvider.DEEPSEEK, onClick = { provider = AiProvider.DEEPSEEK }, label = { Text("DeepSeek") })
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) { Text("Foto auswählen") }

        imageUri?.let { uri ->
            Spacer(Modifier.height(12.dp))
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(240.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(12.dp))
            Button(enabled = !busy, onClick = {
                scope.launch {
                    busy = true
                    result = try {
                        val input = ctx.contentResolver.openInputStream(uri)!!
                        val bytes = input.readBytes()
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        val r = AppAi.calories(ctx, provider, bmp, "FoodScan").getOrThrow()
                        "≈ ${r.kcal} kcal (≈${r.confidence}% sicher)\n${r.text}"
                    } catch (e: Exception) {
                        "Fehler: ${e.message}"
                    } finally {
                        busy = false
                    }
                }
            }) { Text(if (busy) "Analysiere…" else "Kalorien schätzen") }
        }
        result?.let {
            Spacer(Modifier.height(12.dp))
            Card { Text(it, Modifier.padding(16.dp)) }
        }
    }
}