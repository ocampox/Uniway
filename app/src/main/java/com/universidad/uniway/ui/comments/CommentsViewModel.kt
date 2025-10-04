package com.universidad.uniway.ui.comments

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.uniway.data.Comment
import com.universidad.uniway.data.Post
import com.universidad.uniway.data.UserRepository
import com.universidad.uniway.data.UserRole
import com.universidad.uniway.network.ApiRepository
import com.universidad.uniway.network.ApiResult
import com.universidad.uniway.network.TokenManager
import kotlinx.coroutines.launch
import java.util.*

/**
 * CommentsViewModel - ViewModel para la gestión del sistema de comentarios
 * 
 * Esta clase maneja toda la lógica de presentación de los comentarios, incluyendo:
 * - Carga de comentarios desde la API con fallback a datos mock
 * - Creación de nuevos comentarios con sincronización al backend
 * - Manejo de estados de carga y errores específicos de comentarios
 * - Actualización automática de la UI cuando se agregan comentarios
 * - Validación de usuarios antes de crear comentarios
 * 
 * Características del sistema de comentarios:
 * - Los comentarios se guardan permanentemente en la base de datos
 * - Se actualizan automáticamente los contadores en los posts
 * - Fallback local si hay problemas de conexión
 * - Logs detallados para debugging
 * 
 * Arquitectura MVVM:
 * - Expone LiveData observables para la UI de comentarios
 * - Maneja llamadas asíncronas con corrutinas
 * - Separa la lógica de comentarios de la UI
 * - Proporciona feedback inmediato al usuario
 */
class CommentsViewModel(private val context: Context) : ViewModel() {

    // ==================== LIVEDATA OBSERVABLES ====================
    
    /** Lista de comentarios del post actual - observable por la UI */
    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> = _comments

    /** Estado de carga específico para comentarios */
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /** Mensajes de error específicos del sistema de comentarios */
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    /** Comentario recién agregado - para feedback inmediato al usuario */
    private val _commentAdded = MutableLiveData<Comment?>()
    val commentAdded: LiveData<Comment?> = _commentAdded
    
    // ==================== DEPENDENCIAS ====================
    
    /** Repositorio para comunicación con la API de comentarios */
    private val apiRepository = ApiRepository()
    
    /** Repositorio para gestión de usuarios locales */
    private val userRepository = UserRepository(context)
    
    /** TokenManager para obtener información del usuario autenticado */
    private val tokenManager = TokenManager(context)

    fun loadComments(postId: String) {
        _isLoading.value = true
        
        android.util.Log.d("CommentsViewModel", "=== DEBUG: Cargando comentarios ===")
        android.util.Log.d("CommentsViewModel", "Post ID: $postId")
        
        viewModelScope.launch {
            when (val result = apiRepository.getCommentsByPostId(postId)) {
                is ApiResult.Success -> {
                    android.util.Log.d("CommentsViewModel", "✅ Comentarios obtenidos: ${result.data.size}")
                    val comments = result.data.map { commentResponse ->
                        apiRepository.convertToComment(commentResponse)
                    }
                    _comments.value = comments
                    _errorMessage.value = null
                }
                is ApiResult.Error -> {
                    android.util.Log.e("CommentsViewModel", "❌ Error al cargar comentarios: ${result.message}")
                    _errorMessage.value = "Error al cargar comentarios: ${result.message}"
                    
                    // Fallback: cargar comentarios mock si no hay comentarios actuales
                    if (_comments.value.isNullOrEmpty()) {
                        loadMockComments(postId)
                    }
                }
                is ApiResult.Loading -> {
                    // Ya está manejado por _isLoading
                }
            }
            _isLoading.value = false
        }
    }

