package com.uniway.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad Comment - Representa los comentarios en las publicaciones del foro
 * 
 * Esta clase mapea la tabla 'comments' en la base de datos y almacena todos los
 * comentarios que los usuarios hacen en las publicaciones.
 * 
 * Características:
 * - Cada comentario pertenece a un post específico
 * - Cada comentario tiene un autor (usuario)
 * - Los comentarios pueden ser aprobados o rechazados por moderadores
 * - Se mantiene un historial de creación y modificación
 * 
 * Relaciones:
 * - ManyToOne con Post (el post al que pertenece el comentario)
 * - ManyToOne con User (el autor del comentario)
 */
@Entity
@Table(name = "comments")
@EntityListeners(AuditingEntityListener.class) // Habilita auditoría automática
public class Comment {
    
    // ==================== CAMPOS PRINCIPALES ====================
    
    /** Identificador único del comentario (UUID de 36 caracteres) */
    @Id
    @Column(length = 36)
    private String id;
    
    /** Referencia al post al que pertenece este comentario (carga lazy) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    
    /** Referencia al usuario autor del comentario (carga lazy) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    
    /** Contenido del comentario (máximo 1000 caracteres, campo de texto) */
    @NotBlank
    @Size(max = 1000)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    // ==================== MODERACIÓN ====================
    
    /** Indica si el comentario ha sido aprobado por moderadores (por defecto true) */
    @Column(name = "is_approved")
    private Boolean isApproved = true;
    
    // ==================== AUDITORÍA AUTOMÁTICA ====================
    
    /** Fecha y hora de creación del comentario (se establece automáticamente) */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /** Fecha y hora de última modificación (se actualiza automáticamente) */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // ==================== CONSTRUCTORES ====================
    
    /** Constructor por defecto requerido por JPA */
    public Comment() {}
    
    /** 
     * Constructor con campos principales para crear un comentario
     * @param id Identificador único del comentario
     * @param post Post al que pertenece el comentario
     * @param author Usuario autor del comentario
     * @param content Contenido del comentario
     */
    public Comment(String id, Post post, User author, String content) {
        this.id = id;
        this.post = post;
        this.author = author;
        this.content = content;
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Boolean getIsApproved() { return isApproved; }
    public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}







