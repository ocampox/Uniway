package com.universidad.uniway.ui.resetpassword

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.uniway.network.ApiRepository
import com.universidad.uniway.network.ApiResult
import kotlinx.coroutines.launch

class ResetPasswordViewModel(private val context: Context) : ViewModel() {

    private val _resetPasswordResult = MutableLiveData<ResetPasswordResult>()
    val resetPasswordResult: LiveData<ResetPasswordResult> = _resetPasswordResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val apiRepository = ApiRepository()

    fun resetPassword(email: String, code: String, newPassword: String) {
        _isLoading.value = true

        // Validaciones
        when {
            email.isBlank() || code.isBlank() || newPassword.isBlank() -> {
                _resetPasswordResult.value = ResetPasswordResult.Error("Por favor completa todos los campos")
                _isLoading.value = false
                return
            }
            newPassword.length < 6 -> {
                _resetPasswordResult.value = ResetPasswordResult.Error("La contraseña debe tener al menos 6 caracteres")
                _isLoading.value = false
                return
            }
        }

        // Restablecer contraseña
        viewModelScope.launch {
            when (val result = apiRepository.resetPassword(email, code, newPassword)) {
                is ApiResult.Success -> {
                    _resetPasswordResult.value = ResetPasswordResult.Success(result.data.message)
                }
                is ApiResult.Error -> {
                    _resetPasswordResult.value = ResetPasswordResult.Error(result.message)
                }
                is ApiResult.Loading -> {
                    // Ya está manejado por _isLoading
                }
            }
            _isLoading.value = false
        }
    }
}

sealed class ResetPasswordResult {
    data class Success(val message: String) : ResetPasswordResult()
    data class Error(val message: String) : ResetPasswordResult()
}
