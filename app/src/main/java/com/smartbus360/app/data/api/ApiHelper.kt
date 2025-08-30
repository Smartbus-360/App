package com.smartbus360.app.data.api

import com.smartbus360.app.data.model.request.BusReachedStoppageRequest
import com.smartbus360.app.data.model.request.LoginRequest
import com.smartbus360.app.data.model.request.LoginStudentRequest
import com.smartbus360.app.data.model.request.MarkFinalStopRequest
import retrofit2.http.Body
import com.smartbus360.app.data.model.request.QrExchangeRequest

class ApiHelper (private val apiService: ApiService)  {

    suspend fun getDrivers() = apiService.getDrivers()
    suspend fun login(@Body data: LoginRequest)= apiService.login(data)
    suspend fun loginStudent(@Body data: LoginStudentRequest)= apiService.loginStudent(data)
    suspend fun getDriverDetail(userId: Int, authToken: String)= apiService.getDriverDetail(userId, authToken)
    suspend fun getUserDetail(userId: Int, authToken: String)= apiService.getUserDetail(userId, authToken)
    suspend fun getUserDetail2(userId: Int, authToken: String)= apiService.getUserDetail2(userId, authToken)

    suspend fun getDriverDetail2(userId: Int, authToken: String)= apiService.getDriverDetail2(userId, authToken)
    suspend fun getDriverDetailNew(userId: Int, authToken: String)= apiService.getDriverDetailNew(userId, authToken)

    suspend fun busReachedStoppage( authToken: String, data: BusReachedStoppageRequest)= apiService.busReachedStoppage( authToken,data)

    suspend fun getBusReplacedStatus(busId: Int, authToken: String)= apiService.getBusReplacedStatus( busId,authToken)


    suspend fun markFinalStop( authToken: String, data: MarkFinalStopRequest)= apiService.markFinalStop( authToken,data)

    suspend fun advertisementBanner() = apiService.advertisementBanner()

    suspend fun notifications( authToken: String)= apiService.notifications( authToken)
    suspend fun busNotifications( authToken: String)= apiService.busNotifications( authToken)

    suspend fun exchangeQr(body: QrExchangeRequest) = apiService.exchangeQr(body)


    suspend fun getReachTimes(routeId: Int, authToken: String)= apiService.getReachTimes( routeId,authToken)
}