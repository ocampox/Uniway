package com.uniway.repository;

import com.uniway.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * VerificationCodeRepository - Repositorio para operaciones con códigos de verificación
 * 
 * Este repositorio maneja todas las operaciones relacionadas con los códigos
 * de verificación de email durante el proceso de registro.
 * 
 * Funcionalidades:
 * - Buscar códigos por email y código
 * - Verificar validez de códigos (no expirados, no usados)
 * - Limpiar códigos expirados automáticamente
 * - Obtener códigos activos por email
 */
@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, String> {
    
    /**
     * Busca un código de verificación específico para un email
     * @param email Email del usuario
     * @param code Código de verificación
     * @return Optional con el código si existe
     */
    @Query("SELECT vc FROM VerificationCode vc WHERE vc.email = :email AND vc.code = :code")
    Optional<VerificationCode> findByEmailAndCode(@Param("email") String email, @Param("code") String code);
    
    /**
     * Busca códigos de verificación válidos (no usados y no expirados) para un email
     * @param email Email del usuario
     * @param now Fecha y hora actual para comparar expiración
     * @return Lista de códigos válidos
     */
    @Query("SELECT vc FROM VerificationCode vc WHERE vc.email = :email AND vc.isUsed = false AND vc.expiresAt > :now ORDER BY vc.createdAt DESC")
    List<VerificationCode> findValidCodesByEmail(@Param("email") String email, @Param("now") LocalDateTime now);
    
    /**
     * Busca todos los códigos de un email específico
     * @param email Email del usuario
     * @return Lista de códigos ordenados por fecha de creación descendente
     */
    @Query("SELECT vc FROM VerificationCode vc WHERE vc.email = :email ORDER BY vc.createdAt DESC")
    List<VerificationCode> findByEmailOrderByCreatedAtDesc(@Param("email") String email);
    
    /**
     * Busca códigos expirados para limpieza automática
     * @param now Fecha y hora actual
     * @return Lista de códigos expirados
     */
    @Query("SELECT vc FROM VerificationCode vc WHERE vc.expiresAt < :now")
    List<VerificationCode> findExpiredCodes(@Param("now") LocalDateTime now);
    
    /**
     * Cuenta códigos válidos para un email (para limitar intentos)
     * @param email Email del usuario
     * @param now Fecha y hora actual
     * @return Número de códigos válidos
     */
    @Query("SELECT COUNT(vc) FROM VerificationCode vc WHERE vc.email = :email AND vc.isUsed = false AND vc.expiresAt > :now")
    long countValidCodesByEmail(@Param("email") String email, @Param("now") LocalDateTime now);
    
    /**
     * Elimina códigos expirados (para limpieza automática)
     * @param now Fecha y hora actual
     */
    @Query("DELETE FROM VerificationCode vc WHERE vc.expiresAt < :now")
    void deleteExpiredCodes(@Param("now") LocalDateTime now);
}