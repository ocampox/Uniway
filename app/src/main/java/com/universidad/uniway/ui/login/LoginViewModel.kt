package com.universidad.uniway.ui.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.uniway.data.User
import com.universidad.uniway.data.UserRepository
import com.universidad.uniway.network.ApiRepository
import com.universidad.uniway.network.ApiResult
import com.universidad.uniway.network.TokenManager
import kotlinx.coroutines.launch

class LoginViewModel(private val context: Context) : ViewModel() {

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val userRepository = UserRepository(context)
    private val apiRepository = ApiRepository()
    private val tokenManager = TokenManager(context)

    fun loginUser(email: String, password: String, rememberMe: Boolean) {
        _isLoading.value = true

        // Concatenar el dominio institucional al email
        val fullEmail = if (email.contains("@")) {
            email // Si ya tiene @, usar tal como está
        } else {
            "$email@pascualbravo.edu.co"
        }

        // Validaciones
        when {
            email.isBlank() || password.isBlank() -> {
                _loginResult.value = LoginResult.Error("Por favor completa todos los campos")
                _isLoading.value = false
                return
            }
            !isValidEmail(fullEmail) -> {
                _loginResult.value = LoginResult.Error("Correo electrónico inválido")
                _isLoading.value = false
                return
            }
        }

        // Intentar login con API
        viewModelScope.launch {
            when (val result = apiRepository.login(fullEmail, password)) {
                is ApiResult.Success -> {
                    // Login exitoso con API
                    val authResponse = result.data
                    
                    // Guardar token y datos del usuario
                    tokenManager.saveAuthData(authResponse)
                    tokenManager.setRememberMe(rememberMe)
                    
                    // Convertir y guardar usuario localmente
                    val user = apiRepository.convertToUser(authResponse.user)
                    userRepository.saveUser(user)
                    userRepository.setCurrentUser(user)
                    
                    _loginResult.value = LoginResult.Success(user)
                }
                is ApiResult.Error -> {
                    // Error en API, intentar login local como fallback
                    loginLocalFallback(fullEmail, password, result.message)
                }
                is ApiResult.Loading -> {
                    // Ya está manejado por _isLoading
                }
            }
            _isLoading.value = false
        }
    }
    
    private fun loginLocalFallback(email: String, password: String, apiError: String) {
        // Buscar usuario en el repositorio local
        val user = userRepository.getUserByEmail(email)
        
        if (user != null && user.password == password) {
            // Usuario encontrado localmente
            userRepository.setCurrentUser(user)
            _loginResult.value = LoginResult.Success(user)
        } else if (user != null) {
            // Usuario existe pero contraseña incorrecta
            _loginResult.value = LoginResult.Error("Correo electrónico o contraseña incorrectos")
        } else {
            // Usuario no existe localmente, mostrar error de API si es de credenciales
            val errorMessage = if (apiError.contains("Credenciales") || apiError.contains("401") || apiError.contains("400")) {
                "Correo electrónico o contraseña incorrectos"
            } else {
                "Error de conexión: $apiError"
            }
            _loginResult.value = LoginResult.Error(errorMessage)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        // Validar que sea un email válido y que termine con @pascualbravo.edu.co
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && 
               email.endsWith("@pascualbravo.edu.co")
    }
}

sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

