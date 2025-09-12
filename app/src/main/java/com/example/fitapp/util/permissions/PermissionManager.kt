package com.example.fitapp.util.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/**
 * Comprehensive permission handling for FitApp
 *
 * Manages camera, notification, and storage permissions with
 * proper rationale dialogs and graceful fallback handling.
 */
class PermissionManager(private val context: Context) {
    /**
     * App permissions with descriptions and rationales
     */
    enum class AppPermission(
        val permission: String,
        val title: String,
        val description: String,
        val rationale: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val isRequired: Boolean = true,
    ) {
        CAMERA(
            permission = Manifest.permission.CAMERA,
            title = "Kamera",
            description = "Für Barcode-Scanner und Food-Fotos",
            rationale = "Die Kamera-Berechtigung wird benötigt, um Barcodes zu scannen und Fotos von Lebensmitteln aufzunehmen.",
            icon = Icons.Default.Camera,
            isRequired = true,
        ),

        VIBRATE(
            permission = Manifest.permission.VIBRATE,
            title = "Vibration",
            description = "Für Barcode-Scanner Feedback",
            rationale = "Die Vibrations-Berechtigung ermöglicht haptisches Feedback beim erfolgreichen Scannen von Barcodes.",
            icon = Icons.Default.Vibration,
            isRequired = false,
        ),

        NOTIFICATION(
            permission =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.POST_NOTIFICATIONS
                } else {
                    "" // Not needed for older versions
                },
            title = "Benachrichtigungen",
            description = "Für Fasten-Erinnerungen und Fortschritt",
            rationale = "Benachrichtigungen informieren Sie über Fastenzeiten, Ziele und wichtige Gesundheitsdaten.",
            icon = Icons.Default.Notifications,
            isRequired = false,
        ),
        ;

        fun isGranted(context: Context): Boolean {
            return if (permission.isBlank()) {
                true // Permission not applicable for this Android version
            } else {
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    /**
     * Health Connect permissions (disabled for compatibility)
     */
    object HealthPermissions {
        val REQUIRED_PERMISSIONS = emptySet<String>()

        fun getPermissionLabels(): Map<String, String> {
            return emptyMap()
        }
    }

    /**
     * Check if all required permissions are granted
     */
    fun hasAllRequiredPermissions(): Boolean {
        return AppPermission.values()
            .filter { it.isRequired }
            .all { it.isGranted(context) }
    }

    /**
     * Get list of missing permissions
     */
    fun getMissingPermissions(): List<AppPermission> {
        return AppPermission.values()
            .filter { !it.isGranted(context) && it.permission.isNotBlank() }
    }

    /**
     * Get list of missing required permissions
     */
    fun getMissingRequiredPermissions(): List<AppPermission> {
        return getMissingPermissions().filter { it.isRequired }
    }
}

/**
 * Composable permission request UI with rationale
 */
@Composable
fun PermissionRequestScreen(
    permissions: List<PermissionManager.AppPermission>,
    onPermissionsResult: (Map<String, Boolean>) -> Unit,
    onSkip: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { results ->
            onPermissionsResult(results)
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Berechtigungen erforderlich",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "FitApp benötigt einige Berechtigungen, um alle Funktionen anbieten zu können.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Permission cards
        permissions.forEach { permission ->
            PermissionCard(
                permission = permission,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action buttons
        Button(
            onClick = {
                val permissionsToRequest =
                    permissions
                        .map { it.permission }
                        .filter { it.isNotBlank() }
                        .toTypedArray()

                if (permissionsToRequest.isNotEmpty()) {
                    permissionLauncher.launch(permissionsToRequest)
                } else {
                    onPermissionsResult(emptyMap())
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Berechtigungen erteilen")
        }

        if (onSkip != null && permissions.any { !it.isRequired }) {
            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Überspringen")
            }
        }
    }
}

@Composable
private fun PermissionCard(
    permission: PermissionManager.AppPermission,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = permission.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = permission.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    if (permission.isRequired) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Erforderlich",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                Text(
                    text = permission.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Health Connect permission request composable (disabled for compatibility)
 */
@Composable
fun HealthConnectPermissionScreen(
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Health Connect integration disabled for compatibility
    LaunchedEffect(Unit) {
        onSkip()
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Health Connect Integration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Health Connect integration is currently disabled for compatibility. This feature will be available in a future update.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Continue")
            }
        }
    }
}
