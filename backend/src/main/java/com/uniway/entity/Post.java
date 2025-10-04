package com.uniway.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Post - Representa las publicaciones del foro estudiantil
 * 
 * Esta clase mapea la tabla 'posts' en la base de datos y contiene toda la información
 * de las publicaciones creadas por los usuarios del sistema.
 * 
 * Tipos de posts:
 * - GENERAL: Publicaciones normales de estudiantes
 * - NEWS: Noticias oficiales de administración
 * - ALERT: Alertas de seguridad
 * - ANNOUNCEMENT: Anuncios oficiales
 * 
 * Prioridades:
 * - NORMAL: Contenido estándar
 * - HIGH: Contenido importante
 * - URGENT: Contenido crítico (alertas)
 * 
 * Relaciones:
 * - ManyToOne con User (autor del post)
 * - OneToMany con Comment (comentarios del post)
 * - OneToMany con Reaction (reacciones al post)
 */
@Entity
@Table(name = "posts")
@EntityListeners(AuditingEntityListener.class) // Habilita auditoría automática
public class Post {
    
    @Id
    @Column(length = 36)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    
    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "post_type")
    private PostType postType = PostType.GENERAL;
    
    @Enumerated(EnumType.STRING)
    private PostPriority priority = PostPriority.NORMAL;
    
    @Column(name = "is_pinned")
    private Boolean isPinned = false;
    
    @Column(name = "is_alert")
    private Boolean isAlert = false;
    
    @Column(name = "is_approved")
    private Boolean isApproved = true;
    
    @Column(name = "like_count")
    private Integer likeCount = 0;
    
    @Column(name = "dislike_count")
    private Integer dislikeCount = 0;
    
    @Column(name = "comment_count")
    private Integer commentCount = 0;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relaciones
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reaction> reactions = new ArrayList<>();
    

    
    // Constructores
    public Post() {}
    
    public Post(String id, User author, String content, PostType postType) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.postType = postType;
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public PostType getPostType() { return postType; }
    public void setPostType(PostType postType) { this.postType = postType; }
    
    public PostPriority getPriority() { return priority; }
    public void setPriority(PostPriority priority) { this.priority = priority; }
    
    public Boolean getIsPinned() { return isPinned; }
    public void setIsPinned(Boolean isPinned) { this.isPinned = isPinned; }
    
    public Boolean getIsAlert() { return isAlert; }
    public void setIsAlert(Boolean isAlert) { this.isAlert = isAlert; }
    
    public Boolean getIsApproved() { return isApproved; }
    public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }
    
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    
    public Integer getDislikeCount() { return dislikeCount; }
    public void setDislikeCount(Integer dislikeCount) { this.dislikeCount = dislikeCount; }
    
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }
    
    public List<Reaction> getReactions() { return reactions; }
    public void setReactions(List<Reaction> reactions) { this.reactions = reactions; }
    

}



