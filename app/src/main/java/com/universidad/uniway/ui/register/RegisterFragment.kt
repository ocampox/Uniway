package com.universidad.uniway.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.universidad.uniway.R
import com.universidad.uniway.data.UserRole
import com.universidad.uniway.databinding.FragmentRegisterBinding
import com.universidad.uniway.ui.ViewModelFactory

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: RegisterViewModel by lazy {
        ViewModelProvider(requireActivity(), ViewModelFactory(requireContext()))[RegisterViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        setupClickListeners()
        setupInputValidations()
    }

    private fun setupClickListeners() {
        binding.buttonRegister.setOnClickListener {
            registerUser()
        }

        binding.textViewGoToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }
    }

    private fun setupInputValidations() {
        // Validación en tiempo real para mejorar UX
        binding.editTextConfirmPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validatePasswordMatch()
            }
        }
    }

    private fun registerUser() {
        val fullName = binding.editTextFullName.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString()
        val confirmPassword = binding.editTextConfirmPassword.text.toString()
        
        // Validaciones locales
        if (!validateInputs(fullName, email, password, confirmPassword)) {
            return
        }
        
        val selectedRole = getSelectedRole()
        if (selectedRole == null) {
            Toast.makeText(context, "Por favor selecciona un cargo", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.sendVerificationCode(fullName, email, password, selectedRole)
    }

    private fun validateInputs(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            fullName.isEmpty() -> {
                binding.editTextFullName.error = "El nombre completo es requerido"
                false
            }
            email.isEmpty() || email.contains("@") && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.editTextEmail.error = "Ingresa un email válido"
                false
            }
            password.length < 6 -> {
                binding.editTextPassword.error = "La contraseña debe tener al menos 6 caracteres"
                false
            }
            password != confirmPassword -> {
                binding.editTextConfirmPassword.error = "Las contraseñas no coinciden"
                false
            }
            else -> true
        }
    }

    private fun validatePasswordMatch(): Boolean {
        val password = binding.editTextPassword.text.toString()
        val confirmPassword = binding.editTextConfirmPassword.text.toString()
        
        val isValid = password.isNotEmpty() && password == confirmPassword
        if (!isValid && confirmPassword.isNotEmpty()) {
            binding.editTextConfirmPassword.error = "Las contraseñas no coinciden"
        } else {
            binding.editTextConfirmPassword.error = null
        }
        return isValid
    }

    private fun getSelectedRole(): UserRole? {
        return when (binding.radioGroupRole.checkedRadioButtonId) {
            R.id.radioStudent -> UserRole.STUDENT
            else -> null
        }
    }

    private fun setupObservers() {
        viewModel.codeSentResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is CodeSentResult.Success -> {
                    // Navegar a la pantalla de verificación con todos los datos
                    navigateToEmailVerification(result.email)
                }
                is CodeSentResult.Error -> {
                    showError(result.message)
                    enableRegisterButton()
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        }
        
        viewModel.verificationResult.observe(viewLifecycleOwner) { result ->
            // Este observer podría no ser necesario aquí, pero lo dejamos por si acaso
            when (result) {
                is VerificationResult.Error -> {
                    showError(result.message)
                }
                else -> {
                    // No hacemos nada con success/loading aquí
                }
            }
        }
    }

    private fun navigateToEmailVerification(email: String) {
        val fullName = binding.editTextFullName.text.toString().trim()
        val password = binding.editTextPassword.text.toString()
        val role = getSelectedRole() ?: UserRole.STUDENT
        
        val bundle = Bundle().apply {
            putString("email", email)
            putString("fullName", fullName)
            putString("password", password)
            putString("role", role.name)
        }
        
        findNavController().navigate(R.id.nav_email_verification, bundle)
    }

    private fun showLoading() {
        binding.buttonRegister.isEnabled = false
        binding.buttonRegister.text = "Enviando código..."

    }

    private fun hideLoading() {
        binding.buttonRegister.isEnabled = true
        binding.buttonRegister.text = "Registrarse"

    }

    private fun enableRegisterButton() {
        binding.buttonRegister.isEnabled = true
        binding.buttonRegister.text = "Registrarse"
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}