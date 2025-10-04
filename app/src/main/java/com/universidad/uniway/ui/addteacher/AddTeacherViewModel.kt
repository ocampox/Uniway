package com.universidad.uniway.ui.addteacher

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.uniway.network.ApiRepository
import com.universidad.uniway.network.ApiResult
import com.universidad.uniway.network.TokenManager
import kotlinx.coroutines.launch

class AddTeacherViewModel(private val context: Context) : ViewModel() {

    private val apiRepository = ApiRepository()
    private val tokenManager = TokenManager(context)

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun addTeacherRecommendation(teacherName: String, subject: String, semester: String, reference: String, rating: Int) {
        val userId = tokenManager.getUserId()
        if (userId == null) {
            _errorMessage.value = "Usuario no encontrado. Por favor, inicia sesión nuevamente."
            return
        }

        _isLoading.value = true
        _errorMessage.value = ""
        _successMessage.value = ""

        viewModelScope.launch {
            android.util.Log.d("AddTeacherViewModel", "Publicando recomendación:")
            android.util.Log.d("AddTeacherViewModel", "  Profesor: $teacherName")
            android.util.Log.d("AddTeacherViewModel", "  Materia: $subject")
            android.util.Log.d("AddTeacherViewModel", "  Semestre: $semester")
            android.util.Log.d("AddTeacherViewModel", "  Rating: $rating estrellas")
            android.util.Log.d("AddTeacherViewModel", "  Referencia: ${reference.take(50)}...")
            
            // Usar el nuevo sistema de recomendaciones con rating
            when (val result = apiRepository.createTeacherRecommendationWithRating(
                studentId = userId,
                teacherName = teacherName,
                subject = subject,
                semester = semester,
                year = extractYearFromSemester(semester),
                reference = reference,
                rating = rating
            )) {
                is ApiResult.Success -> {
                    android.util.Log.d("AddTeacherViewModel", "✅ Recomendación publicada exitosamente")
                    _successMessage.value = "Recomendación de '$teacherName' publicada exitosamente"
                }
                is ApiResult.Error -> {
                    android.util.Log.e("AddTeacherViewModel", "❌ Error: ${result.message}")
                    _errorMessage.value = "Error al publicar recomendación: ${result.message}"
                }
                is ApiResult.Loading -> {
                    // Loading state already set
                }
            }
            _isLoading.value = false
        }
    }

    private fun generateEmailFromName(name: String): String {
        // Generar un email basado en el nombre del profesor
        val cleanName = name.lowercase()
            .trim()
            .replace(" ", ".")
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("ñ", "n")
            .replace("ü", "u")
            .replace("[^a-z.]".toRegex(), "") // Remover caracteres especiales
        
        return "$cleanName@pascualbravo.edu.co"
    }

    private fun extractYearFromSemester(semester: String): Int {
        // Extraer año del formato "2024-1" o "2024-2"
        return try {
            semester.split("-")[0].toInt()
        } catch (e: Exception) {
            java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        }
    }
}