    fun addComment(postId: String, content: String) {
        // Usar TokenManager como fuente principal de información del usuario
        val userId = tokenManager.getUserId()
        val userEmail = tokenManager.getUserEmail()
        val userName = tokenManager.getUserName()
        val userRole = tokenManager.getUserRole()
        
        if (userId == null || userEmail == null || userName == null) {
            _errorMessage.value = "No se pudo obtener la información del usuario. Por favor, inicia sesión nuevamente."
            return
        }
        
        _isLoading.value = true
        
        android.util.Log.d("CommentsViewModel", "=== DEBUG: Agregando comentario ===")
        android.util.Log.d("CommentsViewModel", "Post ID: $postId")
        android.util.Log.d("CommentsViewModel", "Content: $content")
        android.util.Log.d("CommentsViewModel", "User: $userEmail")
        android.util.Log.d("CommentsViewModel", "User ID: $userId")
        
        viewModelScope.launch {
            when (val result = apiRepository.createCommentDev(postId, userEmail, content)) {
                is ApiResult.Success -> {
                    android.util.Log.d("CommentsViewModel", "✅ Comentario creado exitosamente")
                    val newComment = apiRepository.convertToComment(result.data.comment)
                    _commentAdded.value = newComment
                    
                    // Recargar comentarios para mostrar el nuevo
                    loadComments(postId)
                }
                is ApiResult.Error -> {
                    android.util.Log.e("CommentsViewModel", "❌ Error al crear comentario: ${result.message}")
                    _errorMessage.value = "Error al crear comentario: ${result.message}"
                    
                    // Fallback: crear comentario localmente
                    createCommentLocally(postId, content, userId, userName, userRole)
                }
                is ApiResult.Loading -> {
                    // Manejado por el estado de loading
                }
            }
            _isLoading.value = false
        }
    }
    
    private fun loadMockComments(postId: String) {
        android.util.Log.d("CommentsViewModel", "Cargando comentarios mock como fallback")
        
        val mockComments = listOf(
            Comment(
                id = "mock-1",
                postId = postId,
                authorId = "user1",
                authorName = "Juan Pérez",
                authorRole = UserRole.STUDENT,
                content = "Excelente post, muy informativo!",
                timestamp = Date(System.currentTimeMillis() - 3600000),
                isApproved = true
            ),
            Comment(
                id = "mock-2",
                postId = postId,
                authorId = "user2",
                authorName = "María García",
                authorRole = UserRole.STUDENT,
                content = "Estoy de acuerdo contigo, gracias por compartir.",
                timestamp = Date(System.currentTimeMillis() - 7200000),
                isApproved = true
            )
        )
        
        _comments.value = mockComments
    }
    
    private fun createCommentLocally(postId: String, content: String, userId: String, userName: String, userRole: String?) {
        val newComment = Comment(
            id = UUID.randomUUID().toString(),
            postId = postId,
            authorId = userId,
            authorName = userName,
            authorRole = try { UserRole.valueOf(userRole ?: "STUDENT") } catch (e: Exception) { UserRole.STUDENT },
            content = content,
            timestamp = Date(),
            isApproved = true
        )
        
        _commentAdded.value = newComment
        
        // Agregar a la lista actual
        val currentComments = _comments.value?.toMutableList() ?: mutableListOf()
        currentComments.add(0, newComment)
        _comments.value = currentComments
    }
    
