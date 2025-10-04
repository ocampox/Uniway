package com.uniway.service;

import com.uniway.entity.VerificationCode;
import com.uniway.repository.VerificationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * EmailService - Servicio para envío de emails y gestión de códigos de verificación
 * 
 * Este servicio maneja todo el sistema de verificación de correos electrónicos
 * durante el proceso de registro de usuarios.
 * 
 * Funcionalidades:
 * - Generación de códigos de verificación aleatorios
 * - Envío de emails con códigos de verificación
 * - Validación de códigos ingresados por usuarios
 * - Limpieza automática de códigos expirados
 * - Control de límites de envío para prevenir spam
 * 
 * Configuración de seguridad:
 * - Códigos de 6 dígitos numéricos
 * - Expiración automática en 10 minutos
 * - Máximo 3 códigos válidos por email
 * - Un solo uso por código
 */
@Service
@Transactional
public class EmailService {
    
    // ==================== CONSTANTES DE CONFIGURACIÓN ====================
    
    /** Duración de validez de los códigos en minutos */
    private static final int CODE_EXPIRATION_MINUTES = 10;
    
    /** Máximo número de códigos válidos por email */
    private static final int MAX_CODES_PER_EMAIL = 3;
    
    /** Longitud del código de verificación */
    private static final int CODE_LENGTH = 6;
    
    // ==================== DEPENDENCIAS ====================
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private VerificationCodeRepository verificationCodeRepository;
    
    // ==================== MÉTODOS PRINCIPALES ====================
    
    /**
     * Genera y envía un código de verificación al email especificado
     * 
     * @param email Email destinatario (debe ser @pascualbravo.edu.co)
     * @return Código generado (solo para testing, en producción no se devuelve)
     * @throws RuntimeException si hay demasiados códigos activos o error de envío
     */
    public String sendVerificationCode(String email) {
        System.out.println("=== DEBUG: Enviando código de verificación ===");
        System.out.println("Email: " + email);
        
        // Verificar límite de códigos activos
        long activeCodesCount = verificationCodeRepository.countValidCodesByEmail(email, LocalDateTime.now());
        if (activeCodesCount >= MAX_CODES_PER_EMAIL) {
            throw new RuntimeException("Demasiados códigos de verificación activos. Espera 10 minutos antes de solicitar otro.");
        }
        
        // Generar código aleatorio de 6 dígitos
        String code = generateVerificationCode();
        
        // Crear registro en base de datos
        VerificationCode verificationCode = new VerificationCode(
            UUID.randomUUID().toString(),
            email, 
            code, 
            LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES)
        );
        
        // DEBUG COMPLETO
        System.out.println("=== DEBUG COMPLETO DEL CÓDIGO ===");
        System.out.println("Antes de guardar:");
        System.out.println("  - ID: " + verificationCode.getId());
        System.out.println("  - Email: " + verificationCode.getEmail());
        System.out.println("  - Código: " + verificationCode.getCode());
        System.out.println("  - isUsed: " + verificationCode.getIsUsed());
        System.out.println("  - isUsed == null: " + (verificationCode.getIsUsed() == null));
        System.out.println("  - Expira: " + verificationCode.getExpiresAt());
        
        verificationCodeRepository.save(verificationCode);
        
        // Verificar en base de datos
        VerificationCode savedCode = verificationCodeRepository.findByEmailAndCode(email, code)
            .orElseThrow(() -> new RuntimeException("Error guardando código"));
        
        System.out.println("Después de guardar (desde BD):");
        System.out.println("  - isUsed: " + savedCode.getIsUsed());
        System.out.println("  - isUsed == null: " + (savedCode.getIsUsed() == null));
        
        if (savedCode.getIsUsed()) {
            System.out.println("❌❌❌ ALERTA: El código se guardó como USADO");
        } else {
            System.out.println("✅ Código guardado correctamente como NO USADO");
        }
        
        // Enviar email
        try {
            sendVerificationEmail(email, code);
            System.out.println("Email enviado exitosamente a: " + email);
        } catch (Exception e) {
            System.err.println("Error enviando email: " + e.getMessage());
            // En desarrollo, no fallar si no se puede enviar email
            System.out.println("MODO DESARROLLO: Código generado: " + code);
        }
        
