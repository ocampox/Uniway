package com.uniway.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad User - Representa a los usuarios del sistema (estudiantes, administración)
 * 
 * Esta clase mapea la tabla 'users' en la base de datos y contiene toda la información
 * personal y de autenticación de los usuarios del foro estudiantil.
 * 
 * Relaciones:
 * - OneToMany con Post (un usuario puede tener muchos posts)
 * - OneToMany con Comment (un usuario puede tener muchos comentarios)
 * - OneToMany con Reaction (un usuario puede tener muchas reacciones)
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class) // Habilita auditoría automática (createdAt, updatedAt)
public class User {
    
    // ==================== CAMPOS PRINCIPALES ====================
    
    /** Identificador único del usuario (UUID de 36 caracteres) */
    @Id
    @Column(length = 36)
    private String id;
    
    /** Email institucional del usuario - usado para login y debe ser único */
    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;
    
    /** Contraseña encriptada con BCrypt (mínimo 6 caracteres antes de encriptar) */
    @NotBlank
    @Size(min = 6)
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    /** Rol del usuario en el sistema (STUDENT, ADMINISTRATION) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    
    /** Nombre completo del usuario */
    @NotBlank
    @Column(name = "full_name", nullable = false)
    private String fullName;
    
    // ==================== INFORMACIÓN ESTUDIANTIL ====================
    
    /** Número de carnet estudiantil (único, solo para estudiantes) */
    @Column(name = "student_id", unique = true)
    private String studentId;
    
    /** Programa académico del estudiante */
    private String program;
    
    // ==================== INFORMACIÓN PERSONAL ====================
    
    /** URL de la imagen de perfil del usuario */
    @Column(name = "profile_image_url")
    private String profileImageUrl;
    
    /** Número de teléfono del usuario */
    private String phone;
    
    /** Dirección de residencia del usuario (campo de texto largo) */
    @Column(columnDefinition = "TEXT")
    private String address;
    
    /** Indica si el usuario está activo en el sistema (por defecto true) */
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // ==================== AUDITORÍA AUTOMÁTICA ====================
    
    /** Fecha y hora de creación del usuario (se establece automáticamente) */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /** Fecha y hora de última modificación (se actualiza automáticamente) */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // ==================== RELACIONES CON OTRAS ENTIDADES ====================
    
    /** Lista de posts creados por este usuario (carga lazy para optimizar rendimiento) */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> posts = new ArrayList<>();
    
    /** Lista de comentarios creados por este usuario (carga lazy) */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();
    
    /** Lista de reacciones (likes/dislikes) dadas por este usuario (carga lazy) */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reaction> reactions = new ArrayList<>();
    

    
    // ==================== CONSTRUCTORES ====================
    
    /** Constructor por defecto requerido por JPA */
    public User() {}
    
    /** 
     * Constructor con campos principales para crear un usuario
     * @param id Identificador único del usuario
     * @param email Email institucional del usuario
     * @param passwordHash Contraseña ya encriptada con BCrypt
     * @param role Rol del usuario en el sistema
     * @param fullName Nombre completo del usuario
     */
    public User(String id, String email, String passwordHash, UserRole role, String fullName) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
    }
    
    // ==================== GETTERS Y SETTERS ====================
    
    /** Obtiene el ID único del usuario */
    public String getId() { return id; }
    /** Establece el ID único del usuario */
    public void setId(String id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }
    
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<Post> getPosts() { return posts; }
    public void setPosts(List<Post> posts) { this.posts = posts; }
    
    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }
    
    public List<Reaction> getReactions() { return reactions; }
    public void setReactions(List<Reaction> reactions) { this.reactions = reactions; }
    

}










