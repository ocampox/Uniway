package com.universidad.uniway.ui.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.universidad.uniway.R
import com.universidad.uniway.data.Post
import com.universidad.uniway.data.Comment
import java.util.*

class CommentsFragmentSimple : Fragment() {

    private lateinit var postTitleTextView: TextView
    private lateinit var recyclerViewComments: RecyclerView
    private lateinit var editTextComment: EditText
    private lateinit var buttonAddComment: Button
    
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var viewModel: CommentsViewModel
    private var currentPost: Post? = null
    private val comments = mutableListOf<Comment>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_comments_simple, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Obtener referencias a las vistas
        postTitleTextView = view.findViewById(R.id.textViewPostTitle)
        recyclerViewComments = view.findViewById(R.id.recyclerViewComments)
        editTextComment = view.findViewById(R.id.editTextComment)
        buttonAddComment = view.findViewById(R.id.buttonAddComment)
        
        // Obtener el post desde los argumentos
        currentPost = arguments?.getParcelable("post")
        
        // Inicializar ViewModel
        viewModel = ViewModelProvider(this, CommentsViewModelFactory(requireContext()))[CommentsViewModel::class.java]
        
        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        loadComments()
        updatePostHeader()
    }

    private fun setupRecyclerView() {
        val tokenManager = com.universidad.uniway.network.TokenManager(requireContext())
        
        commentAdapter = CommentAdapter(
            comments = comments,
            getCurrentUserId = { tokenManager.getUserId() },
            getUserRole = { tokenManager.getUserRole() },
            onEditClick = { comment -> onEditComment(comment) },
            onDeleteClick = { comment -> onDeleteComment(comment) }
        )
        
        recyclerViewComments.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = commentAdapter
        }
    }

    private fun setupClickListeners() {
        buttonAddComment.setOnClickListener {
            addComment()
        }
    }

    private fun setupObservers() {
        // Observar comentarios
        viewModel.comments.observe(viewLifecycleOwner) { commentList ->
            android.util.Log.d("CommentsFragment", "Comentarios recibidos: ${commentList.size}")
            commentList.forEach { comment ->
                android.util.Log.d("CommentsFragment", "  - ${comment.id}: ${comment.content.take(50)}...")
            }
            
            comments.clear()
            comments.addAll(commentList)
            commentAdapter.notifyDataSetChanged()
        }
        
        // Observar estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // TODO: Mostrar/ocultar indicador de carga
            android.util.Log.d("CommentsFragment", "Loading: $isLoading")
        }
        
        // Observar errores
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                android.util.Log.e("CommentsFragment", "Error: $it")
            }
        }
        
        // Observar comentario agregado
        viewModel.commentAdded.observe(viewLifecycleOwner) { comment ->
            comment?.let {
                Toast.makeText(context, "Comentario agregado exitosamente", Toast.LENGTH_SHORT).show()
                editTextComment.text?.clear()
                android.util.Log.d("CommentsFragment", "Comentario agregado: ${it.id}")
            }
        }
    }
    
    private fun loadComments() {
        currentPost?.let { post ->
            android.util.Log.d("CommentsFragment", "Cargando comentarios para post: ${post.id}")
            viewModel.loadComments(post.id)
        }
    }

    private fun updatePostHeader() {
        currentPost?.let { post ->
            postTitleTextView.text = "Comentarios: ${post.content.take(30)}..."
        }
    }

    private fun addComment() {
        val commentText = editTextComment.text.toString().trim()
        
        if (commentText.isEmpty()) {
            Toast.makeText(context, "Por favor escribe un comentario", Toast.LENGTH_SHORT).show()
            return
        }

        currentPost?.let { post ->
            android.util.Log.d("CommentsFragment", "Agregando comentario: $commentText")
            viewModel.addComment(post.id, commentText)
        }
    }

    private fun onEditComment(comment: Comment) {
        android.util.Log.d("CommentsFragment", "Edit comment clicked: ${comment.id}")
        showEditCommentDialog(comment)
    }
    
    private fun onDeleteComment(comment: Comment) {
        android.util.Log.d("CommentsFragment", "Delete comment clicked: ${comment.id}")
        showDeleteCommentDialog(comment)
    }
    
    private fun showEditCommentDialog(comment: Comment) {
        val editText = android.widget.EditText(requireContext()).apply {
            setText(comment.content)
            hint = "Editar comentario..."
            setPadding(48, 48, 48, 48)
            setSingleLine(false)
            maxLines = 5
        }
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Editar Comentario")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val newContent = editText.text.toString().trim()
                if (newContent.isNotEmpty() && newContent != comment.content) {
                    android.util.Log.d("CommentsFragment", "Editando comentario: ${comment.id} -> $newContent")
                    viewModel.editComment(comment, newContent)
                } else {
                    android.util.Log.d("CommentsFragment", "Sin cambios en el comentario")
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showDeleteCommentDialog(comment: Comment) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Comentario")
            .setMessage("¿Estás seguro de que deseas eliminar este comentario?\n\nEsta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteComment(comment)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    companion object {
        fun newInstance(post: Post): CommentsFragmentSimple {
            val fragment = CommentsFragmentSimple()
            val args = Bundle()
            args.putParcelable("post", post)
            fragment.arguments = args
            return fragment
        }
    }
}





