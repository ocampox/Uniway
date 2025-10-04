package com.universidad.uniway.ui.teacher

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.uniway.network.ApiRepository
import com.universidad.uniway.network.ApiResult
import com.universidad.uniway.network.TokenManager
import kotlinx.coroutines.launch

class TeacherMainViewModel(private val context: Context) : ViewModel() {

    private val apiRepository = ApiRepository()
    private val tokenManager = TokenManager(context)

    private val _myRecommendationsCount = MutableLiveData<Int>()
    val myRecommendationsCount: LiveData<Int> = _myRecommendationsCount

    private val _totalRecommendationsCount = MutableLiveData<Int>()
    val totalRecommendationsCount: LiveData<Int> = _totalRecommendationsCount

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun loadStatistics() {
        val userId = tokenManager.getUserId()
        if (userId == null) {
            _errorMessage.value = "Usuario no encontrado"
            return
        }

        viewModelScope.launch {
            // Cargar estadísticas de recomendaciones
            when (val result = apiRepository.getRecommendationStats(userId)) {
                is ApiResult.Success -> {
                    _myRecommendationsCount.value = result.data.myRecommendationsCount
                    _totalRecommendationsCount.value = result.data.totalRecommendationsCount
                }
                is ApiResult.Error -> {
                    android.util.Log.e("TeacherMainViewModel", "Error loading stats: ${result.message}")
                    _myRecommendationsCount.value = 0
                    _totalRecommendationsCount.value = 0
                }
                is ApiResult.Loading -> {
                    // Loading state
                }
            }


        }
    }
}