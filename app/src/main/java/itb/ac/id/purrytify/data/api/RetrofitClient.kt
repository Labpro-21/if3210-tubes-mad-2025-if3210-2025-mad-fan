//package itb.ac.id.purrytify.data.api
//
//import android.content.Context
//import itb.ac.id.purrytify.data.api.interceptors.AuthInterceptor
//import itb.ac.id.purrytify.data.api.interceptors.TokenManager
//import okhttp3.OkHttpClient
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
//object RetrofitClient {
//    private const val BASE_URL = "http://34.101.226.132:3000/"
//    private var tokenManager: TokenManager? = null
//
//    fun init(context: Context) {
//        if (tokenManager == null) {
//            tokenManager = TokenManager(context)
//        }
//    }
//
//    fun getTokenManager(): TokenManager {
//        return tokenManager ?: throw IllegalStateException("TokenManager is not initialized.")
//    }
//
//    private val client: OkHttpClient by lazy {
//        OkHttpClient.Builder()
//            .addInterceptor(AuthInterceptor(getTokenManager()))
//            .build()
//    }
//
//    val api: ApiService by lazy {
//        Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .client(client)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(ApiService::class.java)
//    }
//}
