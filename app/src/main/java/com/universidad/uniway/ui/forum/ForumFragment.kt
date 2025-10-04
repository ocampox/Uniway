package com.universidad.uniway.ui.forum

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.universidad.uniway.R
import com.universidad.uniway.data.Post
import com.universidad.uniway.data.PostType
import com.universidad.uniway.data.PostPriority
import com.universidad.uniway.data.UserRole
import com.universidad.uniway.databinding.FragmentForumBinding
import com.universidad.uniway.ui.ViewModelFactory

class ForumFragment : Fragment() {

    private var _binding: FragmentForumBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ForumViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory(requireContext()))[ForumViewModel::class.java]
    }
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        loadPosts()
    }

    private fun setupRecyclerView() {
        val tokenManager = com.universidad.uniway.network.TokenManager(requireContext())
        
        postAdapter = PostAdapter(
            posts = mutableListOf(),
            currentUserRole = try { 
                UserRole.valueOf(tokenManager.getUserRole() ?: "STUDENT") 
            } catch (e: Exception) { 
                UserRole.STUDENT 
            },
            getCurrentUserId = { tokenManager.getUserId() },
            onPostClick = { post -> onPostClick(post) },
            onLikeClick = { post -> onLikeClick(post) },
            onDislikeClick = { post -> onDislikeClick(post) },
            onCommentClick = { post -> onCommentClick(post) },

            onEditClick = { post -> onEditClick(post) },
            onDeleteClick = { post -> onDeleteClick(post) }
        )
        
        binding.recyclerViewPosts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postAdapter
        }
    }

    private fun setupObservers() {
        viewModel.posts.observe(viewLifecycleOwner, Observer { posts ->
            postAdapter.updatePosts(posts)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                
                // Si hay error de conexión, mostrar opción de recargar
                if (it.contains("Error de conexión")) {
                    Toast.makeText(context, "Toca el botón + para recargar posts", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun setupClickListeners() {
        binding.fabNewPost.setOnClickListener {
            showCreatePostDialog()
        }
        
        // Agregar funcionalidad de recarga con long press
        binding.fabNewPost.setOnLongClickListener {
            Toast.makeText(context, "Recargando posts desde el servidor...", Toast.LENGTH_SHORT).show()
            viewModel.forceReloadFromApi()
            true
        }
    }

    private fun loadPosts() {
        viewModel.loadPosts()
    }

    private fun showCreatePostDialog() {
        val dialog = CreatePostDialog.newInstance { content, postType, priority ->
            viewModel.createPost(content, postType, priority)
            Toast.makeText(context, "Publicación creada exitosamente", Toast.LENGTH_SHORT).show()
        }
        dialog.show(parentFragmentManager, "CreatePostDialog")
    }

    private fun onPostClick(post: Post) {
        // TODO: Abrir detalle del post
        Toast.makeText(context, "Ver post: ${post.content.take(20)}...", Toast.LENGTH_SHORT).show()
    }

    private fun onLikeClick(post: Post) {
        viewModel.likePost(post)
    }

    private fun onDislikeClick(post: Post) {
        viewModel.dislikePost(post)
    }

    private fun onCommentClick(post: Post) {
        // Abrir fragmento de comentarios simplificado
        val commentsFragment = com.universidad.uniway.ui.comments.CommentsFragmentSimple.newInstance(post)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, commentsFragment)
            .addToBackStack("comments")
            .commit()
    }



    private fun onEditClick(post: Post) {
        if (!viewModel.canEditPost(post)) {
            Toast.makeText(context, "No tienes permisos para editar este post", Toast.LENGTH_SHORT).show()
            return
        }
        
        showEditPostDialog(post)
    }

    private fun onDeleteClick(post: Post) {
        if (!viewModel.canDeletePost(post)) {
            Toast.makeText(context, "No tienes permisos para eliminar este post", Toast.LENGTH_SHORT).show()
            return
        }
        
        showDeleteConfirmationDialog(post)
    }
    
    private fun showEditPostDialog(post: Post) {
        val dialog = EditPostDialog.newInstance(post) { newContent, newPostType ->
            viewModel.editPost(post, newContent, newPostType)
            Toast.makeText(context, "Post actualizado exitosamente", Toast.LENGTH_SHORT).show()
        }
        dialog.show(parentFragmentManager, "EditPostDialog")
    }
    
    private fun showDeleteConfirmationDialog(post: Post) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_confirmation, null)
        val messageText = dialogView.findViewById<android.widget.TextView>(R.id.textMessage)
        val buttonCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonCancel)
        val buttonDelete = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonDelete)
        
        messageText.text = "¿Estás seguro de que deseas eliminar esta publicación?\n\nEsta acción no se puede deshacer."
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        buttonDelete.setOnClickListener {
            dialog.dismiss()
            viewModel.deletePost(post)
            Toast.makeText(context, "Post eliminado exitosamente", Toast.LENGTH_SHORT).show()
        }
        
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

