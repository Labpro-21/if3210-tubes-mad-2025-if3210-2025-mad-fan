package itb.ac.id.purrytify.utils

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

data class AudioDevice(
    val id: Int,
    val name: String,
    val type: Int,
    val icon: ImageVector,
    val isConnected: Boolean = false
)

class AudioDeviceManager(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun getAvailableAudioDevices(): List<AudioDevice> {
        val devices = mutableListOf<AudioDevice>()
        
        // speaker hp sebagai default
        devices.add(
            AudioDevice(
                id = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                name = "Phone Speaker",
                type = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                icon = Icons.Default.VolumeUp,
                isConnected = true
            )
        )

        // Add earpiece
        // devices.add(
        //     AudioDevice(
        //         id = AudioDeviceInfo.TYPE_BUILTIN_EARPIECE,
        //         name = "Earpiece",
        //         type = AudioDeviceInfo.TYPE_BUILTIN_EARPIECE,
        //         icon = Icons.Default.PhoneAndroid,
        //         isConnected = true
        //     )
        // )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            
            for (deviceInfo in audioDevices) {
                when (deviceInfo.type) {
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> {
                        devices.add(
                            AudioDevice(
                                id = deviceInfo.id,
                                name = deviceInfo.productName?.toString() ?: "Bluetooth Headset",
                                type = deviceInfo.type,
                                icon = Icons.Default.Bluetooth,
                                isConnected = true
                            )
                        )
                    }
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> {
                        devices.add(
                            AudioDevice(
                                id = deviceInfo.id,
                                name = "Wired Headphones",
                                type = deviceInfo.type,
                                icon = Icons.Default.Headphones,
                                isConnected = true
                            )
                        )
                    }
                    AudioDeviceInfo.TYPE_WIRED_HEADSET -> {
                        devices.add(
                            AudioDevice(
                                id = deviceInfo.id,
                                name = "Wired Headset",
                                type = deviceInfo.type,
                                icon = Icons.Default.Headset,
                                isConnected = true
                            )
                        )
                    }
                    AudioDeviceInfo.TYPE_USB_HEADSET -> {
                        devices.add(
                            AudioDevice(
                                id = deviceInfo.id,
                                name = "USB Headset",
                                type = deviceInfo.type,
                                icon = Icons.Default.Usb,
                                isConnected = true
                            )
                        )
                    }
                }
            }
        } else {
            if (audioManager.isBluetoothA2dpOn) {
                devices.add(
                    AudioDevice(
                        id = -1,
                        name = "Bluetooth Device",
                        type = AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                        icon = Icons.Default.Bluetooth,
                        isConnected = true
                    )
                )
            }
            
            if (audioManager.isWiredHeadsetOn) {
                devices.add(
                    AudioDevice(
                        id = -2,
                        name = "Wired Headset",
                        type = AudioDeviceInfo.TYPE_WIRED_HEADSET,
                        icon = Icons.Default.Headset,
                        isConnected = true
                    )
                )
            }
        }

        // Remove duplicates by type but keep phone speaker always available
        return devices.distinctBy { it.type }
    }

    fun getCurrentAudioDevice(): AudioDevice? {
        val devices = getAvailableAudioDevices()
        
        return when {
            audioManager.isSpeakerphoneOn -> devices.find { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
            audioManager.isWiredHeadsetOn -> devices.find { 
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET || 
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES 
            }
            audioManager.isBluetoothA2dpOn -> devices.find { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP }
            else -> {
                // If no specific mode is active, return the first available device (usually speaker)
                devices.firstOrNull() ?: devices.find { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
            }
        }
    }

    fun setAudioDevice(device: AudioDevice): Boolean {
        return try {
            Log.d("AudioDeviceManager", "Switching to audio device: ${device.name}")
            
            // Set the audio mode to normal for music playback
            audioManager.mode = AudioManager.MODE_NORMAL
            
            // reset state audio routing
            if (audioManager.isBluetoothScoOn) {
                audioManager.stopBluetoothSco()
                audioManager.isBluetoothScoOn = false
            }
            audioManager.isSpeakerphoneOn = false
            
            when (device.type) {
                AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> {
                    // Force enable speakerphone to route audio to speaker
                    audioManager.isSpeakerphoneOn = true
                    Log.d("AudioDeviceManager", "Forced audio to phone speaker")
                }
                AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> {
                    // Keep speakerphone disabled for earpiece output
                    audioManager.isSpeakerphoneOn = false
                    Log.d("AudioDeviceManager", "Audio routed to earpiece")
                }
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> {
                    // For Bluetooth A2DP, disable speakerphone and let system route to Bluetooth
                    audioManager.isSpeakerphoneOn = false
                    Log.d("AudioDeviceManager", "Audio routed to Bluetooth A2DP")
                }
                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> {
                    // For wired devices, disable speakerphone
                    audioManager.isSpeakerphoneOn = false
                    Log.d("AudioDeviceManager", "Audio routed to wired device")
                }
            }
            
            Log.d("AudioDeviceManager", "Audio device switched successfully to: ${device.name}")
            Log.d("AudioDeviceManager", "Current state - isSpeakerphoneOn: ${audioManager.isSpeakerphoneOn}, isBluetoothA2dpOn: ${audioManager.isBluetoothA2dpOn}")
            true
        } catch (e: Exception) {
            Log.e("AudioDeviceManager", "Failed to switch audio device: ${e.message}")
            false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioDeviceSelectionDialog(
    availableDevices: List<AudioDevice>,
    currentDevice: AudioDevice?,
    onDeviceSelected: (AudioDevice) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Audio Output",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                availableDevices.forEach { device ->
                    val isSelected = currentDevice?.type == device.type
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDeviceSelected(device)
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = device.icon,
                            contentDescription = device.name,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Text(
                                    text = "Currently active",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
fun AudioRoutingButton(
    currentDevice: AudioDevice?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = currentDevice?.icon ?: Icons.Default.VolumeUp,
            contentDescription = "Audio Output",
            tint = Color.White
        )
    }
}
