package com.universidad.uniway.ui.comments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.universidad.uniway.R
import com.universidad.uniway.data.Comment
import java.text.SimpleDateFormat
import java.util.*

class CommentAdapter(
    private var comments: MutableList<Comment> = mutableListOf(),
    private val getCurrentUserId: (() -> String?)? = null,
    private val getUserRole: (() -> String?)? = null,
    private val onEditClick: ((Comment) -> Unit)? = null,
    private val onDeleteClick: ((Comment) -> Unit)? = null
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val authorTextView: TextView = itemView.findViewById(R.id.textViewCommentAuthor)
        val contentTextView: TextView = itemView.findViewById(R.id.textViewCommentContent)
        val timeTextView: TextView = itemView.findViewById(R.id.textViewCommentTime)
        val layoutActions: View = itemView.findViewById(R.id.layoutCommentActions)
        val editButton: View = itemView.findViewById(R.id.imageViewEditComment)
        val deleteButton: View = itemView.findViewById(R.id.imageViewDeleteComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        
        holder.authorTextView.text = comment.authorName
        holder.contentTextView.text = comment.content
        
        // Formatear fecha
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.timeTextView.text = "hace ${getTimeAgo(comment.timestamp)}"
        
        // Configurar permisos y visibilidad de botones
        val currentUserId = getCurrentUserId?.invoke()
        val userRole = getUserRole?.invoke()
        val canEdit = canUserEditComment(comment, currentUserId, userRole)
        val canDelete = canUserDeleteComment(comment, currentUserId, userRole)
        
        // Mostrar botones de acción solo si tiene permisos
        if (canEdit || canDelete) {
            holder.layoutActions.visibility = View.VISIBLE
            holder.editButton.visibility = if (canEdit) View.VISIBLE else View.GONE
            holder.deleteButton.visibility = if (canDelete) View.VISIBLE else View.GONE
        } else {
            holder.layoutActions.visibility = View.GONE
        }
        
        // Configurar click listeners
        holder.editButton.setOnClickListener {
            android.util.Log.d("CommentAdapter", "Edit clicked for comment: ${comment.id}")
            onEditClick?.invoke(comment)
        }
        
        holder.deleteButton.setOnClickListener {
            android.util.Log.d("CommentAdapter", "Delete clicked for comment: ${comment.id}")
            onDeleteClick?.invoke(comment)
        }
    }

    override fun getItemCount(): Int = comments.size

    fun updateComments(newComments: List<Comment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }

    fun addComment(comment: Comment) {
        comments.add(0, comment) // Agregar al inicio
        notifyItemInserted(0)
    }

    private fun getTimeAgo(date: Date): String {
        val now = Date()
        val diff = now.time - date.time
        
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 0 -> "${days}d"
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes}m"
            else -> "ahora"
        }
    }
    
    /**
     * Verifica si el usuario actual puede editar un comentario específico
     */
    private fun canUserEditComment(comment: Comment, userId: String?, userRole: String?): Boolean {
        android.util.Log.d("CommentAdapter", "=== DEBUG: Verificando permisos de edición de comentario ===")
        android.util.Log.d("CommentAdapter", "Comment ID: ${comment.id}")
        android.util.Log.d("CommentAdapter", "Comment Author ID: ${comment.authorId}")
        android.util.Log.d("CommentAdapter", "Current User ID: $userId")
        android.util.Log.d("CommentAdapter", "User Role: $userRole")
        
        if (userId == null) {
            android.util.Log.d("CommentAdapter", "❌ Usuario ID es null")
            return false
        }
        
        // El autor puede editar su propio comentario
        if (comment.authorId == userId) {
            android.util.Log.d("CommentAdapter", "✅ Usuario es el autor del comentario")
            return true
        }
        
        // Administradores pueden editar cualquier comentario
        if (userRole == "ADMINISTRATION") {
            android.util.Log.d("CommentAdapter", "✅ Usuario es administrador")
            return true
        }
        
        android.util.Log.d("CommentAdapter", "❌ Usuario no tiene permisos de edición")
        return false
    }
    
    /**
     * Verifica si el usuario actual puede eliminar un comentario específico
     */
    private fun canUserDeleteComment(comment: Comment, userId: String?, userRole: String?): Boolean {
        android.util.Log.d("CommentAdapter", "=== DEBUG: Verificando permisos de eliminación de comentario ===")
        android.util.Log.d("CommentAdapter", "Comment ID: ${comment.id}")
        android.util.Log.d("CommentAdapter", "Comment Author ID: ${comment.authorId}")
        android.util.Log.d("CommentAdapter", "Current User ID: $userId")
        android.util.Log.d("CommentAdapter", "User Role: $userRole")
        
        if (userId == null) {
            android.util.Log.d("CommentAdapter", "❌ Usuario ID es null")
            return false
        }
        
        // El autor puede eliminar su propio comentario
        if (comment.authorId == userId) {
            android.util.Log.d("CommentAdapter", "✅ Usuario es el autor del comentario")
            return true
        }
        
        // Administradores pueden eliminar cualquier comentario
        if (userRole == "ADMINISTRATION") {
            android.util.Log.d("CommentAdapter", "✅ Usuario es administrador")
            return true
        }
        
        android.util.Log.d("CommentAdapter", "❌ Usuario no tiene permisos de eliminación")
        return false
    }
    
    fun updateComment(comment: Comment) {
        android.util.Log.d("CommentAdapter", "=== Actualizando comentario en adaptador ===")
        android.util.Log.d("CommentAdapter", "Comment ID: ${comment.id}")
        android.util.Log.d("CommentAdapter", "New content: ${comment.content}")
        
        val index = comments.indexOfFirst { it.id == comment.id }
        android.util.Log.d("CommentAdapter", "Index encontrado: $index")
        
        if (index != -1) {
            comments[index] = comment
            notifyItemChanged(index)
            android.util.Log.d("CommentAdapter", "✅ Comentario actualizado en adaptador")
        } else {
            android.util.Log.e("CommentAdapter", "❌ Comentario no encontrado en adaptador")
        }
    }
    
    fun removeComment(commentId: String) {
        val index = comments.indexOfFirst { it.id == commentId }
        if (index != -1) {
            comments.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
