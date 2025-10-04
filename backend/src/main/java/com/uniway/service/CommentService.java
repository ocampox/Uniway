package com.uniway.service;

import com.uniway.entity.Comment;
import com.uniway.entity.Post;
import com.uniway.entity.User;
import com.uniway.repository.CommentRepository;
import com.uniway.repository.PostRepository;
import com.uniway.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CommentService {
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public List<Comment> getCommentsByPostId(String postId) {
        System.out.println("=== DEBUG: getCommentsByPostId ===");
        System.out.println("Post ID: " + postId);
        
        List<Comment> comments = commentRepository.findApprovedCommentsByPostId(postId);
        System.out.println("Comentarios encontrados: " + comments.size());
        
        return comments;
    }
    
    public Comment createComment(String postId, String authorId, String content) {
        System.out.println("=== DEBUG: createComment ===");
        System.out.println("Post ID: " + postId + ", Author ID: " + authorId);
        System.out.println("Content: " + content);
        
        // Verificar que el post existe
        Optional<Post> postOpt = postRepository.findByIdWithAuthor(postId);
        if (!postOpt.isPresent()) {
            throw new RuntimeException("Post no encontrado con ID: " + postId);
        }
        
        // Verificar que el usuario existe
        Optional<User> userOpt = userRepository.findById(authorId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + authorId);
        }
        
        Post post = postOpt.get();
        User author = userOpt.get();
        
        // Crear nuevo comentario
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID().toString());
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setContent(content);
        comment.setIsApproved(true); // Auto-aprobar por ahora
        
        Comment savedComment = commentRepository.save(comment);
        System.out.println("Comentario creado con ID: " + savedComment.getId());
        
        // Actualizar contador de comentarios del post
        updatePostCommentCount(post);
        
        return savedComment;
    }
    
    public Comment updateComment(String commentId, String content, String userId) {
        System.out.println("=== DEBUG: updateComment ===");
        System.out.println("Comment ID: " + commentId + ", User ID: " + userId);
        System.out.println("New Content: " + content);
        
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (!commentOpt.isPresent()) {
            throw new RuntimeException("Comentario no encontrado con ID: " + commentId);
        }
        
        Comment comment = commentOpt.get();
        
        // Obtener información del usuario para verificar permisos
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
        }
        
        User user = userOpt.get();
        
        // Verificar permisos: el autor del comentario o un administrador pueden editarlo
        boolean isAuthor = comment.getAuthor().getId().equals(userId);
        boolean isAdmin = user.getRole().toString().equals("ADMINISTRATION");
        
        System.out.println("Author ID: " + comment.getAuthor().getId());
        System.out.println("User ID: " + userId);
        System.out.println("Is Author: " + isAuthor);
        System.out.println("User Role: " + user.getRole());
        System.out.println("Is Admin: " + isAdmin);
        
        if (!isAuthor && !isAdmin) {
            throw new RuntimeException("No tienes permisos para editar este comentario");
        }
        
        comment.setContent(content);
        Comment savedComment = commentRepository.save(comment);
        
        System.out.println("Comentario actualizado exitosamente");
        
        return savedComment;
    }
    
    public void deleteComment(String commentId, String userId) {
        System.out.println("=== DEBUG: deleteComment ===");
        System.out.println("Comment ID: " + commentId + ", User ID: " + userId);
        
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (!commentOpt.isPresent()) {
            throw new RuntimeException("Comentario no encontrado con ID: " + commentId);
        }
        
        Comment comment = commentOpt.get();
        
        // Obtener información del usuario para verificar permisos
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
        }
        
        User user = userOpt.get();
        
        // Verificar permisos: el autor del comentario o un administrador pueden eliminarlo
        boolean isAuthor = comment.getAuthor().getId().equals(userId);
        boolean isAdmin = user.getRole().toString().equals("ADMINISTRATION");
        
        System.out.println("Author ID: " + comment.getAuthor().getId());
        System.out.println("User ID: " + userId);
        System.out.println("Is Author: " + isAuthor);
        System.out.println("User Role: " + user.getRole());
        System.out.println("Is Admin: " + isAdmin);
        
        if (!isAuthor && !isAdmin) {
            throw new RuntimeException("No tienes permisos para eliminar este comentario");
        }
        
        Post post = comment.getPost();
        commentRepository.deleteById(commentId);
        
        System.out.println("Comentario eliminado exitosamente");
        
        // Actualizar contador de comentarios del post
        updatePostCommentCount(post);
    }
    
    public Comment approveComment(String commentId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (!commentOpt.isPresent()) {
            throw new RuntimeException("Comentario no encontrado con ID: " + commentId);
        }
        
        Comment comment = commentOpt.get();
        comment.setIsApproved(true);
        
        Comment savedComment = commentRepository.save(comment);
        
        // Actualizar contador de comentarios del post
        updatePostCommentCount(comment.getPost());
        
        return savedComment;
    }
    
    public List<Comment> getPendingComments() {
        return commentRepository.findPendingComments();
    }
    
    private void updatePostCommentCount(Post post) {
        long commentCount = commentRepository.countApprovedCommentsByPostId(post.getId());
        post.setCommentCount((int) commentCount);
        postRepository.save(post);
        
        System.out.println("Contador de comentarios actualizado para post " + post.getId() + ": " + commentCount);
    }
    
    public com.uniway.dto.CommentDto convertToDto(Comment comment) {
        com.uniway.dto.CommentDto dto = new com.uniway.dto.CommentDto();
        dto.setId(comment.getId());
        dto.setPostId(comment.getPost().getId());
        dto.setAuthorId(comment.getAuthor().getId());
        dto.setAuthorName(comment.getAuthor().getFullName());
        dto.setAuthorRole(comment.getAuthor().getRole().toString());
        dto.setContent(comment.getContent());
        dto.setIsApproved(comment.getIsApproved());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        return dto;
    }
}