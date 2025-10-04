package com.uniway.controller;

import com.uniway.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * VerificationController - Controlador para el sistema de verificación de emails
 * 
 * Este controlador maneja los endpoints relacionados con la verificación de
 * correos electrónicos durante el proceso de registro de usuarios.
 * 
 * Endpoints disponibles:
 * - Enviar código de verificación
 * - Verificar código ingresado por el usuario
 * - Reenviar código si es necesario
 */
@RestController
@RequestMapping("/verification")
@Tag(name = "Verificación", description = "Endpoints para verificación de correos electrónicos")
@CrossOrigin(origins = "*")
public class VerificationController {
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Envía un código de verificación al email especificado
     * 
     * @param request Objeto con el email destinatario
     * @return Respuesta con estado del envío
     */
    @PostMapping("/send-code")
    @Operation(summary = "Enviar código de verificación por email")
    public ResponseEntity<?> sendVerificationCode(@RequestBody SendCodeRequest request) {
        try {
            System.out.println("=== DEBUG: Enviando código de verificación ===");
            System.out.println("Email: " + request.getEmail());
            
            // Validar formato de email institucional
            if (!request.getEmail().endsWith("@pascualbravo.edu.co")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Solo se permiten correos institucionales @pascualbravo.edu.co");
                return ResponseEntity.badRequest().body(error);
            }
            
            String code = emailService.sendVerificationCode(request.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Código de verificación enviado exitosamente");
            response.put("email", request.getEmail());
            response.put("expiresInMinutes", 10);
            
            // Solo en modo desarrollo, incluir el código en la respuesta
            if (isDevMode()) {
                response.put("devCode", code);
                response.put("devNote", "Código incluido solo en modo desarrollo");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error enviando código: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Verifica un código de verificación ingresado por el usuario
     * 
     * @param request Objeto con email y código a verificar
     * @return Respuesta indicando si el código es válido
     */
    @PostMapping("/verify-code")
    @Operation(summary = "Verificar código de verificación")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeRequest request) {
        try {
            System.out.println("=== DEBUG: Verificando código ===");
            System.out.println("Email: " + request.getEmail() + ", Código: " + request.getCode());
            
            boolean isValid = emailService.verifyCode(request.getEmail(), request.getCode());
            
            Map<String, Object> response = new HashMap<>();
            
            if (isValid) {
                response.put("valid", true);
                response.put("message", "Código verificado exitosamente");
                response.put("email", request.getEmail());
                return ResponseEntity.ok(response);
            } else {
                response.put("valid", false);
                response.put("message", "Código inválido, expirado o ya utilizado");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            System.err.println("Error verificando código: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("valid", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Reenvía un código de verificación (útil si el usuario no recibió el email)
     * 
     * @param request Objeto con el email para reenvío
     * @return Respuesta con estado del reenvío
     */
    @PostMapping("/resend-code")
    @Operation(summary = "Reenviar código de verificación")
    public ResponseEntity<?> resendVerificationCode(@RequestBody SendCodeRequest request) {
        try {
            System.out.println("=== DEBUG: Reenviando código ===");
            System.out.println("Email: " + request.getEmail());
            
            // Limpiar códigos expirados antes de generar uno nuevo
            emailService.cleanupExpiredCodes();
            
            String code = emailService.sendVerificationCode(request.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Código reenviado exitosamente");
            response.put("email", request.getEmail());
            response.put("expiresInMinutes", 10);
            
            // Solo en modo desarrollo
            if (isDevMode()) {
                response.put("devCode", code);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error reenviando código: " + e.getMessage());
            
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
    
    // ==================== CLASES DE REQUEST ====================
    
    /**
     * Clase para requests de envío de código
     */
    public static class SendCodeRequest {
        private String email;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    /**
     * Clase para requests de verificación de código
     */
    public static class VerifyCodeRequest {
        private String email;
        private String code;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
}