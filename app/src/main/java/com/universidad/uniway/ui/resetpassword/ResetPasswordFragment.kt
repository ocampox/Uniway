package com.universidad.uniway.ui.resetpassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.universidad.uniway.R
import com.universidad.uniway.databinding.FragmentResetPasswordBinding
import com.universidad.uniway.ui.ViewModelFactory

class ResetPasswordFragment : Fragment() {

    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ResetPasswordViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory(requireContext()))[ResetPasswordViewModel::class.java]
    }

    private var email: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Obtener email desde argumentos
        email = arguments?.getString("email")
        
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.resetPasswordResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ResetPasswordResult.Success -> {
                    Toast.makeText(context, "Contraseña restablecida exitosamente", Toast.LENGTH_SHORT).show()
                    // Navegar de vuelta al login
                    findNavController().navigate(R.id.action_reset_password_to_login)
                }
                is ResetPasswordResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.buttonResetPassword.isEnabled = !isLoading
            binding.buttonResetPassword.text = if (isLoading) "Restableciendo..." else "Restablecer Contraseña"
        }
    }

    private fun setupClickListeners() {
        binding.buttonResetPassword.setOnClickListener {
            resetPassword()
        }

        binding.textViewBackToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_reset_password_to_login)
        }
    }

    private fun resetPassword() {
        val code = binding.editTextCode.text.toString().trim()
        val newPassword = binding.editTextNewPassword.text.toString()
        val confirmPassword = binding.editTextConfirmPassword.text.toString()
        
        when {
            code.isBlank() -> {
                Toast.makeText(context, "Por favor ingresa el código", Toast.LENGTH_SHORT).show()
                return
            }
            newPassword.isBlank() -> {
                Toast.makeText(context, "Por favor ingresa la nueva contraseña", Toast.LENGTH_SHORT).show()
                return
            }
            newPassword.length < 6 -> {
                Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return
            }
            newPassword != confirmPassword -> {
                Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return
            }
        }

        email?.let { emailAddress ->
            viewModel.resetPassword(emailAddress, code, newPassword)
        } ?: run {
            Toast.makeText(context, "Error: Email no encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
