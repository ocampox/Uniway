package com.universidad.uniway.ui.forum

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.uniway.data.Post
import com.universidad.uniway.data.PostType
import com.universidad.uniway.data.PostPriority
import com.universidad.uniway.data.User
import com.universidad.uniway.data.UserRole
import com.universidad.uniway.data.UserRepository
import com.universidad.uniway.network.ApiRepository
import com.universidad.uniway.network.ApiResult
import com.universidad.uniway.network.TokenManager
import kotlinx.coroutines.launch
import java.util.*

/**
 * ForumViewModel - ViewModel para la gestión del estado del foro estudiantil
 * 
 * Esta clase maneja toda la lógica de presentación del foro, incluyendo:
 * - Carga de posts desde la API con fallback a datos mock
 * - Sistema de reacciones (likes/dislikes) con sincronización al backend
 * - Creación de nuevos posts con validación de usuario
 * - Manejo de estados de carga y errores
 * - Actualización automática de la UI cuando cambian los datos
 * 
 * Arquitectura MVVM:
 * - Expone LiveData observables para la UI
 * - Maneja llamadas asíncronas con corrutinas
 * - Separa la lógica de negocio de la UI
 * - Proporciona fallbacks para funcionar sin conexión
 * 
 * Dependencias:
 * - ApiRepository: Para comunicación con el backend
 * - UserRepository: Para gestión de usuarios locales
 * - TokenManager: Para manejo de autenticación JWT
 */
class ForumViewModel(private val context: Context) : ViewModel() {

    // ==================== LIVEDATA OBSERVABLES ====================
    
    /** Lista de posts del foro - observable por la UI */
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    /** Estado de carga - observable por la UI para mostrar indicadores */
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /** Mensajes de error - observable por la UI para mostrar errores al usuario */
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // ==================== DEPENDENCIAS ====================
    
    /** Repositorio para gestión de usuarios locales */
    private val userRepository = UserRepository(context)
    private val apiRepository = ApiRepository()
    private val tokenManager = TokenManager(context)

    fun loadPosts() {
        _isLoading.value = true
        
        android.util.Log.d("ForumViewModel", "=== DEBUG: Cargando posts desde API ===")
        
        viewModelScope.launch {
            when (val result = apiRepository.getPosts()) {
                is ApiResult.Success -> {
                    // Convertir posts de la API al formato local
                    android.util.Log.d("ForumViewModel", "✅ Posts obtenidos de la API: ${result.data.size}")
                    val posts = result.data.map { postResponse ->
                        android.util.Log.d("ForumViewModel", "Post: ${postResponse.authorName} - ${postResponse.content.take(50)}...")
                        apiRepository.convertToPost(postResponse)
                    }
                    _posts.value = posts
                    android.util.Log.d("ForumViewModel", "Posts convertidos y asignados: ${posts.size}")
                    
                    // Limpiar mensaje de error si la carga fue exitosa
                    _errorMessage.value = null
                }
                is ApiResult.Error -> {
                    // Error en API, cargar posts mock como fallback
                    android.util.Log.e("ForumViewModel", "❌ Error al cargar posts de la API: ${result.message}")
                    android.util.Log.e("ForumViewModel", "Código de error: ${result.code}")
                    
                    // NO cargar posts mock automáticamente, mostrar error
                    _errorMessage.value = "Error de conexión: ${result.message}. Verifica que el backend esté ejecutándose."
                    
                    // Solo cargar posts mock si no hay posts actuales
                    if (_posts.value.isNullOrEmpty()) {
                        android.util.Log.w("ForumViewModel", "Cargando posts mock como fallback")
                        loadMockPosts()
                    }
                }
                is ApiResult.Loading -> {
                    // Ya está manejado por _isLoading
                }
            }
            _isLoading.value = false
        }
    }
    
    fun forceReloadFromApi() {
        android.util.Log.d("ForumViewModel", "=== FORZANDO RECARGA DESDE API ===")
        loadPosts()
    }
    
