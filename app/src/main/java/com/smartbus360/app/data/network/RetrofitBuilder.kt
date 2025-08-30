package com.smartbus360.app.data.network

import android.util.Log
import com.google.firebase.ktx.BuildConfig
import com.smartbus360.app.data.api.ApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager



//object RetrofitBuilder {
//
//    private fun getRetrofit(): Retrofit {
//        val client = OkHttpClient.Builder()
//        return Retrofit.Builder()
//            .baseUrl("https://2494-2401-4900-1ca8-9b21-e56c-6f5a-7962-9646.ngrok-free.app/")        //       DEV_ALTERNATE_BASE_URL    DEV_STAGING_BASE_URL
//           // .callFactory(client)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }
//
//    val apiService: ApiService = getRetrofit().create(ApiService::class.java)
//}

val baseUrl = com.smartbus360.app.BuildConfig.API_BASE_URL


object RetrofitBuilder {

    // Custom interceptor to handle error responses
    private fun getCustomInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)

            // Handle specific error codes
            when (response.code) {
                400 -> Log.e("Bad Request", "Error: ${response.message}")
                401 -> Log.e("Unauthorized", "Error: ${response.message}")
                404 -> Log.e("Not Found", "Error: ${response.message}")
                else -> {
                    if (!response.isSuccessful) {
                        Log.e("Unexpected Error", "Error code: ${response.code}, message: ${response.message}")
                    }
                }
            }

            response
        }
    }

    // Function to create OkHttpClient with SSL configuration
    private fun getOkHttpClient(): OkHttpClient {
        try {
            // TrustManager for accepting all certificates
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )

            // Create an SSLContext that uses our trust manager
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, SecureRandom())

            val sslSocketFactory = sslContext.socketFactory

            // Logging interceptor for debugging purposes
            val logging = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            }

            // Build OkHttpClient
            return OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager) // Custom SSL Factory
                .hostnameVerifier { _, _ -> true } // Trust all hostnames
                .addInterceptor(logging) // Logging interceptor
                .addInterceptor(getCustomInterceptor()) // Custom error handling interceptor
                .connectTimeout(60, TimeUnit.SECONDS) // Connection timeout
                .readTimeout(60, TimeUnit.SECONDS) // Read timeout
                .writeTimeout(60, TimeUnit.SECONDS) // Write timeout
                .build()

        } catch (e: Exception) {
            throw RuntimeException("Failed to create a secure client: ${e.message}", e)
        }
    }

    // Function to get Retrofit instance
    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(
                if (BuildConfig.BUILD_TYPE == "release")
                    "https://api.smartbus360.com/"
//                "https://staging.smartbus360.com/"
                else
                    "https://api.smartbus360.com/"
//                    "https://staging.smartbus360.com/"

            ) // Base URL
            .client(getOkHttpClient()) // Attach custom OkHttpClient
            .addConverterFactory(GsonConverterFactory.create()) // JSON converter
            .build()
    }
//    https://staging.smartbus360.com

    // API Service instance
    val apiService: ApiService = getRetrofit().create(ApiService::class.java)
}
