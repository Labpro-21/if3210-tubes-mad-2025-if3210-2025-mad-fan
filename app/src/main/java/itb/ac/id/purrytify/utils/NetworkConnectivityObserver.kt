package itb.ac.id.purrytify.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

enum class ConnectionStatus {
    Available, Unavailable, Losing, Lost
}

interface ConnectivityObserver {
    fun observe(): Flow<ConnectionStatus>
}

class NetworkConnectivityObserver(
    private val context: Context
): ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun observe(): Flow<ConnectionStatus> {
        return callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch { send(ConnectionStatus.Available) }
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    val currentStatus = getCurrentConnectivityStatus(connectivityManager)
                    if (currentStatus != ConnectionStatus.Available) {
                        launch { send(ConnectionStatus.Losing) }
                    } else {
                        Log.d("Network", "Ignored Losing event — another network is still available")
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    // Check if any active network still has INTERNET
                    val currentStatus = getCurrentConnectivityStatus(connectivityManager)
                    if (currentStatus != ConnectionStatus.Available) {
                        launch { send(ConnectionStatus.Lost) }
                    } else {
                        Log.d("Network", "Ignored Lost event — another network is still available")
                    }
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    val currentStatus = getCurrentConnectivityStatus(connectivityManager)
                    if (currentStatus != ConnectionStatus.Available) {
                        launch { send(ConnectionStatus.Unavailable) }
                    } else {
                        Log.d("Network", "Ignored Unavailable event — another network is still available")
                    }
                }
            }

            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()

            connectivityManager.registerNetworkCallback(networkRequest, callback)

            // Cek status
            val currentStatus = getCurrentConnectivityStatus(connectivityManager)
            send(currentStatus)
            Log.d("Network", "Current status : $currentStatus" )

            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
    }

    private fun getCurrentConnectivityStatus(
        connectivityManager: ConnectivityManager
    ): ConnectionStatus {
        val network = connectivityManager.activeNetwork ?: return ConnectionStatus.Unavailable
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return ConnectionStatus.Unavailable

        return if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            ConnectionStatus.Available
        } else {
            ConnectionStatus.Unavailable
        }
    }
}