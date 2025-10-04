package com.uniway.controller;

import com.uniway.dto.CommentDto;
import com.uniway.entity.Comment;
import com.uniway.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments")
@Tag(name = "Comments", description = "Endpoints para gestión de comentarios")
@CrossOrigin(origins = "*")
public class CommentController {
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private com.uniway.service.PostService postService;
    
    @GetMapping("/post/{postId}")
    @Operation(summary = "Obtener comentarios de una publicación")
    public ResponseEntity<?> getCommentsByPostId(@PathVariable String postId) {
        try {
            System.out.println("=== DEBUG: Obteniendo comentarios del post " + postId + " ===");
            
            List<Comment> comments = commentService.getCommentsByPostId(postId);
            List<CommentDto> commentDtos = comments.stream()
                .map(commentService::convertToDto)
                .collect(java.util.stream.Collectors.toList());
            
            System.out.println("Comentarios convertidos: " + commentDtos.size());
            
            return ResponseEntity.ok(commentDtos);
            
        } catch (Exception e) {
            System.err.println("=== ERROR: Obteniendo comentarios ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener comentarios: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PostMapping
    @Operation(summary = "Crear nuevo comentario")
    @Transactional
    public ResponseEntity<?> createComment(@RequestBody CreateCommentRequest request) {
        try {
            System.out.println("=== DEBUG: Creando comentario ===");
            System.out.println("Post ID: " + request.getPostId());
            System.out.println("Author ID: " + request.getAuthorId());
            System.out.println("Content: " + request.getContent());
            
            Comment comment = commentService.createComment(
                request.getPostId(),
                request.getAuthorId(),
                request.getContent()
            );
            
            CommentDto commentDto = commentService.convertToDto(comment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("comment", commentDto);
            response.put("message", "Comentario creado exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("=== ERROR: Creando comentario ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/dev")
    @Operation(summary = "Crear comentario (modo desarrollo)")
    @Transactional
    public ResponseEntity<?> createCommentDev(@RequestBody CreateCommentRequestDev request) {
        try {
            System.out.println("=== DEBUG: Creando comentario (desarrollo) ===");
            System.out.println("Post ID: " + request.getPostId());
            System.out.println("Author Email: " + request.getAuthorEmail());
            System.out.println("Content: " + request.getContent());
            
            // Buscar usuario por email (modo desarrollo)
            String userId = getUserIdByEmail(request.getAuthorEmail());
            
            Comment comment = commentService.createComment(
                request.getPostId(),
                userId,
                request.getContent()
            );
            
            CommentDto commentDto = commentService.convertToDto(comment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("comment", commentDto);
            response.put("message", "Comentario creado exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("=== ERROR: Creando comentario (desarrollo) ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar comentario")
    @Transactional
    public ResponseEntity<?> updateComment(
        @PathVariable String id,
        @RequestBody UpdateCommentRequest request
    ) {
        try {
            System.out.println("=== DEBUG: Actualizando comentario ===");
            System.out.println("Comment ID: " + id);
            System.out.println("Content: " + request.getContent());
            System.out.println("User ID: " + request.getUserId());
            
            Comment comment = commentService.updateComment(id, request.getContent(), request.getUserId());
            CommentDto commentDto = commentService.convertToDto(comment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("comment", commentDto);
            response.put("message", "Comentario actualizado exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("=== ERROR: Actualizando comentario ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar comentario")
    @Transactional
    public ResponseEntity<?> deleteComment(
        @PathVariable String id,
        @RequestParam String userId
    ) {
        try {
            System.out.println("=== DEBUG: CommentController.deleteComment ===");
            System.out.println("Comment ID: " + id);
            System.out.println("User ID: " + userId);
            
            commentService.deleteComment(id, userId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Comentario eliminado exitosamente");
            
            System.out.println("Respuesta enviada: " + response);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("=== ERROR: CommentController.deleteComment ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/{id}/approve")
    @Operation(summary = "Aprobar comentario")
    @Transactional
    public ResponseEntity<?> approveComment(@PathVariable String id) {
        try {
            Comment comment = commentService.approveComment(id);
            CommentDto commentDto = commentService.convertToDto(comment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("comment", commentDto);
            response.put("message", "Comentario aprobado exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/pending")
    @Operation(summary = "Obtener comentarios pendientes de aprobación")
    public ResponseEntity<?> getPendingComments() {
        try {
            List<Comment> comments = commentService.getPendingComments();
            List<CommentDto> commentDtos = comments.stream()
                .map(commentService::convertToDto)
                .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(commentDtos);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // Método auxiliar para obtener usuario por email (modo desarrollo)
    private String getUserIdByEmail(String email) {
        return postService.getUserIdByEmail(email);
    }
    
    // Clases internas para requests
    public static class CreateCommentRequest {
        private String postId;
        private String authorId;
        private String content;
        
        public String getPostId() { return postId; }
        public void setPostId(String postId) { this.postId = postId; }
        
        public String getAuthorId() { return authorId; }
        public void setAuthorId(String authorId) { this.authorId = authorId; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
    
    public static class CreateCommentRequestDev {
        private String postId;
        private String authorEmail;
        private String content;
        
        public String getPostId() { return postId; }
        public void setPostId(String postId) { this.postId = postId; }
        
        public String getAuthorEmail() { return authorEmail; }
        public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
    
    public static class UpdateCommentRequest {
        private String content;
        private String userId;
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
    
    public static class DeleteCommentRequest {
        private String userId;
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
}