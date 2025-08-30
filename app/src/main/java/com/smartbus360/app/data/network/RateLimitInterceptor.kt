package com.smartbus360.app.data.network

import okhttp3.Interceptor
import okhttp3.Response

class RateLimitInterceptor : Interceptor {
    private val maxRequestsPerSecond = 1 // Change this based on your API's rate limits
    private val requestTimeStamps = mutableListOf<Long>()

    override fun intercept(chain: Interceptor.Chain): Response {
        synchronized(this) {
            val now = System.currentTimeMillis()
            requestTimeStamps.removeAll { it < now - 1000 } // Keep timestamps from the last second

            if (requestTimeStamps.size >= maxRequestsPerSecond) {
                Thread.sleep(1000) // Wait for 1 second if the limit is reached
            }
            requestTimeStamps.add(now)
        }
        return chain.proceed(chain.request())
    }
}
