package com.smartbus360.app.data.api

import com.smartbus360.app.data.model.request.BusReachedStoppageRequest
import com.smartbus360.app.data.model.request.LoginRequest
import com.smartbus360.app.data.model.request.LoginStudentRequest
import com.smartbus360.app.data.model.request.MarkFinalStopRequest
import com.smartbus360.app.data.model.request.UpdateLocationRequest
import com.smartbus360.app.data.model.response.AdvertisementBannerResponse
import com.smartbus360.app.data.model.response.BusNotificationResponse
import com.smartbus360.app.data.model.response.BusReachedStoppageResponse
import com.smartbus360.app.data.model.response.BusReplacedResponse
import com.smartbus360.app.data.model.response.DateLogResponse
import com.smartbus360.app.data.model.response.DriverLocationsListResponse
import com.smartbus360.app.data.model.response.DriverLoginResponse
import com.smartbus360.app.data.model.response.DriversList
import com.smartbus360.app.data.model.response.GetDriverDetailResponseNewXX
import com.smartbus360.app.data.model.response.GetUserDetailResponseX
import com.smartbus360.app.data.model.response.MarkFinalStopResponse
import com.smartbus360.app.data.model.response.NotificationResponse
import com.smartbus360.app.data.model.response.ReachTimeResponse
import com.smartbus360.app.data.model.response.StudentLoginResponse
import com.smartbus360.app.data.model.response.UpdateLocationResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.Response
import com.smartbus360.app.data.model.response.AdminLoginResponse
import retrofit2.http.Headers
import com.smartbus360.app.data.model.request.AdminLoginRequest
import com.smartbus360.app.data.model.request.QrExchangeRequest


interface ApiService {

    @GET("drivers")
    suspend fun getDrivers(): DriversList


    @POST("api/login/driver")
    suspend fun login(@Body data: LoginRequest): DriverLoginResponse

    @POST("api/login/user")
    suspend fun loginStudent(@Body data: LoginStudentRequest): StudentLoginResponse

    @GET("api/driver/details/{driverId}")
    suspend fun getDriverDetail(
        @Path("driverId") userId: Int,
        @Header("Authorization") token: String
    ): GetDriverDetailResponseNewXX

    @GET("api/user/details/{userId}")
    suspend fun getUserDetail(
        @Path("userId") userId: Int,
        @Header("Authorization") token: String
    ): GetUserDetailResponseX

    @GET("api/user/details/{userId}")
    suspend fun getUserDetail2(
        @Path("userId") userId: Int,
        @Header("Authorization") token: String
    ): GetUserDetailResponseX

    @GET("api/driver/details/{driverId}")
    suspend fun getDriverDetail2(
        @Path("driverId") userId: Int,
        @Header("Authorization") token: String
    ): GetDriverDetailResponseNewXX

    @GET("api/driver/details/{driverId}")
    suspend fun getDriverDetailNew(
        @Path("driverId") userId: Int,
        @Header("Authorization") token: String
    ): GetDriverDetailResponseNewXX

    @POST("api/stoppage/reached")
    suspend fun busReachedStoppage(@Header("Authorization") token: String,
        @Body data: BusReachedStoppageRequest): BusReachedStoppageResponse

    @GET("api/bus/replacement/{busId}")
    suspend fun getBusReplacedStatus(
        @Path("busId") busId: Int,
        @Header("Authorization") token: String
    ): BusReplacedResponse

    @POST("api/mark-final-stop")
    suspend fun markFinalStop(@Header("Authorization") token: String,
                                   @Body data: MarkFinalStopRequest): MarkFinalStopResponse

    @GET("api/advertisement/banner")
    suspend fun advertisementBanner(): AdvertisementBannerResponse

    @GET("api/notifications")
    suspend fun notifications(@Header("Authorization") token: String): NotificationResponse

    @GET("api/bus-notifications")
    suspend fun busNotifications(@Header("Authorization") token: String): NotificationResponse

    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("api/admin/signin")
    suspend fun adminLogin(@Body request: AdminLoginRequest): Response<AdminLoginResponse>


    @GET("api/reach-times/{routeId}")
    suspend fun getReachTimes(
        @Path("routeId") routeId: Int,
        @Header("Authorization") token: String
    ): DateLogResponse

    @POST("api/driver-qr/exchange")
    suspend fun exchangeQr(@Body body: QrExchangeRequest): Response<DriverLoginResponse>


}

//api/bus/replacement/3