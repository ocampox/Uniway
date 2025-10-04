package com.universidad.uniway.ui.profile

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

class ProfileViewModel(private val context: Context) : ViewModel() {

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    private val _updateResult = MutableLiveData<ProfileUpdateResult>()
    val updateResult: LiveData<ProfileUpdateResult> = _updateResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val userRepository = UserRepository(context)
    private val apiRepository = ApiRepository()
    private val tokenManager = TokenManager(context)

    fun loadUserProfile(user: User) {
        _userProfile.value = user
    }
    
    fun clearOldUserData() {
        // Limpiar datos de usuario anterior
        userRepository.logout()
        android.util.Log.d("ProfileViewModel", "Cleared old user data")
    }
    
    fun loadCurrentUser() {
        android.util.Log.d("ProfileViewModel", "loadCurrentUser() called")
        
        // Obtener email del usuario actualmente logueado
        val currentLoggedEmail = tokenManager.getUserEmail()
        android.util.Log.d("ProfileViewModel", "Current logged email: $currentLoggedEmail")
        
        if (currentLoggedEmail == null) {
            android.util.Log.w("ProfileViewModel", "No logged email found, cannot load profile")
            _userProfile.value = null
            return
        }
        
        // Primero intentar cargar desde API si hay token
        val token = tokenManager.getToken()
        if (token != null) {
            android.util.Log.d("ProfileViewModel", "Token found, loading from API")
            viewModelScope.launch {
                when (val result = apiRepository.getUserProfile(token)) {
                    is ApiResult.Success -> {
                        android.util.Log.d("ProfileViewModel", "API call successful, converting user data")
                        val user = apiRepository.convertToUser(result.data)
                        android.util.Log.d("ProfileViewModel", "Converted user: ${user.fullName} (${user.email})")
                        
                        // Verificar que el usuario de la API coincide con el usuario logueado
                        if (user.email == currentLoggedEmail) {
                            android.util.Log.d("ProfileViewModel", "User email matches logged email, loading profile")
                            _userProfile.value = user
                            // Actualizar usuario local
                            userRepository.saveUser(user)
                            userRepository.setCurrentUser(user)
                            // También actualizar TokenManager
                            tokenManager.saveUserInfo(user.id, user.email, user.fullName, user.role.name)
                        } else {
                            android.util.Log.w("ProfileViewModel", "User email mismatch: API=${user.email}, Logged=${currentLoggedEmail}")
                            // Email no coincide, cargar desde fuentes locales
                            loadUserFromTokenManager()
                        }
                    }
                    is ApiResult.Error -> {
                        android.util.Log.w("ProfileViewModel", "Error cargando perfil desde API: ${result.message}")
                        // Error en API, cargar desde fuentes locales
                        loadUserFromTokenManager()
                    }
                    is ApiResult.Loading -> {
                        android.util.Log.d("ProfileViewModel", "Loading from API...")
                    }
                }
            }
        } else {
            android.util.Log.d("ProfileViewModel", "No token found, loading from local sources")
            // No hay token, cargar desde fuentes locales
            loadUserFromTokenManager()
        }
    }
    
