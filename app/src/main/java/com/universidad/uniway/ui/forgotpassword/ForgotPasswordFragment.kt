package com.universidad.uniway.ui.forgotpassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.universidad.uniway.R
import com.universidad.uniway.databinding.FragmentForgotPasswordBinding
import com.universidad.uniway.ui.ViewModelFactory

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ForgotPasswordViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory(requireContext()))[ForgotPasswordViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.forgotPasswordResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ForgotPasswordResult.Success -> {
                    Toast.makeText(context, "Código enviado a tu correo", Toast.LENGTH_SHORT).show()
                    // Navegar al fragmento de reset password
                    val bundle = Bundle().apply {
                        putString("email", result.email)
                    }
                    findNavController().navigate(R.id.action_forgot_password_to_reset_password, bundle)
                }
                is ForgotPasswordResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.buttonSendCode.isEnabled = !isLoading
            binding.buttonSendCode.text = if (isLoading) "Enviando..." else "Enviar Código"
        }
    }

    private fun setupClickListeners() {
        binding.buttonSendCode.setOnClickListener {
            sendResetCode()
        }

        binding.textViewBackToLogin.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun sendResetCode() {
        val email = binding.editTextEmail.text.toString().trim()
        
        if (email.isBlank()) {
            Toast.makeText(context, "Por favor ingresa tu correo", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.sendResetCode(email)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
