package com.uniway.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad Reaction - Representa las reacciones (likes/dislikes) de los usuarios a los posts
 * 
 * Esta clase implementa el sistema de reacciones inteligente del foro, donde cada usuario
 * puede dar solo una reacción por post (like o dislike).
 * 
 * Características del sistema:
 * - Un usuario solo puede tener una reacción por post (restricción única)
 * - Si el usuario ya dio like y presiona like nuevamente, se elimina la reacción
 * - Si el usuario tenía like y presiona dislike, cambia a dislike
 * - Los contadores se actualizan automáticamente en la tabla posts
 * 
 * Tipos de reacción:
 * - LIKE: Reacción positiva al post
 * - DISLIKE: Reacción negativa al post
 * 
 * Relaciones:
 * - ManyToOne con User (usuario que reacciona)
 * - ManyToOne con Post (post al que se reacciona)
 */
@Entity
@Table(name = "reactions", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"})) // Un usuario solo puede reaccionar una vez por post
@EntityListeners(AuditingEntityListener.class) // Habilita auditoría automática
public class Reaction {
    
    // ==================== CAMPOS PRINCIPALES ====================
    
    /** Identificador único de la reacción (UUID de 36 caracteres) */
    @Id
    @Column(length = 36)
    private String id;
    
    /** Referencia al usuario que hace la reacción (carga lazy) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /** Referencia al post al que se reacciona (carga lazy) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    
    /** Tipo de reacción: LIKE o DISLIKE */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType type;
    
    // ==================== AUDITORÍA AUTOMÁTICA ====================
    
    /** Fecha y hora de creación de la reacción (se establece automáticamente) */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // ==================== CONSTRUCTORES ====================
    
    /** Constructor por defecto requerido por JPA */
    public Reaction() {}
    
    /** 
     * Constructor con campos principales para crear una reacción
     * @param id Identificador único de la reacción
     * @param user Usuario que hace la reacción
     * @param post Post al que se reacciona
     * @param type Tipo de reacción (LIKE o DISLIKE)
     */
    public Reaction(String id, User user, Post post, ReactionType type) {
        this.id = id;
        this.user = user;
        this.post = post;
        this.type = type;
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    
    public ReactionType getType() { return type; }
    public void setType(ReactionType type) { this.type = type; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}







