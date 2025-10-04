package com.universidad.uniway.ui.teacherlist

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

class TeacherListViewModel(private val context: Context) : ViewModel() {

    private val apiRepository = ApiRepository()
    private val tokenManager = TokenManager(context)

    private val _studentTeachers = MutableLiveData<List<TeacherRecommendation>>()
    val studentTeachers: LiveData<List<TeacherRecommendation>> = _studentTeachers

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun loadStudentTeachers() {
        val userId = tokenManager.getUserId()
        android.util.Log.d("TeacherListViewModel", "Loading recommendations for userId: $userId")
        
        if (userId == null) {
            android.util.Log.e("TeacherListViewModel", "UserId is null")
            _errorMessage.value = "Usuario no encontrado"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            android.util.Log.d("TeacherListViewModel", "Making API call to getUserRecommendations")
            when (val result = apiRepository.getUserRecommendations(userId)) {
                is ApiResult.Success -> {
                    android.util.Log.d("TeacherListViewModel", "API Success: ${result.data.size} recommendations found")
                    result.data.forEach { recommendation ->
                        android.util.Log.d("TeacherListViewModel", "Recommendation: ${recommendation.teacherName} - ${recommendation.subject}")
                    }
                    _studentTeachers.value = result.data
                    _errorMessage.value = ""
                }
                is ApiResult.Error -> {
                    android.util.Log.e("TeacherListViewModel", "API Error: ${result.message}")
                    _errorMessage.value = result.message
                    _studentTeachers.value = emptyList()
                }
                is ApiResult.Loading -> {
                    android.util.Log.d("TeacherListViewModel", "API Loading...")
                }
            }
            _isLoading.value = false
        }
    }

    fun removeTeacher(recommendation: TeacherRecommendation) {
        val userId = tokenManager.getUserId()
        if (userId == null) {
            _errorMessage.value = "Usuario no encontrado"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            when (val result = apiRepository.deleteRecommendation(recommendation.id, userId)) {
                is ApiResult.Success -> {
                    // Recargar la lista después de remover
                    loadStudentTeachers()
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
                is ApiResult.Loading -> {
                    // Loading state already set
                }
            }
            _isLoading.value = false
        }
    }
}
