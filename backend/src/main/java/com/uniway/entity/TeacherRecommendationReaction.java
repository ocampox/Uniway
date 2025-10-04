package com.uniway.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad TeacherRecommendationReaction - Representa las reacciones (likes/dislikes) 
 * a las recomendaciones de profesores
 * 
 * Esta clase mapea la tabla 'teacher_recommendation_reactions' y maneja el sistema
 * de interacciones sociales para las recomendaciones de profesores, similar al
 * sistema de reacciones de los posts del foro.
 * 
 * Características:
 * - Un usuario solo puede tener una reacción por recomendación (UNIQUE constraint)
 * - Puede cambiar su reacción de LIKE a DISLIKE y viceversa
 * - Puede eliminar su reacción (toggle off)
 * - Auditoría automática de fechas de creación y modificación
 */
@Entity
@Table(name = "teacher_recommendation_reactions")
@EntityListeners(AuditingEntityListener.class)
public class TeacherRecommendationReaction {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private StudentTeacher recommendation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false)
    private ReactionType reactionType;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructores
    public TeacherRecommendationReaction() {}

    public TeacherRecommendationReaction(String id, StudentTeacher recommendation, User user, ReactionType reactionType) {
        this.id = id;
        this.recommendation = recommendation;
        this.user = user;
        this.reactionType = reactionType;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public StudentTeacher getRecommendation() { return recommendation; }
    public void setRecommendation(StudentTeacher recommendation) { this.recommendation = recommendation; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ReactionType getReactionType() { return reactionType; }
    public void setReactionType(ReactionType reactionType) { this.reactionType = reactionType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}