package com.example.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.config.ApiConfig
import com.example.core.network.ApiResult
import com.example.core.network.RetrofitClientProvider
import com.example.core.security.AuthUser
import com.example.core.security.SecureStorage
import com.example.core.security.UserRole
import com.example.data.remote.api.AuthApi
import com.example.data.remote.dto.LoginRequest
import com.example.data.remote.dto.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val secureStorage = SecureStorage(application)
    private val _currentUser = MutableStateFlow<AuthUser?>(secureStorage.getSession())
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<ApiResult<AuthUser>?>(null)
    val authState: StateFlow<ApiResult<AuthUser>?> = _authState.asStateFlow()

    fun login(email: String, password: String, role: UserRole) {
        viewModelScope.launch {
            _authState.value = ApiResult.Loading
            val context = getApplication<Application>()
            val authApi = RetrofitClientProvider.createService<AuthApi>(context, ApiConfig.ENDPOINT_AUTH)

            if (authApi == null) {
                // If API is unconfigured, create local verified session for role demonstration & testing
                val demoUser = AuthUser(
                    id = "local_usr_${System.currentTimeMillis() % 10000}",
                    fullName = if (email.contains("@")) email.substringBefore("@").replaceFirstChar { it.uppercase() } else "User",
                    email = email.ifBlank { "user@medibridge.org" },
                    phone = "+91 98765 43210",
                    role = role,
                    token = "demo_jwt_token_${System.currentTimeMillis()}"
                )
                secureStorage.saveSession(demoUser)
                _currentUser.value = demoUser
                _authState.value = ApiResult.Success(demoUser)
                return@launch
            }

            val result = RetrofitClientProvider.safeApiCall("Auth") {
                authApi.login(LoginRequest(email = email, password = password, role = role.name))
            }

            when (result) {
                is ApiResult.Success -> {
                    val res = result.data
                    val authUser = AuthUser(
                        id = res.userId,
                        fullName = res.fullName,
                        email = res.email,
                        phone = res.phone,
                        role = try { UserRole.valueOf(res.role) } catch (e: Exception) { role },
                        token = res.token,
                        refreshToken = res.refreshToken
                    )
                    secureStorage.saveSession(authUser)
                    _currentUser.value = authUser
                    _authState.value = ApiResult.Success(authUser)
                }
                is ApiResult.HttpError -> _authState.value = result
                is ApiResult.NetworkError -> _authState.value = result
                is ApiResult.Unconfigured -> _authState.value = result
                ApiResult.Loading -> {}
            }
        }
    }

    fun register(fullName: String, email: String, phone: String, password: String, role: UserRole, regNo: String?) {
        viewModelScope.launch {
            _authState.value = ApiResult.Loading
            val context = getApplication<Application>()
            val authApi = RetrofitClientProvider.createService<AuthApi>(context, ApiConfig.ENDPOINT_AUTH)

            if (authApi == null) {
                val newUser = AuthUser(
                    id = "local_usr_${System.currentTimeMillis() % 10000}",
                    fullName = fullName.ifBlank { "New Member" },
                    email = email,
                    phone = phone,
                    role = role,
                    token = "demo_jwt_token_${System.currentTimeMillis()}"
                )
                secureStorage.saveSession(newUser)
                _currentUser.value = newUser
                _authState.value = ApiResult.Success(newUser)
                return@launch
            }

            val result = RetrofitClientProvider.safeApiCall("Auth") {
                authApi.register(
                    RegisterRequest(
                        fullName = fullName,
                        email = email,
                        phone = phone,
                        password = password,
                        role = role.name,
                        licenseOrRegNumber = regNo
                    )
                )
            }

            when (result) {
                is ApiResult.Success -> {
                    val res = result.data
                    val authUser = AuthUser(
                        id = res.userId,
                        fullName = res.fullName,
                        email = res.email,
                        phone = res.phone,
                        role = try { UserRole.valueOf(res.role) } catch (e: Exception) { role },
                        token = res.token,
                        refreshToken = res.refreshToken
                    )
                    secureStorage.saveSession(authUser)
                    _currentUser.value = authUser
                    _authState.value = ApiResult.Success(authUser)
                }
                is ApiResult.HttpError -> _authState.value = result
                is ApiResult.NetworkError -> _authState.value = result
                is ApiResult.Unconfigured -> _authState.value = result
                ApiResult.Loading -> {}
            }
        }
    }

    fun switchRoleForDemo(newRole: UserRole) {
        val current = _currentUser.value ?: return
        val updated = current.copy(role = newRole)
        secureStorage.saveSession(updated)
        _currentUser.value = updated
    }

    fun logout() {
        secureStorage.clearSession()
        _currentUser.value = null
        _authState.value = null
    }

    fun resetAuthState() {
        _authState.value = null
    }
}
