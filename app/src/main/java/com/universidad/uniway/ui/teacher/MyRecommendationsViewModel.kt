package com.universidad.uniway.ui.teacher

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.uniway.data.TeacherRecommendation
import com.universidad.uniway.network.ApiRepository
import com.universidad.uniway.network.ApiResult
import com.universidad.uniway.network.TokenManager
import kotlinx.coroutines.launch

class MyRecommendationsViewModel(private val context: Context) : ViewModel() {

    private val apiRepository = ApiRepository()
    private val tokenManager = TokenManager(context)

    private val _myRecommendations = MutableLiveData<List<TeacherRecommendation>>()
    val myRecommendations: LiveData<List<TeacherRecommendation>> = _myRecommendations

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    fun loadMyRecommendations() {
        val userId = tokenManager.getUserId()
        if (userId == null) {
            _errorMessage.value = "Usuario no encontrado. Por favor, inicia sesión nuevamente."
            return
        }

        _isLoading.value = true
        _errorMessage.value = ""

        viewModelScope.launch {
            android.util.Log.d("MyRecommendationsViewModel", "Cargando mis recomendaciones para usuario: $userId")
            
            when (val result = apiRepository.getUserRecommendations(userId)) {
                is ApiResult.Success -> {
                    android.util.Log.d("MyRecommendationsViewModel", "✅ Recomendaciones cargadas: ${result.data.size}")
                    _myRecommendations.value = result.data
                }
                is ApiResult.Error -> {
                    android.util.Log.e("MyRecommendationsViewModel", "❌ Error: ${result.message}")
                    _errorMessage.value = "Error al cargar recomendaciones: ${result.message}"
                    _myRecommendations.value = emptyList()
                }
                is ApiResult.Loading -> {
                    // Loading state already set
                }
            }
            _isLoading.value = false
        }
    }

    fun removeRecommendation(recommendation: TeacherRecommendation) {
        val userId = tokenManager.getUserId()
        if (userId == null) {
            _errorMessage.value = "Usuario no encontrado"
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            android.util.Log.d("MyRecommendationsViewModel", "Eliminando recomendación: ${recommendation.id}")
            
            when (val result = apiRepository.deleteRecommendation(recommendation.id, userId)) {
                is ApiResult.Success -> {
                    android.util.Log.d("MyRecommendationsViewModel", "✅ Recomendación eliminada")
                    _successMessage.value = "Recomendación eliminada exitosamente"
                    // Recargar la lista
                    loadMyRecommendations()
                }
                is ApiResult.Error -> {
                    android.util.Log.e("MyRecommendationsViewModel", "❌ Error: ${result.message}")
                    _errorMessage.value = "Error al eliminar recomendación: ${result.message}"
                }
                is ApiResult.Loading -> {
                    // Loading state already set
                }
            }
            _isLoading.value = false
        }
    }
}