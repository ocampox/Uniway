package com.universidad.uniway.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.universidad.uniway.R
import com.universidad.uniway.databinding.FragmentMyRecommendationsBinding
import com.universidad.uniway.ui.ViewModelFactory
import com.universidad.uniway.data.TeacherRecommendation
class MyRecommendationsFragment : Fragment() {

    private var _binding: FragmentMyRecommendationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MyRecommendationsViewModel
    private lateinit var adapter: TeacherRecommendationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyRecommendationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)[MyRecommendationsViewModel::class.java]

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        // Cargar mis recomendaciones
        viewModel.loadMyRecommendations()
    }

    private fun setupRecyclerView() {
        adapter = TeacherRecommendationAdapter(
            onRemoveClick = { recommendation ->
                showDeleteConfirmationDialog(recommendation)
            },
            onLikeClick = { recommendation ->
                // En "Mis Recomendaciones" no se puede dar like a las propias
                android.widget.Toast.makeText(requireContext(), "No puedes dar like a tus propias recomendaciones", android.widget.Toast.LENGTH_SHORT).show()
            },
            onDislikeClick = { recommendation ->
                // En "Mis Recomendaciones" no se puede dar dislike a las propias
                android.widget.Toast.makeText(requireContext(), "No puedes dar dislike a tus propias recomendaciones", android.widget.Toast.LENGTH_SHORT).show()
            },
            showOwnActions = true // Mostrar botón eliminar para propias recomendaciones
        )

        binding.recyclerViewMyRecommendations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MyRecommendationsFragment.adapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddRecommendation.setOnClickListener {
            findNavController().navigate(R.id.action_my_recommendations_to_add_teacher)
        }
    }

    private fun observeViewModel() {
        viewModel.myRecommendations.observe(viewLifecycleOwner) { recommendations ->
            if (recommendations.isEmpty()) {
                binding.recyclerViewMyRecommendations.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
            } else {
                binding.recyclerViewMyRecommendations.visibility = View.VISIBLE
                binding.layoutEmptyState.visibility = View.GONE
                adapter.submitList(recommendations)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // TODO: Mostrar/ocultar loading indicator
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
            }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmationDialog(recommendation: TeacherRecommendation) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_confirmation, null)
        val messageText = dialogView.findViewById<android.widget.TextView>(R.id.textMessage)
        val buttonCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonCancel)
        val buttonDelete = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonDelete)
        
        messageText.text = "¿Estás seguro de que deseas eliminar la recomendación del profesor ${recommendation.teacherName}?\n\nEsta acción no se puede deshacer."
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        buttonDelete.setOnClickListener {
            dialog.dismiss()
            viewModel.removeRecommendation(recommendation)
        }
        
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}