package com.uniway.controller;

import com.uniway.dto.UserDto;
import com.uniway.entity.User;
import com.uniway.entity.UserRole;
import com.uniway.service.AuthService;
import com.uniway.service.UserService;
import com.uniway.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Endpoints para registro e inicio de sesión")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailService emailService;
    
    @PostMapping("/register")
    @Operation(summary = "Iniciar proceso de registro - Enviar código de verificación")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Validar que el email sea institucional
            if (!request.getEmail().endsWith("@pascualbravo.edu.co")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Solo se permiten correos institucionales @pascualbravo.edu.co");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Verificar si el usuario ya existe
            if (userService.findByEmail(request.getEmail()).isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El correo electrónico ya está registrado");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Enviar código de verificación
            String code = emailService.sendVerificationCode(request.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Código de verificación enviado a tu correo institucional");
            response.put("email", request.getEmail());
            response.put("expiresInMinutes", 10);
            response.put("requiresVerification", true);
            
            // Solo en modo desarrollo, incluir el código
            if (isDevMode()) {
                response.put("devCode", code);
                response.put("devNote", "Código incluido solo en modo desarrollo");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/complete-registration")
    @Operation(summary = "Completar registro después de verificar email")
    public ResponseEntity<?> completeRegistration(@Valid @RequestBody CompleteRegistrationRequest request) {
        try {
            // Verificar el código de verificación
            
            
            // Crear el usuario
            User user = authService.register(
                request.getEmail(),
                request.getPassword(),
                request.getRole(),
                request.getFullName(),
                request.getStudentId(),
                request.getProgram()
            );
            
            UserDto userDto = userService.convertToDto(user);
            String token = authService.generateToken(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("user", userDto);
            response.put("token", token);
            response.put("message", "Usuario registrado exitosamente");
            response.put("emailVerified", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = authService.authenticate(request.getEmail(), request.getPassword());
            UserDto userDto = userService.convertToDto(user);
            String token = authService.generateToken(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("user", userDto);
            response.put("token", token);
            response.put("message", "Inicio de sesión exitoso");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/forgot-password")
    @Operation(summary = "Enviar código para recuperar contraseña")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            // Validar que el email sea institucional
            if (!request.getEmail().endsWith("@pascualbravo.edu.co")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Solo se permiten correos institucionales @pascualbravo.edu.co");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Verificar si el usuario existe
            if (!userService.findByEmail(request.getEmail()).isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No existe una cuenta con este correo electrónico");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Enviar código de recuperación
            String code = emailService.sendPasswordResetCode(request.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Código de recuperación enviado a tu correo institucional");
            response.put("email", request.getEmail());
            response.put("expiresInMinutes", 10);
            response.put("requiresVerification", true);
            
            // Solo en modo desarrollo, incluir el código
            if (isDevMode()) {
                response.put("devCode", code);
                response.put("devNote", "Código incluido solo en modo desarrollo");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña con código")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            // Verificar el código de verificación
            if (!emailService.verifyCode(request.getEmail(), request.getCode())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Código de verificación inválido o expirado");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Buscar el usuario
            User user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            // Actualizar la contraseña
            authService.updatePassword(user, request.getNewPassword());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Contraseña restablecida exitosamente");
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // ==================== MÉTODOS DE UTILIDAD ====================
    
    /**
     * Verifica si la aplicación está en modo desarrollo
     * @return true si está en modo desarrollo
     */
    private boolean isDevMode() {
        // En producción, cambiar esto a false o usar profiles de Spring
        return true;
    }
    
    // Clases internas para requests
    public static class RegisterRequest {
        private String email;
        private String password;
        private UserRole role;
        private String fullName;
        private String studentId;
        private String program;
        
        // Getters y Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public UserRole getRole() { return role; }
        public void setRole(UserRole role) { this.role = role; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        
        public String getProgram() { return program; }
        public void setProgram(String program) { this.program = program; }
    }
    
    public static class CompleteRegistrationRequest {
        private String email;
        private String password;
        private UserRole role;
        private String fullName;
        private String studentId;
        private String program;
        private String verificationCode;
        
        // Getters y Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public UserRole getRole() { return role; }
        public void setRole(UserRole role) { this.role = role; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        
        public String getProgram() { return program; }
        public void setProgram(String program) { this.program = program; }
        
        public String getVerificationCode() { return verificationCode; }
        public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
    }
    
    public static class LoginRequest {
        private String email;
        private String password;
        
        // Getters y Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class ForgotPasswordRequest {
        private String email;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    public static class ResetPasswordRequest {
        private String email;
        private String code;
        private String newPassword;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}