    private fun loadMockPosts() {
        // Posts de ejemplo para cuando no hay conexión
        val mockPosts = listOf(
            Post(
                id = "3",
                authorId = "student1",
                authorName = "María González",
                authorRole = UserRole.STUDENT,
                content = "¿Alguien sabe si hay algún grupo de estudio para la materia de Programación II? Me gustaría unirme.",
                postType = PostType.GENERAL,
                timestamp = Date(System.currentTimeMillis() - 900000),
                likeCount = 5,
                dislikeCount = 0,
                commentCount = 12
            ),
            Post(
                id = "4",
                authorId = "student2",
                authorName = "Carlos Ruiz",
                authorRole = UserRole.STUDENT,
                content = "Comparto mis apuntes de la clase de Matemáticas. Espero que les sirvan para el examen.",
                postType = PostType.GENERAL,
                timestamp = Date(System.currentTimeMillis() - 600000),
                likeCount = 18,
                dislikeCount = 1,
                commentCount = 6
            )
        )
        _posts.value = mockPosts
    }

    fun likePost(post: Post) {
        val userId = tokenManager.getUserId()
        if (userId == null) {
            _errorMessage.value = "No se pudo obtener la información del usuario"
            return
        }
        
        android.util.Log.d("ForumViewModel", "=== DEBUG: Like post ===")
        android.util.Log.d("ForumViewModel", "Post ID: ${post.id}")
        android.util.Log.d("ForumViewModel", "User ID: $userId")
        
        viewModelScope.launch {
            when (val result = apiRepository.likePost(post.id, userId)) {
                is ApiResult.Success -> {
                    android.util.Log.d("ForumViewModel", "✅ Like enviado exitosamente")
                    // Actualizar el post con la respuesta del servidor
                    val updatedPost = apiRepository.convertToPost(result.data.post)
                    updatePostInList(updatedPost)
                }
                is ApiResult.Error -> {
                    android.util.Log.e("ForumViewModel", "❌ Error al enviar like: ${result.message}")
                    // Fallback: actualizar localmente
                    updatePostLikeLocally(post, true)
                    _errorMessage.value = "Error al actualizar like: ${result.message}"
                }
                is ApiResult.Loading -> {
                    // Manejado por el estado de loading
                }
            }
        }
    }

    fun dislikePost(post: Post) {
        val userId = tokenManager.getUserId()
        if (userId == null) {
            _errorMessage.value = "No se pudo obtener la información del usuario"
            return
        }
        
        android.util.Log.d("ForumViewModel", "=== DEBUG: Dislike post ===")
        android.util.Log.d("ForumViewModel", "Post ID: ${post.id}")
        android.util.Log.d("ForumViewModel", "User ID: $userId")
        
        viewModelScope.launch {
            when (val result = apiRepository.dislikePost(post.id, userId)) {
                is ApiResult.Success -> {
                    android.util.Log.d("ForumViewModel", "✅ Dislike enviado exitosamente")
                    // Actualizar el post con la respuesta del servidor
                    val updatedPost = apiRepository.convertToPost(result.data.post)
                    updatePostInList(updatedPost)
                }
                is ApiResult.Error -> {
                    android.util.Log.e("ForumViewModel", "❌ Error al enviar dislike: ${result.message}")
                    // Fallback: actualizar localmente
                    updatePostLikeLocally(post, false)
                    _errorMessage.value = "Error al actualizar dislike: ${result.message}"
                }
                is ApiResult.Loading -> {
                    // Manejado por el estado de loading
                }
            }
        }
    }
    
    private fun updatePostInList(updatedPost: Post) {
        val currentPosts = _posts.value?.toMutableList() ?: return
        val index = currentPosts.indexOfFirst { it.id == updatedPost.id }
        
        if (index != -1) {
            currentPosts[index] = updatedPost
            _posts.value = currentPosts
        }
    }
    
