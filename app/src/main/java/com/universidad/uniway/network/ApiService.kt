package com.universidad.uniway.network

import com.universidad.uniway.data.Post
import com.universidad.uniway.data.User
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // ==================== AUTENTICACIÓN ====================
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>
    
    @POST("auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<CodeSentResponse>
    
    @POST("auth/complete-registration")
    suspend fun completeRegistration(@Body completeRegistrationRequest: CompleteRegistrationRequest): Response<AuthResponse>
    
    @POST("verification/send-code")
    suspend fun sendVerificationCode(@Body sendCodeRequest: SendCodeRequest): Response<CodeSentResponse>
    
    @POST("verification/verify-code")
    suspend fun verifyCode(@Body verifyCodeRequest: VerifyCodeRequest): Response<VerifyCodeResponse>
    
    @POST("verification/resend-code")
    suspend fun resendVerificationCode(@Body sendCodeRequest: SendCodeRequest): Response<CodeSentResponse>
    
    // ==================== POSTS ====================
    @GET("posts")
    suspend fun getPosts(): Response<List<PostResponse>>
    
    @GET("posts/simple")
    suspend fun getPostsSimple(): Response<List<Map<String, Any>>>
    
    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") postId: String): Response<PostResponse>
    
    @POST("posts")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Body createPostRequest: CreatePostRequest
    ): Response<CreatePostResponse>
    
    @POST("posts/dev")
    suspend fun createPostDev(
        @Body createPostRequest: CreatePostRequestDev
    ): Response<CreatePostResponse>
    
    @PUT("posts/{id}")
    suspend fun updatePost(
        @Header("Authorization") token: String,
        @Path("id") postId: String,
        @Body updatePostRequest: UpdatePostRequest
    ): Response<UpdatePostResponse>
    
    @PUT("posts/{id}/dev")
    suspend fun updatePostDev(
        @Path("id") postId: String,
        @Body updatePostRequest: UpdatePostRequestDev
    ): Response<UpdatePostResponse>
    
    @DELETE("posts/{id}")
    suspend fun deletePost(
        @Header("Authorization") token: String,
        @Path("id") postId: String,
        @Query("userId") userId: String
    ): Response<DeletePostResponse>
    
    @DELETE("posts/{id}/dev")
    suspend fun deletePostDev(
        @Path("id") postId: String,
        @Query("userEmail") userEmail: String
    ): Response<DeletePostResponse>
    
    @POST("posts/{id}/like")
    suspend fun likePost(
        @Path("id") postId: String,
        @Body likeRequest: LikeRequest
    ): Response<LikeResponse>
    
    @POST("posts/{id}/dislike")
    suspend fun dislikePost(
        @Path("id") postId: String,
        @Body likeRequest: LikeRequest
    ): Response<LikeResponse>
    
    // ==================== COMENTARIOS ====================
    @GET("comments/post/{postId}")
    suspend fun getCommentsByPostId(@Path("postId") postId: String): Response<List<CommentResponse>>
    
    @POST("comments")
    suspend fun createComment(
        @Body createCommentRequest: CreateCommentRequest
    ): Response<CreateCommentResponse>
    
    @POST("comments/dev")
    suspend fun createCommentDev(
        @Body createCommentRequest: CreateCommentRequestDev
    ): Response<CreateCommentResponse>
    
    @PUT("comments/{id}")
    suspend fun updateComment(
        @Path("id") commentId: String,
        @Body updateCommentRequest: UpdateCommentRequest
    ): Response<CommentResponse>
    
    @DELETE("comments/{id}")
    suspend fun deleteComment(
        @Path("id") commentId: String,
        @Query("userId") userId: String
    ): Response<Map<String, String>>
    
    // ==================== USUARIO ====================
    @GET("users/profile")
    suspend fun getUserProfile(@Header("Authorization") token: String): Response<UserResponse>
    
    @PUT("users/profile")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Body updateProfileRequest: UpdateProfileRequest
    ): Response<UpdateProfileResponse>
    
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: String): Response<UserResponse>

    // ==================== TEACHERS ====================
    @GET("teachers")
    suspend fun getTeachers(): Response<List<TeacherResponse>>

    @GET("teachers/{id}")
    suspend fun getTeacherById(@Path("id") teacherId: String): Response<TeacherResponse>

    @GET("teachers/{id}/reviews")
    suspend fun getTeacherReviews(@Path("id") teacherId: String): Response<List<TeacherReviewResponse>>

    @POST("teachers/{id}/reviews")
    suspend fun addTeacherReview(
        @Path("id") teacherId: String,
        @Body addTeacherReviewRequest: AddTeacherReviewRequest
    ): Response<AddTeacherReviewResponse>

    // ==================== PASSWORD RESET ====================
    @POST("auth/forgot-password")
    suspend fun sendPasswordResetCode(@Body sendPasswordResetRequest: SendPasswordResetRequest): Response<CodeSentResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body resetPasswordRequest: ResetPasswordRequest): Response<ResetPasswordResponse>

    // ==================== TEACHER RECOMMENDATIONS ====================
    @POST("teacher-recommendations")
    suspend fun createTeacherRecommendation(@Body createRequest: CreateTeacherRecommendationRequest): Response<CreateTeacherRecommendationResponse>

    @POST("teacher-recommendations/with-rating")
    suspend fun createTeacherRecommendationWithRating(@Body createRequest: CreateTeacherRecommendationWithRatingRequest): Response<CreateTeacherRecommendationResponse>

    @GET("teacher-recommendations/user/{userId}")
    suspend fun getUserRecommendations(@Path("userId") userId: String): Response<List<TeacherRecommendationResponse>>

    @GET("teacher-recommendations")
    suspend fun getAllRecommendations(@Query("userId") userId: String?, @Query("subject") subject: String?): Response<List<TeacherRecommendationResponse>>

    @POST("teacher-recommendations/{id}/like")
    suspend fun likeRecommendation(@Path("id") recommendationId: String, @Body likeRequest: LikeRecommendationRequest): Response<LikeRecommendationResponse>

    @POST("teacher-recommendations/{id}/dislike")
    suspend fun dislikeRecommendation(@Path("id") recommendationId: String, @Body likeRequest: LikeRecommendationRequest): Response<LikeRecommendationResponse>

    @DELETE("teacher-recommendations/{id}")
    suspend fun deleteRecommendation(@Path("id") recommendationId: String, @Query("userId") userId: String): Response<DeleteRecommendationResponse>

    @GET("teacher-recommendations/subjects")
    suspend fun getAvailableSubjects(): Response<List<String>>

    @GET("teacher-recommendations/stats/{userId}")
    suspend fun getRecommendationStats(@Path("userId") userId: String): Response<RecommendationStatsResponse>
}

