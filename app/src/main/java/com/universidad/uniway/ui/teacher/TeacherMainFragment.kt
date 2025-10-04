package com.universidad.uniway.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.universidad.uniway.R
import com.universidad.uniway.databinding.FragmentTeacherMainBinding
import com.universidad.uniway.ui.ViewModelFactory

class TeacherMainFragment : Fragment() {

    private var _binding: FragmentTeacherMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TeacherMainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)[TeacherMainViewModel::class.java]

        setupClickListeners()
        observeViewModel()
        loadStatistics()
    }

    private fun setupClickListeners() {
        // Botón para agregar nueva recomendación
        binding.fabAddRecommendation.setOnClickListener {
            findNavController().navigate(R.id.action_teacher_main_to_add_teacher)
        }

        // Card para ver mis recomendaciones
        binding.cardMyRecommendations.setOnClickListener {
            findNavController().navigate(R.id.action_teacher_main_to_my_recommendations)
        }

        // Card para ver todas las recomendaciones
        binding.cardAllRecommendations.setOnClickListener {
            findNavController().navigate(R.id.action_teacher_main_to_all_recommendations)
        }
    }

    private fun observeViewModel() {
        viewModel.myRecommendationsCount.observe(viewLifecycleOwner) { count ->
            binding.textMyRecommendationsCount.text = count.toString()
        }

        viewModel.totalRecommendationsCount.observe(viewLifecycleOwner) { count ->
            binding.textTotalRecommendationsCount.text = count.toString()
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadStatistics() {
        viewModel.loadStatistics()
    }

    override fun onResume() {
        super.onResume()
        // Recargar estadísticas cuando se regrese a este fragmento
        loadStatistics()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}