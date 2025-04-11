package itb.ac.id.purrytify.service

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import itb.ac.id.purrytify.data.repository.AuthRepository
import itb.ac.id.purrytify.ui.auth.LoginActivity
import java.util.concurrent.TimeUnit

@HiltWorker
class TokenCheckService @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val authRepository: AuthRepository
): CoroutineWorker (appContext, workerParams){
    override suspend fun doWork(): Result {
        Log.d("TokenCheckService", "Checking token validity...")

        return try {
            val verifyResponse = authRepository.verifyToken()
            Log.d("TokenCheckService", "Token verification response: ${verifyResponse.code()}")

            when {
                verifyResponse.isSuccessful -> {
                    Log.d("TokenCheckService", "Token valid")
                    TokenCheckServiceScheduler.scheduleTokenCheck(applicationContext)
                    Result.success()
                }
                // TODO: change this to 401 when the backend is fixed
                verifyResponse.code() == 403 || verifyResponse.code() == 401 -> {
                    Log.d("TokenCheckService", "Token expired, refresh")

                    try {
                        val refreshResponse = authRepository.refreshToken()
                        if (refreshResponse.isSuccessful) {
                            Log.d("TokenCheckService", "Token refreshed successfully")
                            TokenCheckServiceScheduler.scheduleTokenCheck(applicationContext)
                            Result.success()
                        } else {
                            Log.d("TokenCheckService", "Refresh failed - logging out")
                            authRepository.logout()
                            val intent =
                                Intent(applicationContext, LoginActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                }
                            applicationContext.startActivity(intent)
                            Result.failure()
                        }
                    } catch (e: Exception) {
                        Log.e("TokenCheckService", "Exception during refresh: ${e.message}")
                        Result.retry()
                    }
                }

                else -> {
                    Log.d("TokenCheckService", "Unexpected error: ${verifyResponse.code()}")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e("TokenCheckService", "Exception during verify: ${e.message}")
            Result.retry()
        }
    }
}

object TokenCheckServiceScheduler {
    fun scheduleTokenCheck(context: Context) {
        val request = OneTimeWorkRequestBuilder<TokenCheckService>()
            // TODO change it to 5 minute
            .setInitialDelay(10, TimeUnit.SECONDS) // Check in 5 minutes
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                5,
                TimeUnit.SECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "TokenCheckService",
            ExistingWorkPolicy.REPLACE,
            request)

    }
}