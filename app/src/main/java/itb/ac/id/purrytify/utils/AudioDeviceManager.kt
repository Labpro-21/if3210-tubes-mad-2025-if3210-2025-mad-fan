package itb.ac.id.purrytify.utils
import android.os.Build
import android.util.Log
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
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

// Additional Bluetooth device type constants for compatibility
object BluetoothDeviceTypes {
    const val TYPE_BLUETOOTH_LE = 25
    const val TYPE_BLUETOOTH_BLE = 26
    const val TYPE_BLUETOOTH_HEARING_AID = 23
}

data class AudioDevice(
    val id: Int,
    val name: String,
    val type: Int,
    val icon: ImageVector,
    val isConnected: Boolean = false
)

class AudioDeviceManager(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun isDeviceCurrentlyActive(device: AudioDevice): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val deviceInfo = audioDevices.find { it.id == device.id }
            
            // Check if the device is still connected
            if (deviceInfo == null && device.type != AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                return false
            }
        }
        
        return when (device.type) {
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> audioManager.isSpeakerphoneOn
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> audioManager.isBluetoothA2dpOn
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_USB_HEADSET -> audioManager.isWiredHeadsetOn
            else -> false
        }
    }
    
    fun refreshDeviceList(): List<AudioDevice> {
        Log.d("AudioDeviceManager", "Refreshing audio device list")
        return getAvailableAudioDevices()
    }

    fun debugListAllDevices() {
        Log.d("AudioDeviceManager", "=== DEBUG: Listing all audio devices ===")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val inputDevices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
            val outputDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            
            Log.d("AudioDeviceManager", "Output devices (${outputDevices.size}):")
            outputDevices.forEachIndexed { index, device ->
                Log.d("AudioDeviceManager", "  [$index] ID: ${device.id}, Type: ${device.type}, Name: ${device.productName}, isSink: ${device.isSink}, isSource: ${device.isSource}")
            }
            
            Log.d("AudioDeviceManager", "Input devices (${inputDevices.size}):")
            inputDevices.forEachIndexed { index, device ->
                Log.d("AudioDeviceManager", "  [$index] ID: ${device.id}, Type: ${device.type}, Name: ${device.productName}, isSink: ${device.isSink}, isSource: ${device.isSource}")
            }
        }
        
        Log.d("AudioDeviceManager", "AudioManager state:")
        Log.d("AudioDeviceManager", "  isSpeakerphoneOn: ${audioManager.isSpeakerphoneOn}")
        Log.d("AudioDeviceManager", "  isBluetoothA2dpOn: ${audioManager.isBluetoothA2dpOn}")
        Log.d("AudioDeviceManager", "  isBluetoothScoOn: ${audioManager.isBluetoothScoOn}")
        Log.d("AudioDeviceManager", "  isWiredHeadsetOn: ${audioManager.isWiredHeadsetOn}")
        Log.d("AudioDeviceManager", "  mode: ${audioManager.mode}")
        Log.d("AudioDeviceManager", "=== END DEBUG ===")
    }

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
                Log.d("AudioDeviceManager", "Checking device: ${deviceInfo.productName} (Type: ${deviceInfo.type}, ID: ${deviceInfo.id})")
                
                when (deviceInfo.type) {
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> {
                        devices.add(
                            AudioDevice(
                                id = deviceInfo.id,
                                name = deviceInfo.productName?.toString() ?: "Bluetooth A2DP",
                                type = deviceInfo.type,
                                icon = Icons.Default.Bluetooth,
                                isConnected = true
                            )
                        )
                        Log.d("AudioDeviceManager", "Found Bluetooth A2DP device: ${deviceInfo.productName} (ID: ${deviceInfo.id})")
                    }
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> {
                        devices.add(
                            AudioDevice(
                                id = deviceInfo.id,
                                name = deviceInfo.productName?.toString() ?: "Bluetooth SCO",
                                type = deviceInfo.type,
                                icon = Icons.Default.Bluetooth,
                                isConnected = true
                            )
                        )
                        Log.d("AudioDeviceManager", "Found Bluetooth SCO device: ${deviceInfo.productName} (ID: ${deviceInfo.id})")
                    }
                    BluetoothDeviceTypes.TYPE_BLUETOOTH_LE -> { // TYPE_BLUETOOTH_LE (not available in older Android versions)
                        devices.add(
                            AudioDevice(
                                id = deviceInfo.id,
                                name = deviceInfo.productName?.toString() ?: "Bluetooth LE",
                                type = deviceInfo.type,
                                icon = Icons.Default.Bluetooth,
                                isConnected = true
                            )
                        )
                        Log.d("AudioDeviceManager", "Found Bluetooth LE device: ${deviceInfo.productName} (ID: ${deviceInfo.id})")
                    }
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> {
                        devices.add(
                            AudioDevice(
                                id = deviceInfo.id,
                                name = deviceInfo.productName?.toString() ?: "Wired Headphones",
                                type = deviceInfo.type,
                                icon = Icons.Default.Headphones,
                                isConnected = true
                            )
                        )
                        Log.d("AudioDeviceManager", "Found Wired Headphones (ID: ${deviceInfo.id})")
                    }
                    AudioDeviceInfo.TYPE_WIRED_HEADSET -> {
                        devices.add(
                            AudioDevice(
                                id = deviceInfo.id,
                                name = deviceInfo.productName?.toString() ?: "Wired Headset",
                                type = deviceInfo.type,
                                icon = Icons.Default.Headset,
                                isConnected = true
                            )
                        )
                        Log.d("AudioDeviceManager", "Found Wired Headset (ID: ${deviceInfo.id})")
                    }
                    AudioDeviceInfo.TYPE_USB_HEADSET -> {
                        devices.add(
                            AudioDevice(
                                id = deviceInfo.id,
                                name = deviceInfo.productName?.toString() ?: "USB Headset",
                                type = deviceInfo.type,
                                icon = Icons.Default.Usb,
                                isConnected = true
                            )
                        )
                        Log.d("AudioDeviceManager", "Found USB Headset (ID: ${deviceInfo.id})")
                    }
                    else -> {
                        // Log all other device types for debugging
                        if (deviceInfo.isSink) { // Only output devices
                            Log.d("AudioDeviceManager", "Found other output device: ${deviceInfo.productName} (Type: ${deviceInfo.type}, ID: ${deviceInfo.id})")
                            
                            // Add support for other device types that might be Bluetooth
                            val deviceName = deviceInfo.productName?.toString() ?: "Unknown Device"
                            val isBluetoothDevice = deviceName.contains("bluetooth", ignoreCase = true) || 
                                                  deviceInfo.type in listOf(
                                                      BluetoothDeviceTypes.TYPE_BLUETOOTH_LE,
                                                      BluetoothDeviceTypes.TYPE_BLUETOOTH_BLE,
                                                      BluetoothDeviceTypes.TYPE_BLUETOOTH_HEARING_AID,
                                                      7, 8, 24, 26, 27 // Other common Bluetooth types
                                                  )
                            
                            if (isBluetoothDevice) {
                                devices.add(
                                    AudioDevice(
                                        id = deviceInfo.id,
                                        name = deviceName,
                                        type = deviceInfo.type,
                                        icon = Icons.Default.Bluetooth,
                                        isConnected = true
                                    )
                                )
                                Log.d("AudioDeviceManager", "Added Bluetooth device: $deviceName (Type: ${deviceInfo.type})")
                            }
                        }
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

        // Remove duplicates by id (not type) to allow multiple devices of same type
        // Keep phone speaker always available
        return devices.distinctBy { it.id }
    }

    fun getCurrentAudioDevice(): AudioDevice? {
        val devices = getAvailableAudioDevices()
        
        Log.d("AudioDeviceManager", "Detecting current audio device...")
        Log.d("AudioDeviceManager", "isSpeakerphoneOn: ${audioManager.isSpeakerphoneOn}")
        Log.d("AudioDeviceManager", "isBluetoothA2dpOn: ${audioManager.isBluetoothA2dpOn}")
        Log.d("AudioDeviceManager", "isBluetoothScoOn: ${audioManager.isBluetoothScoOn}")
        Log.d("AudioDeviceManager", "isWiredHeadsetOn: ${audioManager.isWiredHeadsetOn}")
        
        // Enhanced detection for API 29+ (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                // First check if we can detect a connected Bluetooth device that's likely active
                if (audioManager.isBluetoothA2dpOn || audioManager.isBluetoothScoOn) {
                    val bluetoothDevice = devices.find { isBluetoothDevice(it.type) && it.isConnected }
                    if (bluetoothDevice != null) {
                        Log.d("AudioDeviceManager", "API 29+: Current device is Bluetooth: ${bluetoothDevice.name}")
                        return bluetoothDevice
                    }
                }
                
                // Then check for wired headset
                if (audioManager.isWiredHeadsetOn) {
                    val wiredDevice = devices.find { isWiredDevice(it.type) && it.isConnected }
                    if (wiredDevice != null) {
                        Log.d("AudioDeviceManager", "API 29+: Current device is wired: ${wiredDevice.name}")
                        return wiredDevice
                    }
                }
                
                // Then check for speaker
                if (audioManager.isSpeakerphoneOn) {
                    val speaker = devices.find { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                    if (speaker != null) {
                        Log.d("AudioDeviceManager", "API 29+: Current device is speaker")
                        return speaker
                    }
                }
                
                // If we get here and haven't identified a device, use a more thorough approach
                // for API 29+ by directly checking audio devices
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                    for (deviceInfo in audioDevices) {
                        // Check if this device might be the active one
                        if (deviceInfo.isSink) {
                            // Try to match with our device list
                            val matchedDevice = devices.find { it.id == deviceInfo.id }
                            if (matchedDevice != null) {
                                Log.d("AudioDeviceManager", "API 29+: Detected active device: ${matchedDevice.name}")
                                return matchedDevice
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AudioDeviceManager", "Error in API 29+ device detection: ${e.message}", e)
                // Fall back to legacy detection
            }
        }
        
        // Legacy detection for older Android versions or as fallback
        return when {
            audioManager.isSpeakerphoneOn -> {
                val speaker = devices.find { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                Log.d("AudioDeviceManager", "Current device: Phone Speaker")
                speaker
            }
            audioManager.isBluetoothA2dpOn || audioManager.isBluetoothScoOn -> {
                val bluetoothDevice = devices.find { isBluetoothDevice(it.type) }
                Log.d("AudioDeviceManager", "Current device: Bluetooth - ${bluetoothDevice?.name}")
                bluetoothDevice
            }
            audioManager.isWiredHeadsetOn -> {
                val wiredDevice = devices.find { isWiredDevice(it.type) }
                Log.d("AudioDeviceManager", "Current device: Wired - ${wiredDevice?.name}")
                wiredDevice
            }
            else -> {
                // If no specific mode is detected, check what devices are available
                // and prioritize non-speaker devices (as they might be the active ones)
                val nonSpeakerDevice = devices.find { 
                    it.isConnected && it.type != AudioDeviceInfo.TYPE_BUILTIN_SPEAKER 
                }
                
                val currentDevice = nonSpeakerDevice ?: devices.find { 
                    it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER 
                }
                
                Log.d("AudioDeviceManager", "Current device (default): ${currentDevice?.name}")
                currentDevice
            }
        }
    }

    fun setAudioDevice(device: AudioDevice): Boolean {
        return try {
            Log.d("AudioDeviceManager", "Switching to audio device: ${device.name} (ID: ${device.id}, Type: ${device.type})")
            
            // Set audio mode to normal for music playback
            audioManager.mode = AudioManager.MODE_NORMAL
            
            // Request audio focus first - this is critical for routing to work properly
            val result = audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.w("AudioDeviceManager", "Audio focus request not granted: $result")
                // Continue anyway, but note the issue
            }
            
            // Reset all audio routing states first
            resetAudioRouting()
            
            // Wait for reset to take effect - increased for better reliability
            Thread.sleep(200)
            
            // For API 29+ (Android 10+), use more specific methods for routing
            var success = false
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // API 29+ requires additional handling for audio routing
                try {
                    // First make sure we have the permission to modify audio settings
                    if (context.checkSelfPermission(android.Manifest.permission.MODIFY_AUDIO_SETTINGS) 
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        Log.e("AudioDeviceManager", "Missing MODIFY_AUDIO_SETTINGS permission")
                        // We'll still try to switch even without permission
                    }
                    
                    // Try to apply the appropriate routing
                    when {
                        device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> {
                            // For speaker, explicitly set speakerphone on
                            audioManager.isSpeakerphoneOn = true
                            // Also set microphone mode to normal
                            audioManager.mode = AudioManager.MODE_NORMAL
                            success = true
                        }
                        isBluetoothDevice(device.type) -> {
                            // Turn off speaker first
                            audioManager.isSpeakerphoneOn = false
                            
                            // For Bluetooth, use device-specific approaches
                            if (device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
                                audioManager.startBluetoothSco()
                                audioManager.isBluetoothScoOn = true
                                // Give SCO time to start
                                Thread.sleep(300)
                            } else if (device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP) {
                                // Use a more aggressive approach for A2DP
                                try {
                                    // Some devices need to toggle speaker to trigger proper BT routing
                                    audioManager.isSpeakerphoneOn = true
                                    Thread.sleep(100)
                                    audioManager.isSpeakerphoneOn = false
                                    // Force audio mode to ensure proper routing
                                    audioManager.mode = AudioManager.MODE_NORMAL
                                } catch (e: Exception) {
                                    Log.w("AudioDeviceManager", "A2DP toggle failed: ${e.message}")
                                }
                            }
                            success = true
                        }
                        isWiredDevice(device.type) -> {
                            // For wired devices, turn off speaker and let system handle
                            audioManager.isSpeakerphoneOn = false
                            // Sometimes toggling the mode can help
                            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                            Thread.sleep(100)
                            audioManager.mode = AudioManager.MODE_NORMAL
                            success = true
                        }
                        else -> {
                            Log.w("AudioDeviceManager", "Unknown device type: ${device.type}")
                            success = false
                        }
                    }
                    
                    // For API M+, try using AudioDeviceInfo directly if we have the device ID
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && device.id > 0) {
                        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                        val targetDevice = devices.find { it.id == device.id }
                        
                        if (targetDevice != null) {
                            Log.d("AudioDeviceManager", "Found matching device by ID: ${targetDevice.productName}")
                            // If we're on Android 11+, we can set preferred device
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                try {
                                    // This method is available in Android 11+
                                    val setCommunicationDeviceMethod = 
                                        AudioManager::class.java.getMethod("setCommunicationDevice", AudioDeviceInfo::class.java)
                                    val result = setCommunicationDeviceMethod.invoke(audioManager, targetDevice) as Boolean
                                    Log.d("AudioDeviceManager", "setCommunicationDevice result: $result")
                                    
                                    if (result) {
                                        success = true
                                    }
                                } catch (e: Exception) {
                                    Log.e("AudioDeviceManager", "Failed to use setCommunicationDevice: ${e.message}")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AudioDeviceManager", "API 29+ audio routing error: ${e.message}", e)
                }
            } else {
                // Legacy approach for older Android versions
                success = when {
                    device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> {
                        setToSpeaker()
                    }
                    isBluetoothDevice(device.type) -> {
                        setToBluetoothDevice(device)
                    }
                    isWiredDevice(device.type) -> {
                        setToWiredDevice(device)
                    }
                    else -> {
                        Log.w("AudioDeviceManager", "Unknown device type: ${device.type}")
                        false
                    }
                }
            }
            
            // Give system time to apply changes - increased for better reliability
            Thread.sleep(300)
            
            // Verify the change took effect and retry once if not
            val verificationSuccess = verifyAudioRouting(device)
            if (!verificationSuccess && success) {
                Log.w("AudioDeviceManager", "First attempt succeeded but verification failed. Retrying...")
                // Retry the routing once more with a different approach
                when {
                    device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> {
                        // More aggressive speaker approach
                        audioManager.mode = AudioManager.MODE_NORMAL
                        audioManager.isSpeakerphoneOn = true
                    }
                    isBluetoothDevice(device.type) -> {
                        // Toggle modes to force Bluetooth routing
                        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                        Thread.sleep(100)
                        audioManager.mode = AudioManager.MODE_NORMAL
                    }
                }
                // Give one more chance to take effect
                Thread.sleep(300)
            }
            
            if (success) {
                Log.d("AudioDeviceManager", "Audio device switched successfully to: ${device.name}")
                Log.d("AudioDeviceManager", "Current state - isSpeakerphoneOn: ${audioManager.isSpeakerphoneOn}, isBluetoothA2dpOn: ${audioManager.isBluetoothA2dpOn}, isWiredHeadsetOn: ${audioManager.isWiredHeadsetOn}")
            } else {
                Log.e("AudioDeviceManager", "Failed to switch to device: ${device.name}")
            }
            
            success
        } catch (e: Exception) {
            Log.e("AudioDeviceManager", "Exception while switching audio device: ${e.message}", e)
            false
        }
    }
    
    private fun resetAudioRouting() {
        try {
            // Stop any ongoing Bluetooth SCO
            if (audioManager.isBluetoothScoOn) {
                audioManager.stopBluetoothSco()
                audioManager.isBluetoothScoOn = false
            }
            
            // Reset audio mode to normal
            audioManager.mode = AudioManager.MODE_NORMAL
            
            // Disable speakerphone
            audioManager.isSpeakerphoneOn = false
            
            // If on Android 11+, we can clear the communication device
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val clearCommunicationDeviceMethod = 
                        AudioManager::class.java.getMethod("clearCommunicationDevice")
                    clearCommunicationDeviceMethod.invoke(audioManager)
                    Log.d("AudioDeviceManager", "Communication device cleared")
                } catch (e: Exception) {
                    Log.w("AudioDeviceManager", "Failed to clear communication device: ${e.message}")
                }
            }
            
            // Release audio focus
            audioManager.abandonAudioFocus(null)
            
            Log.d("AudioDeviceManager", "Audio routing reset completed")
        } catch (e: Exception) {
            Log.e("AudioDeviceManager", "Error resetting audio routing: ${e.message}")
        }
    }
    
    private fun setToSpeaker(): Boolean {
        return try {
            audioManager.isSpeakerphoneOn = true
            Log.d("AudioDeviceManager", "Audio routed to phone speaker")
            true
        } catch (e: Exception) {
            Log.e("AudioDeviceManager", "Failed to route to speaker: ${e.message}")
            false
        }
    }
    
    private fun setToBluetoothDevice(device: AudioDevice): Boolean {
        return try {
            // Ensure speakerphone is off
            audioManager.isSpeakerphoneOn = false
            
            // For Bluetooth devices, let the system handle routing
            // The audio should automatically route to the connected Bluetooth device
            when (device.type) {
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> {
                    // For SCO devices, we might need to start SCO
                    try {
                        audioManager.startBluetoothSco()
                        audioManager.isBluetoothScoOn = true
                        Log.d("AudioDeviceManager", "Started Bluetooth SCO for: ${device.name}")
                    } catch (e: Exception) {
                        Log.w("AudioDeviceManager", "Failed to start Bluetooth SCO: ${e.message}")
                    }
                }
                else -> {
                    // For A2DP and other Bluetooth types, system should route automatically
                    Log.d("AudioDeviceManager", "Audio routing to Bluetooth device: ${device.name}")
                }
            }
            
            Log.d("AudioDeviceManager", "Audio routed to Bluetooth device: ${device.name}")
            true
        } catch (e: Exception) {
            Log.e("AudioDeviceManager", "Failed to route to Bluetooth device: ${e.message}")
            false
        }
    }
    
    private fun setToWiredDevice(device: AudioDevice): Boolean {
        return try {
            // For wired devices, disable speakerphone and let system route
            audioManager.isSpeakerphoneOn = false
            Log.d("AudioDeviceManager", "Audio routed to wired device: ${device.name}")
            true
        } catch (e: Exception) {
            Log.e("AudioDeviceManager", "Failed to route to wired device: ${e.message}")
            false
        }
    }
    
    private fun isBluetoothDevice(type: Int): Boolean {
        return type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || 
               type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
               type == BluetoothDeviceTypes.TYPE_BLUETOOTH_LE ||
               type == BluetoothDeviceTypes.TYPE_BLUETOOTH_BLE ||
               type == BluetoothDeviceTypes.TYPE_BLUETOOTH_HEARING_AID ||
               type in listOf(7, 8, 24, 26, 27) // Other common Bluetooth types
    }
    
    private fun isWiredDevice(type: Int): Boolean {
        return type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
               type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
               type == AudioDeviceInfo.TYPE_USB_HEADSET
    }

    fun forceAudioRouting(device: AudioDevice): Boolean {
        Log.d("AudioDeviceManager", "Force routing to: ${device.name}")
        
        // First call the regular setAudioDevice
        var success = setAudioDevice(device)
        
        if (!success) {
            Log.w("AudioDeviceManager", "Standard routing failed, attempting forced routing")
            
            // Try a more aggressive approach with multiple retries
            for (attempt in 1..3) {
                if (success) break
                
                Log.d("AudioDeviceManager", "Force routing attempt $attempt")
                
                // Reset audio routing completely
                resetAudioRouting()
                Thread.sleep(300)
                
                // For each device type, use the most aggressive approach possible
                when {
                    device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> {
                        // For speaker, set multiple properties to ensure it takes effect
                        audioManager.mode = AudioManager.MODE_NORMAL
                        audioManager.isSpeakerphoneOn = true
                        
                        // Some devices need additional steps
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                // Toggle to communication mode and back
                                audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                                Thread.sleep(100)
                                audioManager.mode = AudioManager.MODE_NORMAL
                                audioManager.isSpeakerphoneOn = true
                            } catch (e: Exception) {
                                Log.w("AudioDeviceManager", "Speaker toggle error: ${e.message}")
                            }
                        }
                        
                        success = audioManager.isSpeakerphoneOn
                    }
                    isBluetoothDevice(device.type) -> {
                        // For Bluetooth devices, try multiple approaches
                        try {
                            // Ensure speaker is off
                            audioManager.isSpeakerphoneOn = false
                            
                            when (device.type) {
                                AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> {
                                    // For SCO, toggle on/off cycle can help
                                    audioManager.stopBluetoothSco()
                                    Thread.sleep(200)
                                    audioManager.startBluetoothSco()
                                    audioManager.isBluetoothScoOn = true
                                    Thread.sleep(500) // Give more time for SCO to connect
                                    
                                    // Set communication mode which can help with SCO
                                    audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                                    Thread.sleep(100)
                                    
                                    success = audioManager.isBluetoothScoOn
                                }
                                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> {
                                    // For A2DP, try mode changes and wait longer
                                    audioManager.mode = AudioManager.MODE_NORMAL
                                    Thread.sleep(300)
                                    
                                    // Toggle speaker on/off can trigger A2DP routing
                                    audioManager.isSpeakerphoneOn = true
                                    Thread.sleep(100)
                                    audioManager.isSpeakerphoneOn = false
                                    Thread.sleep(500)
                                    
                                    success = audioManager.isBluetoothA2dpOn || !audioManager.isSpeakerphoneOn
                                }
                                else -> {
                                    // For other Bluetooth types, try general approach
                                    audioManager.mode = AudioManager.MODE_NORMAL
                                    Thread.sleep(500) // Longer wait
                                    
                                    // Check if we succeeded
                                    success = !audioManager.isSpeakerphoneOn
                                }
                            }
                            
                            // If device is Android 11+ try setCommunicationDevice
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && device.id > 0) {
                                try {
                                    val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                                    val targetDevice = devices.find { it.id == device.id }
                                    
                                    if (targetDevice != null) {
                                        val setCommunicationDeviceMethod = 
                                            AudioManager::class.java.getMethod("setCommunicationDevice", AudioDeviceInfo::class.java)
                                        val result = setCommunicationDeviceMethod.invoke(audioManager, targetDevice) as Boolean
                                        
                                        if (result) {
                                            Log.d("AudioDeviceManager", "Force routing via setCommunicationDevice succeeded")
                                            success = true
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.w("AudioDeviceManager", "setCommunicationDevice error: ${e.message}")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("AudioDeviceManager", "Force Bluetooth routing error: ${e.message}")
                        }
                    }
                    isWiredDevice(device.type) -> {
                        // For wired devices, ensure speaker is off and try mode changes
                        audioManager.isSpeakerphoneOn = false
                        
                        // Toggle mode can help with wired routing
                        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                        Thread.sleep(100)
                        audioManager.mode = AudioManager.MODE_NORMAL
                        Thread.sleep(300)
                        
                        success = !audioManager.isSpeakerphoneOn && audioManager.isWiredHeadsetOn
                    }
                }
                
                // Give time for changes to take effect
                Thread.sleep(500)
                
                // Verify if routing worked
                val verificationSuccess = verifyAudioRouting(device)
                if (verificationSuccess) {
                    Log.d("AudioDeviceManager", "Force routing verified successful on attempt $attempt")
                    success = true
                    break
                }
            }
        }
        
        if (success) {
            Log.d("AudioDeviceManager", "Force routing succeeded for device: ${device.name}")
        } else {
            Log.e("AudioDeviceManager", "Force routing failed for device: ${device.name}")
        }
        
        return success
    }
    
    private fun verifyAudioRouting(device: AudioDevice): Boolean {
        try {
            Log.d("AudioDeviceManager", "Verifying audio routing to: ${device.name}")
            
            // Check if the current state matches what we expect for this device type
            val expectedState = when {
                device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> {
                    // For speaker, we expect speakerphone to be on
                    audioManager.isSpeakerphoneOn
                }
                isBluetoothDevice(device.type) -> {
                    // For Bluetooth devices, one of these should be true
                    when (device.type) {
                        AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> audioManager.isBluetoothScoOn
                        else -> audioManager.isBluetoothA2dpOn || !audioManager.isSpeakerphoneOn
                    }
                }
                isWiredDevice(device.type) -> {
                    // For wired devices, check if speaker is off and wired headset is detected
                    !audioManager.isSpeakerphoneOn && audioManager.isWiredHeadsetOn
                }
                else -> {
                    // For unknown types, we can't verify
                    Log.w("AudioDeviceManager", "Cannot verify unknown device type: ${device.type}")
                    true // Assume success for unknown types
                }
            }
            
            Log.d("AudioDeviceManager", "Verification result: $expectedState")
            return expectedState
        } catch (e: Exception) {
            Log.e("AudioDeviceManager", "Error verifying audio routing: ${e.message}")
            return false
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
                    val isSelected = currentDevice?.id == device.id
                    
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
