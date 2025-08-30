package com.smartbus360.app.viewModels

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbus360.app.data.api.ApiHelper
import com.smartbus360.app.data.model.request.LoginRequest
import com.smartbus360.app.data.model.request.LoginStudentRequest
import com.smartbus360.app.data.model.response.DriverLoginResponse
import com.smartbus360.app.data.model.response.StudentLoginResponse
import com.smartbus360.app.data.network.RetrofitBuilder.apiService
import com.smartbus360.app.data.repository.MainRepository
import com.smartbus360.app.data.repository.PreferencesRepository
import com.smartbus360.app.utility.NetworkMonitor
import com.smartbus360.app.utility.isInternetAvailable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.HttpException
import java.io.IOException
import com.smartbus360.app.data.model.request.AdminLoginRequest
import com.smartbus360.app.data.model.request.QrExchangeRequest

class LoginViewModel(
    @SuppressLint("StaticFieldLeak") private val context: Context,
    private val repository: PreferencesRepository,
    private val mainRepository: MainRepository = MainRepository(ApiHelper(apiService))

) : ViewModel() , KoinComponent {

    private val _state = MutableStateFlow(DriverLoginResponse())
    val state: StateFlow<DriverLoginResponse>
        get() = _state

    private val _stateStudent = MutableStateFlow(StudentLoginResponse())
    val stateStudent: StateFlow<StudentLoginResponse>
        get() = _stateStudent

    private val _stateException = MutableStateFlow<Exception?>(null)
    val stateException: StateFlow<Exception?>
        get() = _stateException

    private val _stateExceptionStatus = MutableStateFlow(false)
    val stateExceptionStatus: StateFlow<Boolean>
        get() = _stateExceptionStatus

    data class BlockedInfo(val message: String, val until: String? = null)

    private val _blocked = MutableStateFlow<BlockedInfo?>(null)
    val blocked: StateFlow<BlockedInfo?> get() = _blocked

    private val _adminLoginSuccess = MutableStateFlow(false)
    val adminLoginSuccess: StateFlow<Boolean> get() = _adminLoginSuccess

    val userRole = repository.getUserRole()
    val userName = repository.getUserName()
    val userPass = repository.getUserPass()


    init {
        viewModelScope.launch {
            val savedToken = repository.getAuthToken()
            val savedId = repository.getDriverId()
            val savedName = repository.getUserName()
            val savedPass = repository.getUserPass()

            // ✅ Safely check savedId by using a null check
            if (!savedToken.isNullOrBlank() && savedId != null && savedId > 0) {
                _state.value = DriverLoginResponse(driverId = savedId, token = savedToken)
            } else if (repository.isLoggedIn() && !savedName.isNullOrBlank() && !savedPass.isNullOrBlank()) {
                try {

                    val response = mainRepository.login(LoginRequest(savedName, savedPass))
                    _state.value = response
                    repository.setDriverId(response.driverId)
                    repository.setAuthToken(response.token)
                    repository.setLoggedIn(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun login(req: LoginRequest) {
        viewModelScope.launch {
            try {
                val response = mainRepository.login(req)
                _state.value = response
                repository.setAuthToken(response.token)
                repository.setDriverId(response.driverId)
                repository.setLoggedIn(true)
                repository.setIsQrSession(false)
                repository.setUserName(req.email)
                repository.setUserPass(req.password)
            } catch (e: HttpException) {
                // Handle HTTP exceptions with specific status codes
                when (e.code()) {
                    401-> Log.e("Unauthorized", "Authentication failed. Redirect to login.")

                    403 -> Log.e("LOGIN_ERROR", "Access denied (403): ${e.message}")
                    else -> Log.e("LOGIN_ERROR", "HTTP error: ${e.code()} - ${e.message()}")
                }
                _stateException.value = e
                _stateExceptionStatus.value = true
            } catch (e: IOException) {
                // Handle network-related exceptions
                Log.e("LOGIN_ERROR", "Network error: ${e.message}")
                _stateException.value = e
                _stateExceptionStatus.value = true
            } catch (e: Exception) {
                // Handle any other exceptions
                Log.e("LOGIN_ERROR", "Unexpected error: ${e.message}")
                _stateException.value = e
                _stateExceptionStatus.value = true
            }
        }
    }

    fun loginStudent(req: LoginStudentRequest) {
        viewModelScope.launch {
            try {
                val response = mainRepository.loginStudent(req)
                _stateStudent.value = response
                repository.setUserId(response.userId)
                repository.setAuthToken(response.token)
                repository.setLoggedIn(true)
                repository.setUserName(req.email)
                repository.setUserPass(req.password)
            } catch (e: HttpException) {
                // Handle HTTP exceptions with specific status codes
                when (e.code()) {
                    401-> Log.e("Unauthorized", "Authentication failed. Redirect to login.")

                    403 -> Log.e("LOGIN_ERROR", "Access denied (403): ${e.message}")
                    else -> Log.e("LOGIN_ERROR", "HTTP error: ${e.code()} - ${e.message()}")
                }
                _stateException.value = e
                _stateExceptionStatus.value = true
            } catch (e: IOException) {
                // Handle network-related exceptions
                Log.e("LOGIN_ERROR", "Network error: ${e.message}")
                _stateException.value = e
                _stateExceptionStatus.value = true
            } catch (e: Exception) {
                // Handle any other exceptions
                Log.e("LOGIN_ERROR", "Unexpected error: ${e.message}")
                _stateException.value = e
                _stateExceptionStatus.value = true
            }
        }
    }
    fun loginAdmin(request: AdminLoginRequest) {
        viewModelScope.launch {
            try {
                val cleanedRequest = request.copy(
                    email = request.email.trim(),
                    password = request.password.trim()
                )
                val response = apiService.adminLogin(cleanedRequest)
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        repository.setAuthToken(body.accessToken)
                        repository.setUserRole("admin")
                        repository.setUserName(body.user.username)
                        repository.setLoggedIn(true)
                        // ✅ Ensure AdminDashboard can read this
                        val prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("ACCESS_TOKEN", body.accessToken).commit()
                        _adminLoginSuccess.value = true
                        _stateExceptionStatus.value = false
                        Log.e("ADMIN_LOGIN", "Admin login success: ${body.user.username}")
                    }
                } else {
                    Log.e("ADMIN_LOGIN_ERROR", "Failed: HTTP ${response.code()}")
                    _stateExceptionStatus.value = true
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }
    private val _qrLoginSuccess = MutableStateFlow(false)
    val qrLoginSuccess: StateFlow<Boolean> = _qrLoginSuccess

    fun exchangeQrLogin(qrToken: String) {
        viewModelScope.launch {
            try {
                // Call API directly to avoid repository ambiguity
                val resp = apiService.exchangeQr(QrExchangeRequest(qrToken))  // Response<DriverLoginResponse>
                if (resp.isSuccessful) {
                    val body = resp.body() ?: throw IOException("Empty body from QR exchange")
                    // Save session like manual login
                    _state.value = body
                    repository.setAuthToken(body.token)
                    repository.setIsQrSession(true)
                    repository.setDriverId(body.driverId)
                    repository.setUserRole("driver")
                    repository.setLoggedIn(true)

                    _qrLoginSuccess.value = true
                    _stateExceptionStatus.value = false
                    _stateException.value = null
                } else {
                    // Map common server codes to clearer errors
                    when (resp.code()) {
                        400 -> throw HttpException(resp) // "Invalid or expired QR"
                        423 -> throw HttpException(resp) // "Driver is blocked by an active QR session"
                        else -> throw HttpException(resp)
                    }
                }
            } catch (e: Exception) {
                handleException(e)  // sets stateException + stateExceptionStatus
                _qrLoginSuccess.value = false
            }
        }
    }

    fun resetExceptionState() {
        _stateException.value = null
        _stateExceptionStatus.value = false
    }
    private fun handleException(e: Exception) {
        when (e) {
            is HttpException -> {
                if (e.code() == 401) Log.e("Unauthorized", "Authentication failed.")
                else Log.e("LOGIN_ERROR", "HTTP error: ${e.code()} - ${e.message()}")
            }
            is IOException -> Log.e("LOGIN_ERROR", "Network error: ${e.message}")
            else -> Log.e("LOGIN_ERROR", "Unexpected error: ${e.localizedMessage}")
        }
        _stateException.value = e
        _stateExceptionStatus.value = true
    }

    fun resetAdminLoginSuccess() {
        _adminLoginSuccess.value = false
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearSession()
            _state.value = DriverLoginResponse()  // Reset login state
        }
    }

}




