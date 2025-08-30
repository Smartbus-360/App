package com.smartbus360.app.data.olaMaps

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface OAuthService {
    @FormUrlEncoded
    @POST("realms/olamaps/protocol/openid-connect/token")
    suspend fun getAccessToken(
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("scope") scope: String = "openid",
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): Response<TokenResponse>
}

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String
)

val retrofit = Retrofit.Builder()
    .baseUrl("https://account.olamaps.io/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val oAuthService = retrofit.create(OAuthService::class.java)