    fun editComment(comment: Comment, newContent: String) {
        val userId = tokenManager.getUserId()
        val userEmail = tokenManager.getUserEmail()
        
        if (userId == null || userEmail == null) {
            _errorMessage.value = "No se pudo obtener la información del usuario"
            return
        }
        
        _isLoading.value = true
        
        android.util.Log.d("CommentsViewModel", "=== DEBUG: Editando comentario ===")
        android.util.Log.d("CommentsViewModel", "Comment ID: ${comment.id}")
        android.util.Log.d("CommentsViewModel", "New Content: $newContent")
        android.util.Log.d("CommentsViewModel", "User Email: $userEmail")
        
        viewModelScope.launch {
            try {
                android.util.Log.d("CommentsViewModel", "Iniciando llamada al API...")
                when (val result = apiRepository.updateComment(comment.id, newContent, userId)) {
                    is ApiResult.Success -> {
                        android.util.Log.d("CommentsViewModel", "✅ Comentario actualizado en el backend")
                        android.util.Log.d("CommentsViewModel", "Response data: ${result.data}")
                        
                        try {
                            val updatedComment = apiRepository.convertToComment(result.data)
                            android.util.Log.d("CommentsViewModel", "Comentario convertido exitosamente")
                            updateCommentInList(updatedComment)
                            _errorMessage.value = null
                        } catch (e: Exception) {
                            android.util.Log.e("CommentsViewModel", "Error al convertir comentario: ${e.message}")
                            // Fallback: actualizar localmente
                            val updatedComment = comment.copy(content = newContent)
                            updateCommentInList(updatedComment)
                        }
                    }
                    is ApiResult.Error -> {
                        android.util.Log.e("CommentsViewModel", "❌ Error al actualizar comentario: ${result.message}")
                        _errorMessage.value = "Error al actualizar comentario: ${result.message}"
                        
                        // Fallback: actualizar localmente
                        val updatedComment = comment.copy(content = newContent)
                        updateCommentInList(updatedComment)
                    }
                    is ApiResult.Loading -> {
                        android.util.Log.d("CommentsViewModel", "Estado de loading...")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CommentsViewModel", "Excepción en editComment: ${e.message}")
                e.printStackTrace()
                _errorMessage.value = "Error inesperado: ${e.message}"
                
                // Fallback: actualizar localmente
                val updatedComment = comment.copy(content = newContent)
                updateCommentInList(updatedComment)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteComment(comment: Comment) {
        val userId = tokenManager.getUserId()
        val userEmail = tokenManager.getUserEmail()
        
        if (userId == null || userEmail == null) {
            _errorMessage.value = "No se pudo obtener la información del usuario"
            return
        }
        
        _isLoading.value = true
        
        android.util.Log.d("CommentsViewModel", "=== DEBUG: Eliminando comentario ===")
        android.util.Log.d("CommentsViewModel", "Comment ID: ${comment.id}")
        android.util.Log.d("CommentsViewModel", "User ID: $userId")
        
        viewModelScope.launch {
            when (val result = apiRepository.deleteComment(comment.id, userId)) {
                is ApiResult.Success -> {
                    android.util.Log.d("CommentsViewModel", "✅ Comentario eliminado del backend")
                    removeCommentFromList(comment.id)
                    _errorMessage.value = null
                }
                is ApiResult.Error -> {
                    android.util.Log.e("CommentsViewModel", "❌ Error al eliminar comentario: ${result.message}")
                    _errorMessage.value = "Error al eliminar comentario: ${result.message}"
                    
                    // No hacer fallback para eliminación por seguridad
                }
                is ApiResult.Loading -> {
                    // Manejado por el estado de loading
                }
            }
            _isLoading.value = false
        }
    }
    
    private fun updateCommentInList(updatedComment: Comment) {
        android.util.Log.d("CommentsViewModel", "=== Actualizando comentario en lista ===")
        android.util.Log.d("CommentsViewModel", "Updated comment ID: ${updatedComment.id}")
        android.util.Log.d("CommentsViewModel", "Updated content: ${updatedComment.content}")
        
        val currentComments = _comments.value?.toMutableList()
        if (currentComments == null) {
            android.util.Log.e("CommentsViewModel", "Lista de comentarios es null")
            return
        }
        
        val index = currentComments.indexOfFirst { it.id == updatedComment.id }
        android.util.Log.d("CommentsViewModel", "Index encontrado: $index")
        
        if (index != -1) {
            currentComments[index] = updatedComment
            _comments.value = currentComments
            android.util.Log.d("CommentsViewModel", "✅ Comentario actualizado en la lista")
        } else {
            android.util.Log.e("CommentsViewModel", "❌ No se encontró el comentario en la lista")
        }
    }
    
    private fun removeCommentFromList(commentId: String) {
        val currentComments = _comments.value?.toMutableList() ?: return
        currentComments.removeAll { it.id == commentId }
        _comments.value = currentComments
    }
}