    private fun updatePostLikeLocally(post: Post, isLike: Boolean) {
        val currentPosts = _posts.value?.toMutableList() ?: return
        val index = currentPosts.indexOfFirst { it.id == post.id }
        
        if (index != -1) {
            val updatedPost = if (isLike) {
                if (post.isLiked) {
                    post.copy(
                        isLiked = false,
                        likeCount = post.likeCount - 1
                    )
                } else {
                    post.copy(
                        isLiked = true,
                        isDisliked = false,
                        likeCount = post.likeCount + 1,
                        dislikeCount = if (post.isDisliked) post.dislikeCount - 1 else post.dislikeCount
                    )
                }
            } else {
                if (post.isDisliked) {
                    post.copy(
                        isDisliked = false,
                        dislikeCount = post.dislikeCount - 1
                    )
                } else {
                    post.copy(
                        isDisliked = true,
                        isLiked = false,
                        dislikeCount = post.dislikeCount + 1,
                        likeCount = if (post.isLiked) post.likeCount - 1 else post.likeCount
                    )
                }
            }
            
            currentPosts[index] = updatedPost
            _posts.value = currentPosts
        }
    }



    fun editPost(post: Post, newContent: String, newPostType: PostType = post.postType) {
        val userEmail = tokenManager.getUserEmail()
        if (userEmail == null) {
            _errorMessage.value = "No se pudo obtener la información del usuario"
            return
        }
        
        android.util.Log.d("ForumViewModel", "=== DEBUG: Editando post ===")
        android.util.Log.d("ForumViewModel", "Post ID: ${post.id}")
        android.util.Log.d("ForumViewModel", "New Content: $newContent")
        android.util.Log.d("ForumViewModel", "New PostType: ${newPostType.name}")
        android.util.Log.d("ForumViewModel", "User Email: $userEmail")
        
        viewModelScope.launch {
            when (val result = apiRepository.updatePostDev(
                postId = post.id,
                content = newContent,
                postType = newPostType.name,
                userEmail = userEmail
            )) {
                is ApiResult.Success -> {
                    android.util.Log.d("ForumViewModel", "✅ Post actualizado exitosamente")
                    val updatedPost = apiRepository.convertToPost(result.data.post)
                    updatePostInList(updatedPost)
                    _errorMessage.value = null
                }
                is ApiResult.Error -> {
                    android.util.Log.e("ForumViewModel", "❌ Error al actualizar post: ${result.message}")
                    _errorMessage.value = "Error al actualizar post: ${result.message}"
                }
                is ApiResult.Loading -> {
                    // Manejado por el estado de loading
                }
            }
        }
    }

    fun deletePost(post: Post) {
        val userEmail = tokenManager.getUserEmail()
        if (userEmail == null) {
            _errorMessage.value = "No se pudo obtener la información del usuario"
            return
        }
        
        android.util.Log.d("ForumViewModel", "=== DEBUG: Eliminando post ===")
        android.util.Log.d("ForumViewModel", "Post ID: ${post.id}")
        android.util.Log.d("ForumViewModel", "User Email: $userEmail")
        
        viewModelScope.launch {
            when (val result = apiRepository.deletePostDev(
                postId = post.id,
                userEmail = userEmail
            )) {
                is ApiResult.Success -> {
                    android.util.Log.d("ForumViewModel", "✅ Post eliminado exitosamente")
                    // Remover el post de la lista local
                    val currentPosts = _posts.value?.toMutableList() ?: return@launch
                    currentPosts.removeAll { it.id == post.id }
                    _posts.value = currentPosts
                    _errorMessage.value = null
                }
                is ApiResult.Error -> {
                    android.util.Log.e("ForumViewModel", "❌ Error al eliminar post: ${result.message}")
                    _errorMessage.value = "Error al eliminar post: ${result.message}"
                }
                is ApiResult.Loading -> {
                    // Manejado por el estado de loading
                }
            }
        }
    }
    
    /**
     * Verifica si el usuario actual puede editar un post específico
     * @param post Post a verificar
     * @return true si puede editar, false en caso contrario
     */
    fun canEditPost(post: Post): Boolean {
        val userId = tokenManager.getUserId() ?: return false
        val userRole = tokenManager.getUserRole() ?: return false
        
        // El autor puede editar su propio post
        if (post.authorId == userId) {
            return true
        }
        
        // Administradores pueden editar cualquier post
        if (userRole == "ADMINISTRATION") {
            return true
        }
        
        return false
    }
    