    private fun loadUserFromTokenManager() {
        // Obtener email del usuario actualmente logueado desde TokenManager
        val currentLoggedEmail = tokenManager.getUserEmail()
        android.util.Log.d("ProfileViewModel", "Current logged email: $currentLoggedEmail")
        
        if (currentLoggedEmail == null) {
            android.util.Log.w("ProfileViewModel", "No logged email found in TokenManager")
            _userProfile.value = null
            return
        }
        
        // Buscar usuario por email en UserRepository
        val currentUser = userRepository.getUserByEmail(currentLoggedEmail)
        if (currentUser != null && currentUser.email == currentLoggedEmail) {
            android.util.Log.d("ProfileViewModel", "Loading user from UserRepository by email: ${currentUser.fullName} (${currentUser.email})")
            _userProfile.value = currentUser
            return
        }
        
        // Si no se encuentra en UserRepository, crear usuario básico desde TokenManager
        val userId = tokenManager.getUserId()
        val email = tokenManager.getUserEmail()
        val fullName = tokenManager.getUserName()
        val role = tokenManager.getUserRole()
        
        android.util.Log.d("ProfileViewModel", "TokenManager data - ID: $userId, Email: $email, Name: $fullName, Role: $role")
        
        if (userId != null && email != null && fullName != null && role != null && email == currentLoggedEmail) {
            val user = User(
                id = userId,
                email = email,
                fullName = fullName,
                password = "", // No guardamos la contraseña
                role = try { com.universidad.uniway.data.UserRole.valueOf(role) } catch (e: Exception) { com.universidad.uniway.data.UserRole.STUDENT },
                studentId = "", // Se puede obtener de otros campos si está disponible
                program = "", // Se puede obtener de otros campos si está disponible
                phone = "",
                address = ""
            )
            android.util.Log.d("ProfileViewModel", "Loading user from TokenManager: ${user.fullName} (${user.email})")
            _userProfile.value = user
            // Guardar usuario localmente para futuras referencias
            userRepository.saveUser(user)
            userRepository.setCurrentUser(user)
        } else {
            android.util.Log.w("ProfileViewModel", "No matching user data found for email: $currentLoggedEmail")
            _userProfile.value = null
        }
    }

    fun updateProfile(fullName: String, email: String, phone: String, address: String) {
        _isLoading.value = true

        // Validaciones
        when {
            fullName.isBlank() -> {
                _updateResult.value = ProfileUpdateResult.Error("El nombre completo es obligatorio")
                _isLoading.value = false
                return
            }
            email.isBlank() -> {
                _updateResult.value = ProfileUpdateResult.Error("El correo electrónico es obligatorio")
                _isLoading.value = false
                return
            }
            !isValidEmail(email) -> {
                _updateResult.value = ProfileUpdateResult.Error("Correo electrónico inválido")
                _isLoading.value = false
                return
            }
        }

        val token = tokenManager.getToken()
        val currentUser = _userProfile.value
        
        if (currentUser != null) {
            viewModelScope.launch {
                if (token != null) {
                    // Intentar actualizar perfil en la API
                    when (val result = apiRepository.updateUserProfile(token, fullName, phone, address, currentUser.program)) {
                        is ApiResult.Success -> {
                            // Perfil actualizado exitosamente en la API
                            val updatedUser = apiRepository.convertToUser(result.data.user)
                            _userProfile.value = updatedUser
                            userRepository.saveUser(updatedUser)
                            userRepository.setCurrentUser(updatedUser)
                            _updateResult.value = ProfileUpdateResult.Success(updatedUser)
                        }
                        is ApiResult.Error -> {
                            // Error en API, actualizar localmente
                            updateProfileLocally(currentUser, fullName, email, phone, address)
                        }
                        is ApiResult.Loading -> {
                            // Manejado por el estado de loading
                        }
                    }
                } else {
                    // No hay token, actualizar localmente
                    updateProfileLocally(currentUser, fullName, email, phone, address)
                }
                _isLoading.value = false
            }
        }
    }
    
    private fun updateProfileLocally(currentUser: User, fullName: String, email: String, phone: String, address: String) {
        val updatedUser = currentUser.copy(
            fullName = fullName,
            email = email,
            phone = phone,
            address = address
        )
        
        // Guardar usuario actualizado localmente
        userRepository.saveUser(updatedUser)
        userRepository.setCurrentUser(updatedUser)
        
        _userProfile.value = updatedUser
        _updateResult.value = ProfileUpdateResult.Success(updatedUser)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

sealed class ProfileUpdateResult {
    data class Success(val user: User) : ProfileUpdateResult()
    data class Error(val message: String) : ProfileUpdateResult()
}

