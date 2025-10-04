package com.uniway.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad VerificationCode - Representa los códigos de verificación de email
 * 
 * Esta clase almacena los códigos de verificación enviados por email durante
 * el proceso de registro de usuarios. Los códigos tienen una duración limitada
 * y solo pueden usarse una vez.
 * 
 * Características:
 * - Códigos de 6 dígitos generados aleatoriamente
 * - Expiración automática después de 10 minutos
 * - Un solo uso por código
 * - Asociados a un email específico
 */
@Entity
@Table(name = "verification_codes")
@EntityListeners(AuditingEntityListener.class)
public class VerificationCode {
    
    // ==================== CAMPOS PRINCIPALES ====================
    
    /** Identificador único del código de verificación */
    @Id
    @Column(length = 36)
    private String id;
    
    /** Email al que se envió el código de verificación */
    @Column(nullable = false)
    private String email;
    
    /** Código de verificación de 6 dígitos */
    @Column(nullable = false, length = 6)
    private String code;
    
    /** Fecha y hora de expiración del código (10 minutos después de creación) */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    /** Indica si el código ya fue utilizado */
    @Column(name = "is_used")
    private Boolean isUsed = false;
    
    // ==================== AUDITORÍA ====================
    
    /** Fecha y hora de creación del código */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // ==================== CONSTRUCTORES ====================
    
    /** Constructor por defecto requerido por JPA */
    public VerificationCode() {}
    
    /**
     * Constructor para crear un código de verificación
     * @param id Identificador único
     * @param email Email destinatario
     * @param code Código de 6 dígitos
     * @param expiresAt Fecha de expiración
     */
    public VerificationCode(String id, String email, String code, LocalDateTime expiresAt) {
        this.id = id;
        this.email = email;
        this.code = code;
        this.expiresAt = expiresAt;
        this.isUsed = false; // ← INICIALIZAR COMO FALSE
    }
    
    
   
    
    // ==================== GETTERS Y SETTERS ====================
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public Boolean getIsUsed() {
        return isUsed != null ? isUsed : false;
    }
    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed != null ? isUsed : false;
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // ==================== MÉTODOS DE UTILIDAD ====================
    
    /**
     * Verifica si el código ha expirado
     * @return true si el código ha expirado, false en caso contrario
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Verifica si el código es válido (no usado y no expirado)
     * @return true si el código es válido, false en caso contrario
     */
    public boolean isValid() {
        return !isUsed && !isExpired();
    }
}