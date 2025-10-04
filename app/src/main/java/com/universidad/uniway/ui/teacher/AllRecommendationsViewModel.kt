package com.universidad.uniway.ui.teacher

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.uniway.data.StudentTeacher
import com.universidad.uniway.data.TeacherRecommendation
import com.universidad.uniway.network.ApiRepository
import com.universidad.uniway.network.ApiResult
import com.universidad.uniway.network.TokenManager
import kotlinx.coroutines.launch

class AllRecommendationsViewModel(private val context: Context) : ViewModel() {

    private val apiRepository = ApiRepository()
    private val tokenManager = TokenManager(context)

    private val _allRecommendations = MutableLiveData<List<TeacherRecommendation>>()
    val allRecommendations: LiveData<List<TeacherRecommendation>> = _allRecommendations

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    fun loadAllRecommendations() {
        _isLoading.value = true
        _errorMessage.value = ""

        viewModelScope.launch {
            android.util.Log.d("AllRecommendationsViewModel", "Cargando todas las recomendaciones")
            
            val userId = tokenManager.getUserId()
            android.util.Log.d("AllRecommendationsViewModel", "User ID from TokenManager: $userId")
            
            // Cargar TODAS las recomendaciones (sin filtrar por userId)
            android.util.Log.d("AllRecommendationsViewModel", "Cargando TODAS las recomendaciones...")
            when (val result = apiRepository.getAllRecommendations(null, null)) {
                    is ApiResult.Success -> {
                        android.util.Log.d("AllRecommendationsViewModel", "✅ Recomendaciones cargadas: ${result.data.size}")
                        
                        // Log detallado de cada recomendación para debugging
                        result.data.forEachIndexed { index, rec ->
                            android.util.Log.d("AllRecommendationsViewModel", "Recomendación $index:")
                            android.util.Log.d("AllRecommendationsViewModel", "  ID: ${rec.id}")
                            android.util.Log.d("AllRecommendationsViewModel", "  Estudiante: ${rec.studentName} (ID: ${rec.studentId})")
                            android.util.Log.d("AllRecommendationsViewModel", "  Profesor: ${rec.teacherName}")
                            android.util.Log.d("AllRecommendationsViewModel", "  Materia: ${rec.subject}")
                            android.util.Log.d("AllRecommendationsViewModel", "  Rating: ${rec.rating}")
                            android.util.Log.d("AllRecommendationsViewModel", "  Usuario actual: $userId")
                        }
                        
                        // Mostrar todas las recomendaciones de todos los usuarios
                        android.util.Log.d("AllRecommendationsViewModel", "✅ Todas las recomendaciones cargadas: ${result.data.size}")
                        _allRecommendations.value = result.data
                    }
                    is ApiResult.Error -> {
                        android.util.Log.e("AllRecommendationsViewModel", "❌ Error: ${result.message}")
                        _errorMessage.value = "Error al cargar recomendaciones: ${result.message}"
                        _allRecommendations.value = emptyList()
                    }
                    is ApiResult.Loading -> {
                        // Loading state already set
                    }
                }
            
            _isLoading.value = false
        }
    }
    
    fun forceReloadRecommendations() {
        android.util.Log.d("AllRecommendationsViewModel", "=== FORZANDO RECARGA DE RECOMENDACIONES ===")
        loadAllRecommendations()
    }

    fun likeRecommendation(recommendation: TeacherRecommendation) {
        val userId = tokenManager.getUserId()
        if (userId == null) {
            _errorMessage.value = "Usuario no encontrado"
            return
        }

        viewModelScope.launch {
            android.util.Log.d("AllRecommendationsViewModel", "Dando like a recomendación: ${recommendation.id}")
            
            when (val result = apiRepository.likeRecommendation(recommendation.id, userId)) {
                is ApiResult.Success -> {
                    android.util.Log.d("AllRecommendationsViewModel", "✅ Like exitoso")
                    _successMessage.value = "Like agregado a la recomendación"
                    // Recargar las recomendaciones para actualizar los contadores
                    loadAllRecommendations()
                }
                is ApiResult.Error -> {
                    android.util.Log.e("AllRecommendationsViewModel", "❌ Error en like: ${result.message}")
                    val errorMessage = when {
                        result.message.contains("propias recomendaciones") -> "No puedes dar like a tus propias recomendaciones"
                        result.message.contains("ya has reaccionado") -> "Ya has reaccionado a esta recomendación"
                        else -> "Error al dar like: ${result.message}"
                    }
                    _errorMessage.value = errorMessage
                }
                is ApiResult.Loading -> {
                    // Loading state
                }
            }
        }
    }

    fun dislikeRecommendation(recommendation: TeacherRecommendation) {
        val userId = tokenManager.getUserId()
        if (userId == null) {
            _errorMessage.value = "Usuario no encontrado"
            return
        }

        viewModelScope.launch {
            android.util.Log.d("AllRecommendationsViewModel", "Dando dislike a recomendación: ${recommendation.id}")
            
            when (val result = apiRepository.dislikeRecommendation(recommendation.id, userId)) {
                is ApiResult.Success -> {
                    android.util.Log.d("AllRecommendationsViewModel", "✅ Dislike exitoso")
                    _successMessage.value = "Dislike agregado a la recomendación"
                    // Recargar las recomendaciones para actualizar los contadores
                    loadAllRecommendations()
                }
                is ApiResult.Error -> {
                    android.util.Log.e("AllRecommendationsViewModel", "❌ Error en dislike: ${result.message}")
                    val errorMessage = when {
                        result.message.contains("propias recomendaciones") -> "No puedes dar dislike a tus propias recomendaciones"
                        result.message.contains("ya has reaccionado") -> "Ya has reaccionado a esta recomendación"
                        else -> "Error al dar dislike: ${result.message}"
                    }
                    _errorMessage.value = errorMessage
                }
                is ApiResult.Loading -> {
                    // Loading state
                }
            }
        }
    }

    fun deleteRecommendation(recommendation: TeacherRecommendation) {
        val userId = tokenManager.getUserId()
        if (userId == null) {
            _errorMessage.value = "Usuario no encontrado"
            return
        }

        viewModelScope.launch {
            android.util.Log.d("AllRecommendationsViewModel", "Eliminando recomendación: ${recommendation.id}")
            
            when (val result = apiRepository.deleteRecommendation(recommendation.id, userId)) {
                is ApiResult.Success -> {
                    android.util.Log.d("AllRecommendationsViewModel", "✅ Recomendación eliminada exitosamente")
                    _successMessage.value = "Recomendación eliminada exitosamente"
                    // Recargar las recomendaciones para actualizar la lista
                    loadAllRecommendations()
                }
                is ApiResult.Error -> {
                    android.util.Log.e("AllRecommendationsViewModel", "❌ Error al eliminar: ${result.message}")
                    val errorMessage = when {
                        result.message.contains("permisos") -> "No tienes permisos para eliminar esta recomendación"
                        result.message.contains("no encontrada") -> "La recomendación no fue encontrada"
                        else -> "Error al eliminar recomendación: ${result.message}"
                    }
                    _errorMessage.value = errorMessage
                }
                is ApiResult.Loading -> {
                    // Loading state
                }
            }
        }
    }
}