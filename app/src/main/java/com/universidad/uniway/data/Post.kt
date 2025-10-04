package com.universidad.uniway.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Post(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: UserRole = UserRole.STUDENT,
    val content: String = "",
    val postType: PostType = PostType.GENERAL,
    val isPinned: Boolean = false,
    val isAlert: Boolean = false,
    val priority: PostPriority = PostPriority.NORMAL,
    val timestamp: Date = Date(),
    val likeCount: Int = 0,
    val dislikeCount: Int = 0,
    val commentCount: Int = 0,
    val isLiked: Boolean = false,
    val isDisliked: Boolean = false,

    val isApproved: Boolean = true
) : Parcelable

enum class PostType {
    GENERAL,        // Publicación general de estudiantes
    NEWS,           // Noticia de administración
    ALERT,          // Alerta de seguridad
    ANNOUNCEMENT    // Anuncio oficial
}

enum class PostPriority {
    LOW,            // Prioridad baja
    NORMAL,         // Prioridad normal
    HIGH,           // Prioridad alta
    URGENT          // Prioridad urgente
}

data class Comment(
    val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: UserRole = UserRole.STUDENT,
    val content: String = "",
    val timestamp: Date = Date(),
    val isApproved: Boolean = true
)





