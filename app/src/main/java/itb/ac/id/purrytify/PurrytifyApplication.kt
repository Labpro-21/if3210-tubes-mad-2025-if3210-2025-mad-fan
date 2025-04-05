package itb.ac.id.purrytify

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class PurrytifyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
//        RetrofitClient.init(applicationContext)

        // Initialize any libraries or components here
        // For example, if you're using Timber for logging:
        // Timber.plant(Timber.DebugTree())
    }
}