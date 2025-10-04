package com.universidad.uniway.ui.forum

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.universidad.uniway.R
import com.universidad.uniway.data.Post
import com.universidad.uniway.data.PostType
import com.universidad.uniway.data.UserRole
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(
    private val posts: MutableList<Post>,
    private val currentUserRole: UserRole,
    private val getCurrentUserId: () -> String?, // Función para obtener el ID del usuario actual
    private val onPostClick: (Post) -> Unit,
    private val onLikeClick: (Post) -> Unit,
    private val onDislikeClick: (Post) -> Unit,
    private val onCommentClick: (Post) -> Unit,

    private val onEditClick: (Post) -> Unit,
    private val onDeleteClick: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewAvatar: ImageView = itemView.findViewById(R.id.imageViewAvatar)
        val textViewAuthor: TextView = itemView.findViewById(R.id.textViewAuthor)
        val textViewTimestamp: TextView = itemView.findViewById(R.id.textViewTimestamp)
        val textViewPostType: TextView = itemView.findViewById(R.id.textViewPostType)
        val textViewContent: TextView = itemView.findViewById(R.id.textViewContent)
        val textViewLikeCount: TextView = itemView.findViewById(R.id.textViewLikeCount)
        val textViewDislikeCount: TextView = itemView.findViewById(R.id.textViewDislikeCount)
        val textViewCommentCount: TextView = itemView.findViewById(R.id.textViewCommentCount)
        
        val layoutLike: View = itemView.findViewById(R.id.layoutLike)
        val layoutDislike: View = itemView.findViewById(R.id.layoutDislike)
        val layoutComment: View = itemView.findViewById(R.id.layoutComment)

        val layoutAdminActions: View = itemView.findViewById(R.id.layoutAdminActions)
        
        val imageViewEdit: ImageView = itemView.findViewById(R.id.imageViewEdit)
        val imageViewDelete: ImageView = itemView.findViewById(R.id.imageViewDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        
        // Configurar información básica
        holder.textViewAuthor.text = post.authorName
        holder.textViewTimestamp.text = formatTimestamp(post.timestamp)
        holder.textViewContent.text = post.content
        holder.textViewLikeCount.text = post.likeCount.toString()
        holder.textViewDislikeCount.text = post.dislikeCount.toString()
        holder.textViewCommentCount.text = post.commentCount.toString()

        // Configurar tipo de post
        when (post.postType) {
            PostType.NEWS -> {
                holder.textViewPostType.text = "NOTICIA"
                holder.textViewPostType.visibility = View.VISIBLE
                holder.textViewPostType.setBackgroundResource(R.drawable.post_type_background)
            }
            PostType.ALERT -> {
                holder.textViewPostType.text = "ALERTA"
                holder.textViewPostType.visibility = View.VISIBLE
                holder.textViewPostType.setBackgroundResource(R.drawable.post_type_background)
            }
            PostType.ANNOUNCEMENT -> {
                holder.textViewPostType.text = "ANUNCIO"
                holder.textViewPostType.visibility = View.VISIBLE
                holder.textViewPostType.setBackgroundResource(R.drawable.post_type_background)
            }
            else -> {
                holder.textViewPostType.visibility = View.GONE
            }
        }

        // Configurar acciones según el rol del usuario y permisos
        val canEdit = canUserEditPost(post, currentUserRole, getCurrentUserId())
        val canDelete = canUserDeletePost(post, currentUserRole, getCurrentUserId())
        
        // Mostrar botones de edición/eliminación solo si tiene permisos
        if (canEdit || canDelete) {
            holder.layoutAdminActions.visibility = View.VISIBLE
            holder.imageViewEdit.visibility = if (canEdit) View.VISIBLE else View.GONE
            holder.imageViewDelete.visibility = if (canDelete) View.VISIBLE else View.GONE
        } else {
            holder.layoutAdminActions.visibility = View.GONE
        }
        
        // Configurar likes/dislikes - Todos los usuarios pueden dar likes
        holder.layoutLike.visibility = View.VISIBLE
        holder.layoutDislike.visibility = View.VISIBLE
        holder.layoutLike.isEnabled = true
        holder.layoutDislike.isEnabled = true

        // Configurar click listeners
        holder.layoutLike.setOnClickListener { onLikeClick(post) }
        holder.layoutDislike.setOnClickListener { onDislikeClick(post) }
        holder.layoutComment.setOnClickListener { onCommentClick(post) }

        holder.imageViewEdit.setOnClickListener { onEditClick(post) }
        holder.imageViewDelete.setOnClickListener { onDeleteClick(post) }
        
        // Click en el post completo
        holder.itemView.setOnClickListener { onPostClick(post) }
    }

    override fun getItemCount(): Int = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }

    fun addPost(post: Post) {
        posts.add(0, post) // Agregar al inicio
        notifyItemInserted(0)
    }

    fun updatePost(post: Post) {
        val index = posts.indexOfFirst { it.id == post.id }
        if (index != -1) {
            posts[index] = post
            notifyItemChanged(index)
        }
    }

    fun removePost(postId: String) {
        val index = posts.indexOfFirst { it.id == postId }
        if (index != -1) {
            posts.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    private fun formatTimestamp(timestamp: Date): String {
        val now = Date()
        val diff = now.time - timestamp.time
        
        return when {
            diff < 60000 -> "Hace un momento"
            diff < 3600000 -> "Hace ${diff / 60000} minutos"
            diff < 86400000 -> "Hace ${diff / 3600000} horas"
            else -> dateFormat.format(timestamp)
        }
    }
    
    /**
     * Verifica si el usuario actual puede editar un post específico
     */
    private fun canUserEditPost(post: Post, userRole: UserRole, userId: String?): Boolean {
        android.util.Log.d("PostAdapter", "=== DEBUG: Verificando permisos de edición ===")
        android.util.Log.d("PostAdapter", "Post ID: ${post.id}")
        android.util.Log.d("PostAdapter", "Post Author ID: ${post.authorId}")
        android.util.Log.d("PostAdapter", "Current User ID: $userId")
        android.util.Log.d("PostAdapter", "User Role: $userRole")
        
        if (userId == null) {
            android.util.Log.d("PostAdapter", "❌ Usuario ID es null")
            return false
        }
        
        // El autor puede editar su propio post
        if (post.authorId == userId) {
            android.util.Log.d("PostAdapter", "✅ Usuario es el autor del post")
            return true
        }
        
        // Administradores pueden editar cualquier post
        if (userRole == UserRole.ADMINISTRATION) {
            android.util.Log.d("PostAdapter", "✅ Usuario es administrador")
            return true
        }
        
        android.util.Log.d("PostAdapter", "❌ Usuario no tiene permisos de edición")
        return false
    }
    
    /**
     * Verifica si el usuario actual puede eliminar un post específico
     */
    private fun canUserDeletePost(post: Post, userRole: UserRole, userId: String?): Boolean {
        android.util.Log.d("PostAdapter", "=== DEBUG: Verificando permisos de eliminación ===")
        android.util.Log.d("PostAdapter", "Post ID: ${post.id}")
        android.util.Log.d("PostAdapter", "Post Author ID: ${post.authorId}")
        android.util.Log.d("PostAdapter", "Current User ID: $userId")
        android.util.Log.d("PostAdapter", "User Role: $userRole")
        
        if (userId == null) {
            android.util.Log.d("PostAdapter", "❌ Usuario ID es null")
            return false
        }
        
        // El autor puede eliminar su propio post
        if (post.authorId == userId) {
            android.util.Log.d("PostAdapter", "✅ Usuario es el autor del post")
            return true
        }
        
        // Administradores pueden eliminar cualquier post
        if (userRole == UserRole.ADMINISTRATION) {
            android.util.Log.d("PostAdapter", "✅ Usuario es administrador")
            return true
        }
        
        android.util.Log.d("PostAdapter", "❌ Usuario no tiene permisos de eliminación")
        return false
    }
}