// ==================== REQUEST MODELS ====================
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val role: String,
    val fullName: String,
    val studentId: String?,
    val program: String?
)

data class CompleteRegistrationRequest(
    val email: String,
    val password: String,
    val role: String,
    val fullName: String,
    val studentId: String?,
    val program: String?,
    val verificationCode: String
)

data class SendCodeRequest(
    val email: String
)

data class VerifyCodeRequest(
    val email: String,
    val code: String
)

data class CreatePostRequest(
    val content: String,
    val postType: String,
    val priority: String
)

data class CreatePostRequestDev(
    val content: String,
    val postType: String,
    val priority: String,
    val authorEmail: String
)

data class UpdatePostRequest(
    val content: String,
    val postType: String,
    val userId: String
)

data class UpdatePostRequestDev(
    val content: String,
    val postType: String,
    val userEmail: String
)

data class DeletePostRequest(
    val userId: String
)

data class DeletePostRequestDev(
    val userEmail: String
)

data class UpdateProfileRequest(
    val fullName: String?,
    val phone: String?,
    val address: String?,
    val program: String?
)

data class LikeRequest(
    val userId: String
)

data class CreateCommentRequest(
    val postId: String,
    val authorId: String,
    val content: String
)

data class CreateCommentRequestDev(
    val postId: String,
    val authorEmail: String,
    val content: String
)

data class UpdateCommentRequest(
    val content: String,
    val userId: String
)

data class DeleteCommentRequest(
    val userId: String
)

// ==================== RESPONSE MODELS ====================
data class AuthResponse(
    val user: UserResponse,
    val token: String,
    val message: String
)

