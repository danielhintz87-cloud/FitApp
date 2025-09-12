package com.example.fitapp.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.fitapp.data.db.AppDatabase

@Composable
fun AiLogsScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    val dao = remember { AppDatabase.get(ctx).aiLogDao() }
    val logs = dao.latest().collectAsState(initial = emptyList())

    LazyColumn(modifier = modifier.padding(contentPadding)) {
        items(logs.value) { log ->
            Text(
                "${log.provider} • ${log.type} • ${if (log.success) "OK" else "ERR"} • ${log.tookMs}ms",
                style = MaterialTheme.typography.labelMedium,
            )
            Text(log.prompt, style = MaterialTheme.typography.bodySmall)
            Text(log.result.take(600), style = MaterialTheme.typography.bodySmall)
            Text("—")
        }
    }
}
