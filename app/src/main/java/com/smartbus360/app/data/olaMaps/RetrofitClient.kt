package com.smartbus360.app.data.olaMaps

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.olamaps.io/"

    val olaMapsService: OlaMapsService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OlaMapsService::class.java)
    }
}