data class CodeSentResponse(
    val message: String,
    val email: String,
    val expiresInMinutes: Int,
    val requiresVerification: Boolean,
    val devCode: String? = null,
    val devNote: String? = null
)

data class VerifyCodeResponse(
    val valid: Boolean,
    val message: String,
    val email: String
)

data class UserResponse(
    val id: String,
    val email: String,
    val role: String,
    val fullName: String,
    val studentId: String?,
    val program: String?,
    val profileImageUrl: String?,
    val phone: String?,
    val address: String?,
    val isActive: Boolean,
    val createdAt: String?,
    val updatedAt: String?
)

data class PostResponse(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorRole: String,
    val content: String,
    val postType: String,
    val priority: String,
    val isPinned: Boolean,
    val isAlert: Boolean,
    val isApproved: Boolean,
    val createdAt: String,
    val updatedAt: String?,
    val likeCount: Long,
    val dislikeCount: Long,
    val commentCount: Long,
    val isLiked: Boolean,
    val isDisliked: Boolean,

)

data class CreatePostResponse(
    val post: PostResponse,
    val message: String
)

data class UpdateProfileResponse(
    val user: UserResponse,
    val message: String
)

data class LikeResponse(
    val post: PostResponse,
    val message: String
)

data class CommentResponse(
    val id: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val authorRole: String,
    val content: String,
    val isApproved: Boolean,
    val createdAt: String,
    val updatedAt: String?
)

data class CreateCommentResponse(
    val comment: CommentResponse,
    val message: String
)

data class UpdatePostResponse(
    val post: PostResponse,
    val message: String
)

data class DeletePostResponse(
    val message: String,
    val postId: String
)

data class ApiErrorResponse(
    val error: String
)

// ==================== TEACHER MODELS ====================
data class TeacherResponse(
    val id: String,
    val fullName: String,
    val institutionalEmail: String,
    val subjects: String?,
    val advisoriesAvailable: Boolean,
    val virtualWorkshopsAvailable: Boolean,
    val averageRating: Double,
    val reviewCount: Int
)

data class TeacherReviewResponse(
    val id: String,
    val teacherId: String?,
    val authorId: String,
    val authorName: String?,
    val rating: Int,
    val comment: String?,
    val createdAt: String
)

data class AddTeacherReviewRequest(
    val authorId: String,
    val rating: Int,
    val comment: String
)

data class AddTeacherReviewResponse(
    val review: TeacherReviewResponse,
    val message: String
)

// ==================== PASSWORD RESET MODELS ====================
data class SendPasswordResetRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String
)

data class ResetPasswordResponse(
    val message: String,
    val success: Boolean
)

// ==================== TEACHER RECOMMENDATION MODELS ====================
data class CreateTeacherRecommendationRequest(
    val studentId: String,
    val teacherName: String,
    val subject: String,
    val semester: String,
    val year: Int,
    val reference: String
)

data class CreateTeacherRecommendationWithRatingRequest(
    val studentId: String,
    val teacherName: String,
    val subject: String,
    val semester: String,
    val year: Int,
    val reference: String,
    val rating: Int
)

data class CreateTeacherRecommendationResponse(
    val recommendation: TeacherRecommendationResponse,
    val message: String
)

data class TeacherRecommendationResponse(
    val id: String,
    val studentId: String,
    val studentName: String,
    val teacherName: String,
    val subject: String,
    val semester: String?,
    val year: Int?,
    val reference: String,
    val rating: Int?,
    val isActive: Boolean,
    val createdAt: String,
    val likeCount: Long,
    val dislikeCount: Long,
    val totalReactions: Long,
    val isLiked: Boolean,
    val isDisliked: Boolean,
    val userReaction: String?
)

data class LikeRecommendationRequest(
    val userId: String
)

data class LikeRecommendationResponse(
    val recommendation: TeacherRecommendationResponse,
    val message: String
)

data class DeleteRecommendationRequest(
    val userId: String
)

data class DeleteRecommendationResponse(
    val message: String,
    val recommendationId: String
)

data class RecommendationStatsResponse(
    val myRecommendationsCount: Int,
    val myTotalLikesReceived: Int,
    val myTotalDislikesReceived: Int,
    val totalRecommendationsCount: Int,
    val totalReactionsCount: Int
)