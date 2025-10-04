package com.universidad.uniway.ui.forgotpassword

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.uniway.network.ApiRepository
import com.universidad.uniway.network.ApiResult
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(private val context: Context) : ViewModel() {

    private val _forgotPasswordResult = MutableLiveData<ForgotPasswordResult>()
    val forgotPasswordResult: LiveData<ForgotPasswordResult> = _forgotPasswordResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val apiRepository = ApiRepository()

    fun sendResetCode(email: String) {
        _isLoading.value = true

        // Concatenar el dominio institucional al email
        val fullEmail = if (email.contains("@")) {
            email
        } else {
            "$email@pascualbravo.edu.co"
        }

        // Validaciones
        when {
            email.isBlank() -> {
                _forgotPasswordResult.value = ForgotPasswordResult.Error("Por favor completa el campo de correo")
                _isLoading.value = false
                return
            }
            !isValidEmail(fullEmail) -> {
                _forgotPasswordResult.value = ForgotPasswordResult.Error("Correo electrónico inválido")
                _isLoading.value = false
                return
            }
        }

        // Enviar código de reset
        viewModelScope.launch {
            when (val result = apiRepository.sendPasswordResetCode(fullEmail)) {
                is ApiResult.Success -> {
                    _forgotPasswordResult.value = ForgotPasswordResult.Success(fullEmail, result.data.message)
                }
                is ApiResult.Error -> {
                    _forgotPasswordResult.value = ForgotPasswordResult.Error(result.message)
                }
                is ApiResult.Loading -> {
                    // Ya está manejado por _isLoading
                }
            }
            _isLoading.value = false
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

sealed class ForgotPasswordResult {
    data class Success(val email: String, val message: String) : ForgotPasswordResult()
    data class Error(val message: String) : ForgotPasswordResult()
}
