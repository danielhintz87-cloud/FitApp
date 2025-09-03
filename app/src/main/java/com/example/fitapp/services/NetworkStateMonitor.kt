package com.example.fitapp.services

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.fitapp.util.ApiCallWrapper
import com.example.fitapp.util.NetworkType
import com.example.fitapp.util.StructuredLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Network state monitoring service that tracks connectivity changes
 * and triggers appropriate actions like sync operations
 */
class NetworkStateMonitor(private val context: Context) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val monitorScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val _networkState = MutableSharedFlow<NetworkState>(replay = 1)
    val networkState: SharedFlow<NetworkState> = _networkState.asSharedFlow()
    
    private val _connectionQuality = MutableSharedFlow<ConnectionQuality>(replay = 1)
    val connectionQuality: SharedFlow<ConnectionQuality> = _connectionQuality.asSharedFlow()
    
    private var isMonitoring = false
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    
    init {
        // Initialize with current state
        updateNetworkState()
    }
    
    /**
     * Start monitoring network state changes
     */
    fun startMonitoring() {
        if (isMonitoring) return
        
        try {
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                .build()
            
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    StructuredLogger.info(
                        StructuredLogger.LogCategory.NETWORK,
                        "NetworkStateMonitor",
                        "Network became available: $network"
                    )
                    updateNetworkState()
                    onNetworkAvailable()
                }
                
                override fun onLost(network: Network) {
                    StructuredLogger.info(
                        StructuredLogger.LogCategory.NETWORK,
                        "NetworkStateMonitor",
                        "Network lost: $network"
                    )
                    updateNetworkState()
                    onNetworkLost()
                }
                
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    StructuredLogger.debug(
                        StructuredLogger.LogCategory.NETWORK,
                        "NetworkStateMonitor",
                        "Network capabilities changed: $network"
                    )
                    updateNetworkState()
                    updateConnectionQuality(networkCapabilities)
                }
                
                override fun onUnavailable() {
                    StructuredLogger.warning(
                        StructuredLogger.LogCategory.NETWORK,
                        "NetworkStateMonitor",
                        "Network unavailable"
                    )
                    updateNetworkState()
                }
            }
            
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
            isMonitoring = true
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.NETWORK,
                "NetworkStateMonitor",
                "Network monitoring started"
            )
            
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NETWORK,
                "NetworkStateMonitor",
                "Failed to start network monitoring",
                exception = e
            )
        }
    }
    
    /**
     * Stop monitoring network state changes
     */
    fun stopMonitoring() {
        if (!isMonitoring) return
        
        try {
            networkCallback?.let { callback ->
                connectivityManager.unregisterNetworkCallback(callback)
            }
            networkCallback = null
            isMonitoring = false
            
            StructuredLogger.info(
                StructuredLogger.LogCategory.NETWORK,
                "NetworkStateMonitor",
                "Network monitoring stopped"
            )
            
        } catch (e: Exception) {
            StructuredLogger.error(
                StructuredLogger.LogCategory.NETWORK,
                "NetworkStateMonitor",
                "Failed to stop network monitoring",
                exception = e
            )
        }
    }
    
    /**
     * Get current network state
     */
    fun getCurrentNetworkState(): NetworkState {
        val isConnected = ApiCallWrapper.isNetworkAvailable(context)
        val networkType = ApiCallWrapper.getNetworkType(context)
        
        return NetworkState(
            isConnected = isConnected,
            networkType = networkType,
            timestamp = System.currentTimeMillis()
        )
    }
    
    private fun updateNetworkState() {
        monitorScope.launch {
            try {
                val currentState = getCurrentNetworkState()
                _networkState.emit(currentState)
                
                StructuredLogger.debug(
                    StructuredLogger.LogCategory.NETWORK,
                    "NetworkStateMonitor",
                    "Network state updated: Connected=${currentState.isConnected}, Type=${currentState.networkType}"
                )
                
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.NETWORK,
                    "NetworkStateMonitor",
                    "Failed to update network state",
                    exception = e
                )
            }
        }
    }
    
    private fun updateConnectionQuality(capabilities: NetworkCapabilities) {
        monitorScope.launch {
            try {
                val quality = determineConnectionQuality(capabilities)
                _connectionQuality.emit(quality)
                
                StructuredLogger.debug(
                    StructuredLogger.LogCategory.NETWORK,
                    "NetworkStateMonitor",
                    "Connection quality updated: $quality"
                )
                
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.NETWORK,
                    "NetworkStateMonitor",
                    "Failed to update connection quality",
                    exception = e
                )
            }
        }
    }
    
    private fun determineConnectionQuality(capabilities: NetworkCapabilities): ConnectionQuality {
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                val linkDownstream = capabilities.linkDownstreamBandwidthKbps
                when {
                    linkDownstream > 10000 -> ConnectionQuality.EXCELLENT  // > 10 Mbps
                    linkDownstream > 5000 -> ConnectionQuality.GOOD        // > 5 Mbps
                    linkDownstream > 1000 -> ConnectionQuality.FAIR        // > 1 Mbps
                    else -> ConnectionQuality.POOR
                }
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                // Cellular quality assessment
                ConnectionQuality.FAIR // Default for cellular
            }
            else -> ConnectionQuality.POOR
        }
    }
    
    private fun onNetworkAvailable() {
        monitorScope.launch {
            try {
                StructuredLogger.info(
                    StructuredLogger.LogCategory.NETWORK,
                    "NetworkStateMonitor",
                    "Network available - triggering sync operations"
                )
                
                // Trigger immediate sync when network becomes available
                OfflineSyncManager.triggerImmediateSync(context)
                
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.NETWORK,
                    "NetworkStateMonitor",
                    "Error handling network available event",
                    exception = e
                )
            }
        }
    }
    
    private fun onNetworkLost() {
        monitorScope.launch {
            try {
                StructuredLogger.warning(
                    StructuredLogger.LogCategory.NETWORK,
                    "NetworkStateMonitor",
                    "Network lost - entering offline mode"
                )
                
                // Handle offline mode activation
                // Could show offline indicator in UI, cache operations, etc.
                
            } catch (e: Exception) {
                StructuredLogger.error(
                    StructuredLogger.LogCategory.NETWORK,
                    "NetworkStateMonitor",
                    "Error handling network lost event",
                    exception = e
                )
            }
        }
    }
    
    /**
     * Check if current connection is suitable for heavy operations (sync, downloads)
     */
    fun isConnectionSuitableForHeavyOperations(): Boolean {
        val currentState = getCurrentNetworkState()
        
        return when {
            !currentState.isConnected -> false
            currentState.networkType == NetworkType.WIFI -> true
            currentState.networkType == NetworkType.ETHERNET -> true
            currentState.networkType == NetworkType.CELLULAR -> {
                // Only allow on cellular if explicitly configured or emergency
                // This could be user-configurable
                false
            }
            else -> false
        }
    }
    
    /**
     * Get network type string for display
     */
    fun getNetworkTypeString(networkType: NetworkType): String {
        return when (networkType) {
            NetworkType.WIFI -> "WLAN"
            NetworkType.CELLULAR -> "Mobilfunk"
            NetworkType.ETHERNET -> "Ethernet"
            NetworkType.OTHER -> "Andere"
            NetworkType.NONE -> "Keine"
            NetworkType.UNKNOWN -> "Unbekannt"
            NetworkType.NOT_REQUIRED -> "Nicht erforderlich"
            NetworkType.CONNECTED -> "Verbunden"
        }
    }
}

/**
 * Data classes for network monitoring
 */
data class NetworkState(
    val isConnected: Boolean,
    val networkType: NetworkType,
    val timestamp: Long
)

enum class ConnectionQuality(val displayName: String) {
    EXCELLENT("Ausgezeichnet"),
    GOOD("Gut"),
    FAIR("Ausreichend"),
    POOR("Schlecht"),
    UNKNOWN("Unbekannt")
}

// Missing enum referenced in build errors
enum class NetworkStatus {
    CONNECTED,
    DISCONNECTED,
    CONNECTING
}

/**
 * Extension function to easily integrate network monitoring into components
 */
fun Context.createNetworkMonitor(): NetworkStateMonitor {
    return NetworkStateMonitor(this)
}