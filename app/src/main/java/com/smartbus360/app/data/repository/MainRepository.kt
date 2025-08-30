package com.smartbus360.app.data.repository

import com.smartbus360.app.data.api.ApiHelper
import com.smartbus360.app.data.model.request.BusReachedStoppageRequest
import com.smartbus360.app.data.model.request.LoginRequest
import com.smartbus360.app.data.model.request.LoginStudentRequest
import com.smartbus360.app.data.model.request.MarkFinalStopRequest
import retrofit2.http.Body
import com.smartbus360.app.data.model.request.QrExchangeRequest
import com.smartbus360.app.data.model.response.DriverLoginResponse

class MainRepository (private val apiHelper: ApiHelper) {
//    suspend fun getDrivers() = apiHelper.getDrivers()
    suspend fun login(@Body data: LoginRequest) = apiHelper.login(data)
    suspend fun loginStudent(@Body data: LoginStudentRequest) = apiHelper.loginStudent(data)
//    suspend fun getDriverDetail(userId: Int, authToken: String) = apiHelper.getDriverDetail(userId, authToken)
//    suspend fun getUserDetail(userId: Int, authToken: String) = apiHelper.getUserDetail(userId, authToken)
    suspend fun getUserDetail2(userId: Int, authToken: String) = apiHelper.getUserDetail2(userId, authToken)
//    suspend fun getDriverDetail2(userId: Int, authToken: String) = apiHelper.getDriverDetail2(userId, authToken)
    suspend fun getDriverDetailNew(userId: Int, authToken: String) = apiHelper.getDriverDetailNew(userId, authToken)
    suspend fun busReachedStoppage( authToken: String, data: BusReachedStoppageRequest)= apiHelper.busReachedStoppage( authToken,data)

    suspend fun getBusReplacedStatus(busId: Int, authToken: String)= apiHelper.getBusReplacedStatus(busId,authToken)

    suspend fun markFinalStop( authToken: String, data: MarkFinalStopRequest)= apiHelper.markFinalStop( authToken,data)

    suspend fun advertisementBanner()= apiHelper.advertisementBanner()

    suspend fun notifications( authToken: String)= apiHelper.notifications( authToken)

    suspend fun getReachTimes(routeId: Int, authToken: String)= apiHelper.getReachTimes( routeId,authToken)
    suspend fun busNotifications( authToken: String)= apiHelper.busNotifications( authToken)

    suspend fun exchangeQr(body: QrExchangeRequest): DriverLoginResponse {
        val resp = apiHelper.exchangeQr(body)        // <-- api is ApiHelper
        if (!resp.isSuccessful || resp.body() == null) {
            throw RuntimeException("QR exchange failed: ${resp.code()} ${resp.message()}")
        }
        return resp.body()!!
    }



}