        return code; // Solo para testing, en producción no devolver el código
    }
    
    /**
     * Genera y envía un código de recuperación de contraseña al email especificado
     * 
     * @param email Email destinatario (debe ser @pascualbravo.edu.co)
     * @return Código generado (solo para testing, en producción no se devuelve)
     * @throws RuntimeException si hay demasiados códigos activos o error de envío
     */
    public String sendPasswordResetCode(String email) {
        System.out.println("=== DEBUG: Enviando código de recuperación de contraseña ===");
        System.out.println("Email: " + email);
        
        // Verificar límite de códigos activos
        long activeCodesCount = verificationCodeRepository.countValidCodesByEmail(email, LocalDateTime.now());
        if (activeCodesCount >= MAX_CODES_PER_EMAIL) {
            throw new RuntimeException("Demasiados códigos de recuperación activos. Espera 10 minutos antes de solicitar otro.");
        }
        
        // Generar código aleatorio de 6 dígitos
        String code = generateVerificationCode();
        
        // Crear registro en base de datos
        VerificationCode verificationCode = new VerificationCode(
            UUID.randomUUID().toString(),
            email, 
            code, 
            LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES)
        );
        
        verificationCodeRepository.save(verificationCode);
        
        // Enviar email
        try {
            sendPasswordResetEmail(email, code);
            System.out.println("Email de recuperación enviado exitosamente a: " + email);
        } catch (Exception e) {
            System.err.println("Error enviando email de recuperación: " + e.getMessage());
            // En desarrollo, no fallar si no se puede enviar email
            System.out.println("MODO DESARROLLO: Código de recuperación generado: " + code);
        }
        
        return code; // Solo para testing, en producción no devolver el código
    }
    
    /**
     * Verifica un código de verificación ingresado por el usuario
     * 
     * @param email Email del usuario
     * @param inputCode Código ingresado por el usuario
     * @return true si el código es válido, false en caso contrario
     */
    public boolean verifyCode(String email, String inputCode) {
        System.out.println("=== DEBUG: Verificando código ===");
        System.out.println("Email: " + email + ", Código: " + inputCode);
        
        // Buscar código en la base de datos
        Optional<VerificationCode> codeOpt = verificationCodeRepository.findByEmailAndCode(email, inputCode);
        
        if (!codeOpt.isPresent()) {
            System.out.println("❌ Código no encontrado en la base de datos");
            return false;
        }
        
        VerificationCode verificationCode = codeOpt.get();
        
        // ⚠️ CORREGIR: Verificar manualmente en lugar de usar isValid()
        System.out.println("DEBUG - Código encontrado:");
        System.out.println("  - Usado: " + verificationCode.getIsUsed());
        System.out.println("  - Expira: " + verificationCode.getExpiresAt());
        System.out.println("  - Ahora: " + LocalDateTime.now());
        System.out.println("  - Está expirado: " + verificationCode.getExpiresAt().isBefore(LocalDateTime.now()));
        
        // Verificación manual y explícita
        if (verificationCode.getIsUsed()) {
            System.out.println("❌ Código ya fue utilizado");
            return false;
        }
        
        if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            System.out.println("❌ Código expirado");
            return false;
        }
        
        // Marcar código como usado
        verificationCode.setIsUsed(true);
        verificationCodeRepository.save(verificationCode);
        
        System.out.println("✅ Código verificado exitosamente");
        return true;
    }
    
    /**
     * Limpia códigos expirados de la base de datos
     * Este método puede ejecutarse periódicamente para mantener la BD limpia
     */
    public void cleanupExpiredCodes() {
        System.out.println("=== Limpiando códigos expirados ===");
        
        List<VerificationCode> expiredCodes = verificationCodeRepository.findExpiredCodes(LocalDateTime.now());
        if (!expiredCodes.isEmpty()) {
            verificationCodeRepository.deleteAll(expiredCodes);
            System.out.println("Códigos expirados eliminados: " + expiredCodes.size());
        } else {
            System.out.println("No hay códigos expirados para eliminar");
        }
    }
    
    // ==================== MÉTODOS PRIVADOS ====================
    
    /**
     * Genera un código de verificación aleatorio de 6 dígitos
     * @return Código numérico de 6 dígitos como String
     */
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Genera número entre 100000 y 999999
        return String.valueOf(code);
    }
    
    /**
     * Envía el email con el código de verificación
     * @param email Email destinatario
     * @param code Código de verificación
     */
    private void sendVerificationEmail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("UniWay - Código de Verificación");
        message.setText(buildEmailContent(code));
        message.setFrom("noreply@pascualbravo.edu.co");
        
        mailSender.send(message);
    }
    
    /**
     * Envía el email con el código de recuperación de contraseña
     * @param email Email destinatario
     * @param code Código de recuperación
     */
    private void sendPasswordResetEmail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("UniWay - Recuperación de Contraseña");
        message.setText(buildPasswordResetEmailContent(code));
        message.setFrom("noreply@pascualbravo.edu.co");
        
        mailSender.send(message);
    }
    
    /**
     * Construye el contenido del email de verificación
     * @param code Código de verificación
     * @return Contenido del email formateado
     */
    private String buildEmailContent(String code) {
        return String.format(
            "¡Bienvenido a UniWay!\n\n" +
            "Tu código de verificación es: %s\n\n" +
            "Este código expirará en %d minutos.\n\n" +
            "Si no solicitaste este código, puedes ignorar este mensaje.\n\n" +
            "Saludos,\n" +
            "Equipo UniWay\n" +
            "Institución Universitaria Pascual Bravo",
            code,
            CODE_EXPIRATION_MINUTES
        );
    }
    
    /**
     * Construye el contenido del email de recuperación de contraseña
     * @param code Código de recuperación
     * @return Contenido del email formateado
     */
    private String buildPasswordResetEmailContent(String code) {
        return String.format(
            "Recuperación de Contraseña - UniWay\n\n" +
            "Recibimos una solicitud para restablecer tu contraseña.\n\n" +
            "Tu código de recuperación es: %s\n\n" +
            "Este código expirará en %d minutos.\n\n" +
            "Si no solicitaste este cambio, puedes ignorar este mensaje y tu contraseña permanecerá sin cambios.\n\n" +
            "Por tu seguridad, nunca compartas este código con nadie.\n\n" +
            "Saludos,\n" +
            "Equipo UniWay\n" +
            "Institución Universitaria Pascual Bravo",
            code,
            CODE_EXPIRATION_MINUTES
        );
    }
}