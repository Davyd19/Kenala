package com.app.kenala.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.kenala.api.RetrofitClient
// IMPORT DTO YANG BARU
import com.app.kenala.data.remote.dto.LoginRequest
import com.app.kenala.data.remote.dto.RegisterRequest
import com.app.kenala.data.remote.dto.ChangePasswordRequest
import com.app.kenala.data.local.AppDatabase
import com.app.kenala.data.local.entities.UserEntity
import com.app.kenala.utils.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

// ... (Sisa kode AuthViewModel sama persis seperti sebelumnya, yang penting import di atas sudah benar)
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val userDao = database.userDao()
    private val dataStoreManager = DataStoreManager(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            val token = dataStoreManager.getToken()
            _isLoggedIn.value = !token.isNullOrEmpty()
            if (!token.isNullOrEmpty()) {
                RetrofitClient.setToken(token)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.apiService.login(
                    LoginRequest(email, password)
                )
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    dataStoreManager.saveAuthData(
                        token = authResponse.token,
                        userId = authResponse.user.id,
                        userName = authResponse.user.name,
                        userEmail = authResponse.user.email
                    )
                    RetrofitClient.setToken(authResponse.token)
                    val userEntity = UserEntity(
                        id = authResponse.user.id,
                        name = authResponse.user.name,
                        email = authResponse.user.email,
                        phone = authResponse.user.phone,
                        bio = authResponse.user.bio,
                        profile_image_url = authResponse.user.profile_image_url,
                        level = authResponse.user.level,
                        total_missions = authResponse.user.total_missions,
                        total_distance = authResponse.user.total_distance,
                        current_streak = authResponse.user.current_streak,
                        longest_streak = authResponse.user.longest_streak,
                        total_active_days = authResponse.user.total_active_days
                    )
                    userDao.insertUser(userEntity)
                    _authState.value = AuthState.Success("Login berhasil!")
                    _isLoggedIn.value = true
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Email atau password salah"
                        404 -> "Akun tidak ditemukan"
                        else -> "Login gagal: ${response.message()}"
                    }
                    _authState.value = AuthState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    "Terjadi kesalahan: ${e.message ?: "Tidak dapat terhubung ke server"}"
                )
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.apiService.register(
                    RegisterRequest(name, email, password)
                )
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    dataStoreManager.saveAuthData(
                        token = authResponse.token,
                        userId = authResponse.user.id,
                        userName = authResponse.user.name,
                        userEmail = authResponse.user.email
                    )
                    RetrofitClient.setToken(authResponse.token)
                    val userEntity = UserEntity(
                        id = authResponse.user.id,
                        name = authResponse.user.name,
                        email = authResponse.user.email,
                        phone = authResponse.user.phone,
                        bio = authResponse.user.bio,
                        profile_image_url = authResponse.user.profile_image_url,
                        level = authResponse.user.level,
                        total_missions = authResponse.user.total_missions,
                        total_distance = authResponse.user.total_distance,
                        current_streak = authResponse.user.current_streak,
                        longest_streak = authResponse.user.longest_streak,
                        total_active_days = authResponse.user.total_active_days
                    )
                    userDao.insertUser(userEntity)
                    _authState.value = AuthState.Success("Registrasi berhasil!")
                    _isLoggedIn.value = true
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Email sudah terdaftar"
                        else -> "Registrasi gagal: ${response.message()}"
                    }
                    _authState.value = AuthState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    "Terjadi kesalahan: ${e.message ?: "Tidak dapat terhubung ke server"}"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            dataStoreManager.clearAuthData()
            RetrofitClient.setToken(null)
            userDao.deleteUser()
            _isLoggedIn.value = false
            _authState.value = AuthState.Idle
        }
    }

    fun changePassword(currentPass: String, newPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.changePassword(
                    ChangePasswordRequest(currentPass, newPass)
                )
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = try {
                        if (errorBody != null) {
                            val jsonObject = JSONObject(errorBody)
                            jsonObject.getString("error")
                        } else {
                            "Gagal mengubah password"
                        }
                    } catch (e: Exception) {
                        "Terjadi kesalahan pada server"
                    }
                    onError(message)
                }
            } catch (e: Exception) {
                onError("Tidak dapat terhubung ke server")
            }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}