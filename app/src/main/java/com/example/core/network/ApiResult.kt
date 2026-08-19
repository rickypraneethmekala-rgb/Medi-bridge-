package com.example.core.network

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T, val isLive: Boolean = false, val lastUpdated: String? = null) : ApiResult<T>
    data class HttpError(val code: Int, val userMessage: String, val errorBody: String? = null) : ApiResult<Nothing>
    data class NetworkError(val message: String = "No internet connection or network timeout") : ApiResult<Nothing>
    data class Unconfigured(val serviceName: String, val requiredEndpoint: String = "BASE_URL") : ApiResult<Nothing>
    data object Loading : ApiResult<Nothing>
}