    /**
     * Verifica si el usuario actual puede eliminar un post específico
     * @param post Post a verificar
     * @return true si puede eliminar, false en caso contrario
     */
    fun canDeletePost(post: Post): Boolean {
        val userId = tokenManager.getUserId() ?: return false
        val userRole = tokenManager.getUserRole() ?: return false
        
        // El autor puede eliminar su propio post
        if (post.authorId == userId) {
            return true
        }
        
        // Administradores pueden eliminar cualquier post
        if (userRole == "ADMINISTRATION") {
            return true
        }
        
        return false
    }

    fun createPost(content: String, postType: PostType, priority: PostPriority = PostPriority.NORMAL) {
        // Usar TokenManager como fuente principal de información del usuario
        val userId = tokenManager.getUserId()
        val userEmail = tokenManager.getUserEmail()
        val userName = tokenManager.getUserName()
        val userRole = tokenManager.getUserRole()
        val token = tokenManager.getToken()
        
        if (userId == null || userEmail == null || userName == null) {
            _errorMessage.value = "No se pudo obtener la información del usuario. Por favor, inicia sesión nuevamente."
            return
        }
        
        // Crear objeto User temporal para compatibilidad
        val currentUser = User(
            id = userId,
            email = userEmail,
            fullName = userName,
            role = try { UserRole.valueOf(userRole ?: "STUDENT") } catch (e: Exception) { UserRole.STUDENT },
            password = "", // No necesario para crear posts
            studentId = "",
            program = ""
        )
        
        android.util.Log.d("ForumViewModel", "=== DEBUG: Creando post ===")
        android.util.Log.d("ForumViewModel", "Content: $content")
        android.util.Log.d("ForumViewModel", "PostType: ${postType.name}")
        android.util.Log.d("ForumViewModel", "Priority: ${priority.name}")
        android.util.Log.d("ForumViewModel", "Token: ${if (token != null) "Presente" else "Ausente"}")
        
        viewModelScope.launch {
            // Usar endpoint de desarrollo que identifica al usuario por email
            android.util.Log.d("ForumViewModel", "Intentando crear post en API (desarrollo)...")
            when (val result = apiRepository.createPostDev(content, postType.name, priority.name, userEmail)) {
                is ApiResult.Success -> {
                    // Post creado exitosamente en la API
                    android.util.Log.d("ForumViewModel", "Post creado exitosamente en API")
                    val newPost = apiRepository.convertToPost(result.data.post)
                    val currentPosts = _posts.value?.toMutableList() ?: mutableListOf()
                    currentPosts.add(0, newPost)
                    _posts.value = currentPosts
                    
                    // Recargar posts para asegurar sincronización
                    loadPosts()
                }
                is ApiResult.Error -> {
                    // Error en API, crear post localmente
                    android.util.Log.e("ForumViewModel", "Error en API: ${result.message}")
                    createPostLocally(currentUser, content, postType, priority)
                    _errorMessage.value = "Error al crear post: ${result.message}"
                }
                is ApiResult.Loading -> {
                    // Manejado por el estado de loading
                }
            }
        }
    }
    
    private fun createPostLocally(currentUser: com.universidad.uniway.data.User, content: String, postType: PostType, priority: PostPriority) {
        val newPost = Post(
            id = UUID.randomUUID().toString(),
            authorId = currentUser.id,
            authorName = currentUser.fullName,
            authorRole = currentUser.role,
            content = content,
            postType = postType,
            priority = priority,
            timestamp = Date(),
            likeCount = 0,
            dislikeCount = 0,
            commentCount = 0
        )
        
        val currentPosts = _posts.value?.toMutableList() ?: mutableListOf()
        currentPosts.add(0, newPost)
        _posts.value = currentPosts
    }
}

