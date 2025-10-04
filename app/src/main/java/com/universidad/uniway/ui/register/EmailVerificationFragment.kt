package com.universidad.uniway.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.universidad.uniway.R
import com.universidad.uniway.databinding.FragmentEmailVerificationBinding
import com.universidad.uniway.data.UserRole
import com.universidad.uniway.ui.ViewModelFactory

/**
 * EmailVerificationFragment - Pantalla de verificación de email
 * 
 * Este fragment maneja la verificación del código de verificación
 * enviado por email durante el proceso de registro.
 */
class EmailVerificationFragment : Fragment() {
    
    private var _binding: FragmentEmailVerificationBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: RegisterViewModel by lazy {
        ViewModelProvider(requireActivity(), ViewModelFactory(requireContext()))[RegisterViewModel::class.java]
    }
    
    // Variables para almacenar los datos del registro
    private lateinit var email: String
    private lateinit var fullName: String
    private lateinit var password: String
    private lateinit var role: UserRole
    
    // ⚠️ AÑADIR: Variables de control para prevenir múltiples llamadas
    private var isVerificationInProgress = false
    private var isRegistrationInProgress = false
    
    
    companion object {
        private const val ARG_EMAIL = "email"
        private const val ARG_FULL_NAME = "fullName"
        private const val ARG_PASSWORD = "password"
        private const val ARG_ROLE = "role"
        
        fun newInstance(
            email: String,
            fullName: String,
            password: String,
            role: UserRole
        ): EmailVerificationFragment {
            val fragment = EmailVerificationFragment()
            val args = Bundle().apply {
                putString(ARG_EMAIL, email)
                putString(ARG_FULL_NAME, fullName)
                putString(ARG_PASSWORD, password)
                putString(ARG_ROLE, role.name)
            }
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Obtener y validar los argumentos
        if (!validateArguments()) {
            showError("Datos incompletos. Por favor, complete el registro nuevamente.")
            findNavController().navigateUp()
            return
        }
        
        setupUI()
        setupObservers()
    }
    
    private fun validateArguments(): Boolean {
        val args = arguments ?: return false
        
        email = args.getString(ARG_EMAIL) ?: return false
        fullName = args.getString(ARG_FULL_NAME) ?: ""
        password = args.getString(ARG_PASSWORD) ?: ""
        
        role = when (args.getString(ARG_ROLE)) {
            "STUDENT" -> UserRole.STUDENT
            "ADMINISTRATION" -> UserRole.ADMINISTRATION
            else -> UserRole.STUDENT
        }
        
        return email.isNotEmpty()
    }
    
    private fun setupUI() {
        // Mostrar el email en la pantalla
        binding.textViewEmail.text = email
        
        // Configurar botones
        binding.buttonVerify.setOnClickListener {
            verifyCode()
        }
        
        binding.buttonResendCode.setOnClickListener {
            resendCode()
        }
        
        binding.textViewBackToRegister.setOnClickListener {
            findNavController().navigateUp()
        }
        
        // Auto-focus en el campo de código
        binding.editTextVerificationCode.requestFocus()
        
        // Configurar listener para auto-verificar cuando se ingrese el código completo
        setupCodeInputListener()
    }
    
    private fun setupCodeInputListener() {
    binding.editTextVerificationCode.addTextChangedListener(object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {
            // ⚠️ DESACTIVADO: No auto-verificar automáticamente
            // El usuario debe presionar el botón manualmente
        }
    })
}
    
    private fun setupObservers() {
        viewModel.verificationResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is VerificationResult.Success -> {
                    isVerificationInProgress = false
                    showSuccess("Código verificado correctamente")

                     if (!isRegistrationInProgress) {
                    isRegistrationInProgress = true
                    
                    // Pequeño delay para que el usuario vea el mensaje de éxito
                    binding.root.postDelayed({
                        completeRegistration()
                    }, 1000) // 1 segundo de delay
                }

                    
                    // ⚠️ PREVENIR llamada múltiple a completeRegistration
                    if (!isRegistrationInProgress) {
                        isRegistrationInProgress = true
                        completeRegistration()
                    }
                }
                is VerificationResult.Error -> {
                    isVerificationInProgress = false
                    isRegistrationInProgress = false
                    showError("Error: ${result.message}")
                    enableButtons()
                }
                is VerificationResult.Loading -> {
                    isVerificationInProgress = true
                    disableButtons()
                }
            }
        }
        
        viewModel.registrationResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is RegistrationResult.Success -> {
                    isRegistrationInProgress = false
                    showSuccess("¡Registro completado exitosamente!")
                    navigateToLogin()
                }
                is RegistrationResult.Error -> {
                    isRegistrationInProgress = false
                    showError("Error en registro: ${result.message}")
                    enableButtons()
                    
                    // ⚠️ RESETEAR estado si hay error de verificación
                                   }

            }
        }
        
        viewModel.codeSentResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is CodeSentResult.Success -> {
                    showSuccess("Código reenviado exitosamente")
                    enableButtons()
                }
                is CodeSentResult.Error -> {
                    showError("Error al reenviar: ${result.message}")
                    enableButtons()
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
    }
    private var lastVerificationAttempt = 0L
    private fun verifyCode() {
        // ⚠️ PREVENIR múltiples clics rápidos
        if (isVerificationInProgress) {
            return
        }
        

        
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastVerificationAttempt < 2000) {
            showError("Espera un momento antes de intentar nuevamente")
            return
        }
        lastVerificationAttempt = currentTime
        
        val code = binding.editTextVerificationCode.text.toString().trim()
        
        if (!isValidVerificationCode(code)) {
            return
        }
        
        println("DEBUG: Verificando código: $code para email: $email")
        viewModel.verifyCode(email, code)
    }
    
    private fun isValidVerificationCode(code: String): Boolean {
        return when {
            code.isEmpty() -> {
                binding.editTextVerificationCode.error = "Por favor ingresa el código de verificación"
                false
            }
            code.length != 6 -> {
                binding.editTextVerificationCode.error = "El código debe tener 6 dígitos"
                false
            }
            !code.matches(Regex("\\d+")) -> {
                binding.editTextVerificationCode.error = "El código debe contener solo números"
                false
            }
            else -> {
                binding.editTextVerificationCode.error = null
                true
            }
        }
    }
    
    private fun completeRegistration() {
        val verificationCode = binding.editTextVerificationCode.text.toString().trim()
        println("DEBUG: Completando registro con código: $verificationCode")
        viewModel.completeRegistration(fullName, email, password, role, verificationCode)
    }
    
    private fun resendCode() {
        // ⚠️ RESETEAR estado de verificación al reenviar código

        isVerificationInProgress = false
        isRegistrationInProgress = false
        
        println("DEBUG: Reenviando código a: $email")
        viewModel.resendVerificationCode(email)
    }
    
    private fun showLoading() {
        binding.buttonVerify.isEnabled = false
        binding.buttonResendCode.isEnabled = false
    }
    
    private fun hideLoading() {
        binding.buttonVerify.isEnabled = true
        binding.buttonResendCode.isEnabled = true
    }
    
    private fun disableButtons() {
        binding.buttonVerify.isEnabled = false
        binding.buttonResendCode.isEnabled = false
    }
    
    private fun enableButtons() {
        binding.buttonVerify.isEnabled = true
        binding.buttonResendCode.isEnabled = true
    }
    
    private fun navigateToLogin() {
        findNavController().navigate(R.id.nav_login)
    }
    
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        println("ERROR: $message")
    }
    
    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        println("SUCCESS: $message")
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}