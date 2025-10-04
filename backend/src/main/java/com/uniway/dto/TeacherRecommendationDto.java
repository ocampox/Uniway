package com.uniway.dto;

import java.time.LocalDateTime;

/**
 * TeacherRecommendationDto - DTO para transferir datos de recomendaciones de profesores
 * 
 * Este DTO incluye toda la información necesaria para mostrar una recomendación
 * en el frontend, incluyendo datos del profesor, estudiante, reacción y contadores.
 */
public class TeacherRecommendationDto {
    
    // Información básica de la recomendación
    private String id;
    private String studentId;
    private String studentName;
    private String teacherName;
    private String subject;
    private String semester;
    private Integer year;
    private String reference;
    private Integer rating;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Contadores de reacciones
    private Long likeCount;
    private Long dislikeCount;
    private Long totalReactions;
    
    // Estado de reacción del usuario actual
    private Boolean isLiked;
    private Boolean isDisliked;
    private String userReaction; // "LIKE", "DISLIKE", o null

    // Constructores
    public TeacherRecommendationDto() {}

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Métodos de teacher eliminados - Sistema simplificado

    public Long getLikeCount() { return likeCount; }
    public void setLikeCount(Long likeCount) { this.likeCount = likeCount; }

    public Long getDislikeCount() { return dislikeCount; }
    public void setDislikeCount(Long dislikeCount) { this.dislikeCount = dislikeCount; }

    public Long getTotalReactions() { return totalReactions; }
    public void setTotalReactions(Long totalReactions) { this.totalReactions = totalReactions; }

    public Boolean getIsLiked() { return isLiked; }
    public void setIsLiked(Boolean isLiked) { this.isLiked = isLiked; }

    public Boolean getIsDisliked() { return isDisliked; }
    public void setIsDisliked(Boolean isDisliked) { this.isDisliked = isDisliked; }

    public String getUserReaction() { return userReaction; }
    public void setUserReaction(String userReaction) { this.userReaction = userReaction; }
}