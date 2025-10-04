package com.uniway.dto;

import com.uniway.entity.PostPriority;
import com.uniway.entity.PostType;
import java.time.LocalDateTime;

public class PostDto {
    private String id;
    private String authorId;
    private String authorName;
    private String authorRole;
    private String content;
    private PostType postType;
    private PostPriority priority;
    private Boolean isPinned;
    private Boolean isAlert;
    private Boolean isApproved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long likeCount;
    private Long dislikeCount;
    private Long commentCount;
    private Boolean isLiked;
    private Boolean isDisliked;

    
    // Constructores
    public PostDto() {}
    
    public PostDto(String id, String authorName, String content, PostType postType) {
        this.id = id;
        this.authorName = authorName;
        this.content = content;
        this.postType = postType;
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    
    public String getAuthorRole() { return authorRole; }
    public void setAuthorRole(String authorRole) { this.authorRole = authorRole; }
    
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
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getLikeCount() { return likeCount; }
    public void setLikeCount(Long likeCount) { this.likeCount = likeCount; }
    
    public Long getDislikeCount() { return dislikeCount; }
    public void setDislikeCount(Long dislikeCount) { this.dislikeCount = dislikeCount; }
    
    public Long getCommentCount() { return commentCount; }
    public void setCommentCount(Long commentCount) { this.commentCount = commentCount; }
    
    public Boolean getIsLiked() { return isLiked; }
    public void setIsLiked(Boolean isLiked) { this.isLiked = isLiked; }
    
    public Boolean getIsDisliked() { return isDisliked; }
    public void setIsDisliked(Boolean isDisliked) { this.isDisliked = isDisliked; }
    

}










