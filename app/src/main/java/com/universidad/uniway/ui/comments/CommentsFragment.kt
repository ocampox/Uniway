package com.universidad.uniway.ui.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.universidad.uniway.R
import com.universidad.uniway.data.Post
import com.universidad.uniway.data.Comment
import com.universidad.uniway.databinding.FragmentCommentsBinding
import com.universidad.uniway.ui.ViewModelFactory

class CommentsFragment : Fragment() {

    private var _binding: FragmentCommentsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: CommentsViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory(requireContext()))[CommentsViewModel::class.java]
    }
    
    private lateinit var commentAdapter: CommentAdapter
    private var currentPost: Post? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Obtener el post desde los argumentos de navegación
        currentPost = arguments?.getParcelable("post")
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        loadComments()
        updatePostHeader()
    }

    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter()
        
        binding.recyclerViewComments.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = commentAdapter
        }
    }

    private fun setupObservers() {
        viewModel.comments.observe(viewLifecycleOwner, Observer { comments ->
            commentAdapter.updateComments(comments)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            // Aquí podrías mostrar un progress bar si lo necesitas
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        })

        viewModel.commentAdded.observe(viewLifecycleOwner, Observer { comment ->
            comment?.let {
                commentAdapter.addComment(it)
                binding.editTextComment.text?.clear()
                Toast.makeText(context, "Comentario agregado", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupClickListeners() {
        binding.buttonAddComment.setOnClickListener {
            addComment()
        }
    }

    private fun loadComments() {
        currentPost?.let { post ->
            viewModel.loadComments(post.id)
        }
    }

    private fun updatePostHeader() {
        currentPost?.let { post ->
            binding.textViewPostAuthor.text = post.authorName
            binding.textViewPostContent.text = post.content
            binding.textViewPostLikes.text = "👍 ${post.likeCount}"
            binding.textViewPostDislikes.text = "👎 ${post.dislikeCount}"
            binding.textViewPostComments.text = "💬 ${post.commentCount}"
        }
    }

    private fun addComment() {
        val commentText = binding.editTextComment.text.toString().trim()
        
        if (commentText.isEmpty()) {
            Toast.makeText(context, "Por favor escribe un comentario", Toast.LENGTH_SHORT).show()
            return
        }

        currentPost?.let { post ->
            viewModel.addComment(post.id, commentText)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(post: Post): CommentsFragment {
            val fragment = CommentsFragment()
            val args = Bundle()
            args.putParcelable("post", post)
            fragment.arguments = args
            return fragment
        }
    }
}
