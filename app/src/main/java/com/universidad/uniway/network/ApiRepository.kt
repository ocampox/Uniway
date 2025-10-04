package com.universidad.uniway.network

import com.universidad.uniway.data.Post
import com.universidad.uniway.data.PostType
import com.universidad.uniway.data.PostPriority
import com.universidad.uniway.data.User
import com.universidad.uniway.data.UserRole
import com.universidad.uniway.data.Teacher
import com.universidad.uniway.data.TeacherReview
import com.universidad.uniway.data.StudentTeacher
import com.universidad.uniway.data.TeacherRecommendation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApiRepository {
    
    val apiService = ApiClient.apiService
    
    // ==================== AUTENTICACIÓN ====================
    
    suspend fun login(email: String, password: String): ApiResult<AuthResponse> {
        return safeApiCall {
            apiService.login(LoginRequest(email, password))
        }
    }
    
    suspend fun register(
        email: String,
        password: String,
        role: UserRole,
        fullName: String,
        studentId: String?,
        program: String?
    ): ApiResult<CodeSentResponse> {
        return safeApiCall {
            apiService.register(
                RegisterRequest(
                    email = email,
                    password = password,
                    role = role.name,
                    fullName = fullName,
                    studentId = studentId,
                    program = program
                )
            )
        }
    }
    
    suspend fun sendVerificationCode(email: String): ApiResult<CodeSentResponse> {
        return safeApiCall {
            apiService.sendVerificationCode(SendCodeRequest(email))
        }
    }
    
    suspend fun verifyCode(email: String, code: String): ApiResult<VerifyCodeResponse> {
        return safeApiCall {
            apiService.verifyCode(VerifyCodeRequest(email, code))
        }
    }
    
    suspend fun resendVerificationCode(email: String): ApiResult<CodeSentResponse> {
        return safeApiCall {
            apiService.resendVerificationCode(SendCodeRequest(email))
        }
    }
    
    suspend fun completeRegistration(
        email: String,
        password: String,
        role: UserRole,
        fullName: String,
        studentId: String?,
        program: String?,
        verificationCode: String
    ): ApiResult<AuthResponse> {
        return safeApiCall {
            apiService.completeRegistration(
                CompleteRegistrationRequest(
                    email = email,
                    password = password,
                    role = role.name,
                    fullName = fullName,
                    studentId = studentId,
                    program = program,
                    verificationCode = verificationCode
                )
            )
        }
    }
    
    // ==================== POSTS ====================
    
    suspend fun getPosts(): ApiResult<List<PostResponse>> {
        android.util.Log.d("ApiRepository", "=== DEBUG: Obteniendo posts de la API ===")
        android.util.Log.d("ApiRepository", "URL completa: http://10.0.2.2:8080/posts")
        
        return safeApiCall {
            apiService.getPosts()
        }
    }
    
    suspend fun getPostById(postId: String): ApiResult<PostResponse> {
        return safeApiCall {
            apiService.getPostById(postId)
        }
    }
    
    suspend fun createPost(
        token: String,
        content: String,
        postType: String,
        priority: String
    ): ApiResult<CreatePostResponse> {
        android.util.Log.d("ApiRepository", "=== DEBUG: Enviando createPost ===")
        android.util.Log.d("ApiRepository", "Token: Bearer $token")
        android.util.Log.d("ApiRepository", "Content: $content")
        android.util.Log.d("ApiRepository", "PostType: $postType")
        android.util.Log.d("ApiRepository", "Priority: $priority")
        
        val request = CreatePostRequest(content, postType, priority)
        android.util.Log.d("ApiRepository", "Request: $request")
        
        return safeApiCall {
            apiService.createPost(
                token = "Bearer $token",
                createPostRequest = request
            )
        }
    }
    
    suspend fun createPostDev(
        content: String,
        postType: String,
        priority: String,
        authorEmail: String
    ): ApiResult<CreatePostResponse> {
        android.util.Log.d("ApiRepository", "=== DEBUG: Enviando createPostDev ===")
        android.util.Log.d("ApiRepository", "Content: $content")
        android.util.Log.d("ApiRepository", "PostType: $postType")
        android.util.Log.d("ApiRepository", "Priority: $priority")
        android.util.Log.d("ApiRepository", "AuthorEmail: $authorEmail")
        
        val request = CreatePostRequestDev(content, postType, priority, authorEmail)
        android.util.Log.d("ApiRepository", "Request: $request")
        
        return safeApiCall {
            apiService.createPostDev(request)
        }
    }
    
    suspend fun updatePost(
        token: String,
        postId: String,
        content: String,
        postType: String,
        userId: String
    ): ApiResult<UpdatePostResponse> {
        android.util.Log.d("ApiRepository", "=== DEBUG: Actualizando post ===")
        android.util.Log.d("ApiRepository", "Post ID: $postId")
        android.util.Log.d("ApiRepository", "Content: $content")
        android.util.Log.d("ApiRepository", "PostType: $postType")
        android.util.Log.d("ApiRepository", "User ID: $userId")
        
        return safeApiCall {
            apiService.updatePost(
                token = "Bearer $token",
                postId = postId,
                updatePostRequest = UpdatePostRequest(content, postType, userId)
            )
        }
    }
    
    suspend fun updatePostDev(
        postId: String,
        content: String,
        postType: String,
        userEmail: String
    ): ApiResult<UpdatePostResponse> {
        android.util.Log.d("ApiRepository", "=== DEBUG: Actualizando post (dev) ===")
        android.util.Log.d("ApiRepository", "Post ID: $postId")
        android.util.Log.d("ApiRepository", "Content: $content")
        android.util.Log.d("ApiRepository", "PostType: $postType")
        android.util.Log.d("ApiRepository", "User Email: $userEmail")
        
        return safeApiCall {
            apiService.updatePostDev(
                postId = postId,
                updatePostRequest = UpdatePostRequestDev(content, postType, userEmail)
            )
        }
    }
    
    suspend fun deletePost(
        token: String, 
        postId: String, 
        userId: String
    ): ApiResult<DeletePostResponse> {
        android.util.Log.d("ApiRepository", "=== DEBUG: Eliminando post ===")
        android.util.Log.d("ApiRepository", "Post ID: $postId")
        android.util.Log.d("ApiRepository", "User ID: $userId")
        
        return safeApiCall {
            apiService.deletePost(
                token = "Bearer $token", 
                postId = postId,
                userId = userId
            )
        }
    }
    
    suspend fun deletePostDev(
        postId: String,
        userEmail: String
    ): ApiResult<DeletePostResponse> {
        android.util.Log.d("ApiRepository", "=== DEBUG: Eliminando post (dev) ===")
        android.util.Log.d("ApiRepository", "Post ID: $postId")
        android.util.Log.d("ApiRepository", "User Email: $userEmail")
        
        return safeApiCall {
            apiService.deletePostDev(
                postId = postId,
                userEmail = userEmail
            )
        }
    }
    
    suspend fun likePost(postId: String, userId: String): ApiResult<LikeResponse> {
        android.util.Log.d("ApiRepository", "=== DEBUG: Enviando like ===")
        android.util.Log.d("ApiRepository", "Post ID: $postId")
        android.util.Log.d("ApiRepository", "User ID: $userId")
        
        return safeApiCall {
            apiService.likePost(postId, LikeRequest(userId))
        }
    }
    
    suspend fun dislikePost(postId: String, userId: String): ApiResult<LikeResponse> {
        android.util.Log.d("ApiRepository", "=== DEBUG: Enviando dislike ===")
        android.util.Log.d("ApiRepository", "Post ID: $postId")
        android.util.Log.d("ApiRepository", "User ID: $userId")
        
        return safeApiCall {
            apiService.dislikePost(postId, LikeRequest(userId))
        }
    }
    
    // ==================== COMENTARIOS ====================
    
    suspend fun getCommentsByPostId(postId: String): ApiResult<List<CommentResponse>> {
        android.util.Log.d("ApiRepository", "=== DEBUG: Obteniendo comentarios ===")
        android.util.Log.d("ApiRepository", "Post ID: $postId")
        
        return safeApiCall {
            apiService.getCommentsByPostId(postId)
        }
    }
    
    suspend fun createComment(
        postId: String,
        authorId: String,
        content: String
    ): ApiResult<CreateCommentResponse> {
        android.util.Log.d("ApiRepository", "=== DEBUG: Creando comentario ===")
        android.util.Log.d("ApiRepository", "Post ID: $postId")
        android.util.Log.d("ApiRepository", "Author ID: $authorId")
        android.util.Log.d("ApiRepository", "Content: $content")
        
        return safeApiCall {
            apiService.createComment(CreateCommentRequest(postId, authorId, content))
        }
    }
    
    suspend fun createCommentDev(
        postId: String,
        authorEmail: String,
        content: String
    ): ApiResult<CreateCommentResponse> {
        android.util.Log.d("ApiRepository", "=== DEBUG: Creando comentario (dev) ===")
        android.util.Log.d("ApiRepository", "Post ID: $postId")
        android.util.Log.d("ApiRepository", "Author Email: $authorEmail")
        android.util.Log.d("ApiRepository", "Content: $content")
        
        return safeApiCall {
            apiService.createCommentDev(CreateCommentRequestDev(postId, authorEmail, content))
        }
    }
    
    // ==================== USUARIO ====================
    
    suspend fun getUserProfile(token: String): ApiResult<UserResponse> {
        return safeApiCall {
            apiService.getUserProfile("Bearer $token")
        }
    }
    
    suspend fun updateUserProfile(
        token: String,
        fullName: String?,
        phone: String?,
        address: String?,
        program: String?
    ): ApiResult<UpdateProfileResponse> {
        return safeApiCall {
            apiService.updateUserProfile(
                token = "Bearer $token",
                updateProfileRequest = UpdateProfileRequest(fullName, phone, address, program)
            )
        }
    }
    
    suspend fun getUserById(userId: String): ApiResult<UserResponse> {
        return safeApiCall {
            apiService.getUserById(userId)
        }
    }
    
    // ==================== CONVERSORES ====================
    
    fun convertToUser(userResponse: UserResponse): User {
        return User(
            id = userResponse.id,
            email = userResponse.email,
            password = "", // La API no devuelve la contraseña por seguridad
            fullName = userResponse.fullName,
            studentId = userResponse.studentId ?: "",
            program = userResponse.program ?: "",
            role = try { UserRole.valueOf(userResponse.role) } catch (e: Exception) { UserRole.STUDENT },
            phone = userResponse.phone ?: "",
            address = userResponse.address ?: ""
        )
    }
    
    fun convertToPost(postResponse: PostResponse): Post {
        return Post(
            id = postResponse.id,
            authorId = postResponse.authorId,
            authorName = postResponse.authorName,
            authorRole = try { UserRole.valueOf(postResponse.authorRole) } catch (e: Exception) { UserRole.STUDENT },
            content = postResponse.content,
            postType = try { PostType.valueOf(postResponse.postType) } catch (e: Exception) { PostType.GENERAL },
            isPinned = postResponse.isPinned,
            isAlert = postResponse.isAlert,
            priority = try { PostPriority.valueOf(postResponse.priority) } catch (e: Exception) { PostPriority.NORMAL },
            timestamp = try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(postResponse.createdAt) ?: Date()
            } catch (e: Exception) {
                Date() // Usar fecha actual si hay error en el parsing
            },
            likeCount = postResponse.likeCount.toInt(),
            dislikeCount = postResponse.dislikeCount.toInt(),
            commentCount = postResponse.commentCount.toInt(),
            isLiked = postResponse.isLiked,
            isDisliked = postResponse.isDisliked,

            isApproved = postResponse.isApproved
        )
    }

    // ==================== TEACHERS (ELIMINADO - Usar TEACHER RECOMMENDATIONS) ====================

    // ==================== PASSWORD RESET ====================
    suspend fun sendPasswordResetCode(email: String): ApiResult<CodeSentResponse> {
        return safeApiCall {
            apiService.sendPasswordResetCode(SendPasswordResetRequest(email))
        }
    }

    suspend fun resetPassword(email: String, code: String, newPassword: String): ApiResult<ResetPasswordResponse> {
        return safeApiCall {
            apiService.resetPassword(ResetPasswordRequest(email, code, newPassword))
        }
    }

    // ==================== TEACHER RECOMMENDATIONS ====================
    suspend fun createTeacherRecommendation(
        studentId: String,
        teacherName: String,
        subject: String,
        semester: String,
        year: Int,
        reference: String
    ): ApiResult<CreateTeacherRecommendationResponse> {
        return safeApiCall {
            apiService.createTeacherRecommendation(
                CreateTeacherRecommendationRequest(studentId, teacherName, subject, semester, year, reference)
            )
        }
    }

    suspend fun createTeacherRecommendationWithRating(
        studentId: String,
        teacherName: String,
        subject: String,
        semester: String,
        year: Int,
        reference: String,
        rating: Int
    ): ApiResult<CreateTeacherRecommendationResponse> {
        android.util.Log.d("ApiRepository", "=== DEBUG: Creando recomendación con rating ===")
        android.util.Log.d("ApiRepository", "Student ID: $studentId")
        android.util.Log.d("ApiRepository", "Teacher: $teacherName")
        android.util.Log.d("ApiRepository", "Subject: $subject")
        android.util.Log.d("ApiRepository", "Semester: $semester")
        android.util.Log.d("ApiRepository", "Year: $year")
        android.util.Log.d("ApiRepository", "Rating: $rating")
        android.util.Log.d("ApiRepository", "Reference: ${reference.take(50)}...")
        
        return safeApiCall {
            apiService.createTeacherRecommendationWithRating(
                CreateTeacherRecommendationWithRatingRequest(studentId, teacherName, subject, semester, year, reference, rating)
            )
        }
    }

    suspend fun getUserRecommendations(userId: String): ApiResult<List<TeacherRecommendation>> {
        android.util.Log.d("ApiRepository", "getUserRecommendations called for userId: $userId")
        return when (val result = safeApiCall { apiService.getUserRecommendations(userId) }) {
            is ApiResult.Success -> {
                android.util.Log.d("ApiRepository", "Raw API response: ${result.data.size} recommendations")
                val converted = result.data.map { convertToTeacherRecommendation(it) }
                android.util.Log.d("ApiRepository", "Converted to ${converted.size} TeacherRecommendation objects")
                ApiResult.Success(converted)
            }
            is ApiResult.Error -> {
                android.util.Log.e("ApiRepository", "API Error: ${result.message}")
                ApiResult.Error<List<TeacherRecommendation>>(result.message, result.code)
            }
            is ApiResult.Loading -> {
                android.util.Log.d("ApiRepository", "API Loading...")
                ApiResult.Loading()
            }
        }
    }

    suspend fun getAllRecommendations(userId: String?, subject: String?): ApiResult<List<TeacherRecommendation>> {
        return when (val result = safeApiCall { apiService.getAllRecommendations(userId, subject) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.map { convertToTeacherRecommendation(it) })
            is ApiResult.Error -> ApiResult.Error<List<TeacherRecommendation>>(result.message, result.code)
            is ApiResult.Loading -> ApiResult.Loading()
        }
    }

    suspend fun likeRecommendation(recommendationId: String, userId: String): ApiResult<LikeRecommendationResponse> {
        return safeApiCall {
            apiService.likeRecommendation(recommendationId, LikeRecommendationRequest(userId))
        }
    }

    suspend fun dislikeRecommendation(recommendationId: String, userId: String): ApiResult<LikeRecommendationResponse> {
        return safeApiCall {
            apiService.dislikeRecommendation(recommendationId, LikeRecommendationRequest(userId))
        }
    }

    suspend fun deleteRecommendation(recommendationId: String, userId: String): ApiResult<DeleteRecommendationResponse> {
        return safeApiCall {
            apiService.deleteRecommendation(recommendationId, userId)
        }
    }

    suspend fun getAvailableSubjects(): ApiResult<List<String>> {
        return safeApiCall { apiService.getAvailableSubjects() }
    }

    suspend fun getRecommendationStats(userId: String): ApiResult<RecommendationStatsResponse> {
        return safeApiCall { apiService.getRecommendationStats(userId) }
    }

    private fun convertToTeacherRecommendation(response: TeacherRecommendationResponse): TeacherRecommendation {
        val date = try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(response.createdAt) ?: Date()
        } catch (e: Exception) { Date() }
        return TeacherRecommendation(
            id = response.id,
            studentId = response.studentId,
            studentName = response.studentName,
            teacherName = response.teacherName,
            subject = response.subject,
            semester = response.semester ?: "",
            year = response.year ?: 0,
            reference = response.reference,
            rating = response.rating ?: 0,
            isActive = response.isActive,
            createdAt = date,
            likeCount = response.likeCount,
            dislikeCount = response.dislikeCount,
            totalReactions = response.totalReactions,
            isLiked = response.isLiked,
            isDisliked = response.isDisliked,
            userReaction = response.userReaction
        )
    }
    
    fun convertToComment(commentResponse: CommentResponse): com.universidad.uniway.data.Comment {
        return com.universidad.uniway.data.Comment(
            id = commentResponse.id,
            postId = commentResponse.postId,
            authorId = commentResponse.authorId,
            authorName = commentResponse.authorName,
            authorRole = try { UserRole.valueOf(commentResponse.authorRole) } catch (e: Exception) { UserRole.STUDENT },
            content = commentResponse.content,
            timestamp = try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(commentResponse.createdAt) ?: Date()
            } catch (e: Exception) {
                Date() // Usar fecha actual si hay error en el parsing
            },
            isApproved = commentResponse.isApproved
        )
    }
    
    suspend fun updateComment(commentId: String, content: String, userId: String): ApiResult<CommentResponse> {
        return safeApiCall {
            apiService.updateComment(commentId, UpdateCommentRequest(content, userId))
        }
    }
    
    suspend fun deleteComment(commentId: String, userId: String): ApiResult<Map<String, String>> {
        return safeApiCall {
            apiService.deleteComment(commentId, userId)
        }
    }
}