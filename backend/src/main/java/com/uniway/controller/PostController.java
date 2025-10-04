package com.uniway.controller;

import com.uniway.dto.PostDto;
import com.uniway.entity.Post;
import com.uniway.entity.PostPriority;
import com.uniway.entity.PostType;
import com.uniway.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/posts")
@Tag(name = "Posts", description = "Endpoints para gestión de publicaciones del foro")
@CrossOrigin(origins = "*")
public class PostController {
    
    @Autowired
    private PostService postService;
    
    @GetMapping("/simple")
    @Operation(summary = "Obtener posts - versión simple")
    public ResponseEntity<?> getPostsSimple() {
        try {
            System.out.println("=== DEBUG: Obteniendo posts (simple) ===");
            
            // Crear posts de ejemplo sin base de datos
            java.util.List<Map<String, Object>> mockPosts = new java.util.ArrayList<>();
            
            Map<String, Object> post1 = new HashMap<>();
            post1.put("id", "mock-1");
            post1.put("authorName", "Sistema");
            post1.put("content", "Post de prueba desde el backend");
            post1.put("postType", "GENERAL");
            post1.put("createdAt", java.time.LocalDateTime.now().toString());
            mockPosts.add(post1);
            
            System.out.println("Posts mock creados: " + mockPosts.size());
            return ResponseEntity.ok(mockPosts);
            
        } catch (Exception e) {
            System.err.println("=== ERROR: Posts simple ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error en posts simple: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping
    @Operation(summary = "Obtener todas las publicaciones")
    public ResponseEntity<?> getAllPosts(
            @RequestParam(required = false) PostType postType,
            @RequestParam(required = false) PostPriority priority,
            @RequestParam(required = false) Boolean isPinned,
            Pageable pageable) {
        
        try {
            System.out.println("=== DEBUG: Obteniendo posts ===");
            System.out.println("Parámetros - postType: " + postType + ", priority: " + priority + ", isPinned: " + isPinned);
            
            java.util.List<Post> posts = postService.getAllPostsSimple();
            System.out.println("Posts encontrados: " + posts.size());
            
            if (posts.isEmpty()) {
                System.out.println("No hay posts, devolviendo lista vacía");
                return ResponseEntity.ok(new java.util.ArrayList<>());
            }
            
            java.util.List<PostDto> postDtos = posts.stream()
                .map(postService::convertToDto)
                .collect(java.util.stream.Collectors.toList());
                
            System.out.println("PostDtos convertidos: " + postDtos.size());
            
            return ResponseEntity.ok(postDtos);
            
        } catch (Exception e) {
            System.err.println("=== ERROR: Obteniendo posts ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener posts: " + e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/hash/{password}")
    @Operation(summary = "Generar hash BCrypt para contraseña (solo desarrollo)")
    public ResponseEntity<?> generatePasswordHash(@PathVariable String password) {
        try {
            org.springframework.security.crypto.password.PasswordEncoder encoder = 
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            String hash = encoder.encode(password);
            
            Map<String, String> response = new HashMap<>();
            response.put("password", password);
            response.put("hash", hash);
            response.put("message", "Hash generado correctamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PostMapping("/dev")
    @Operation(summary = "Crear nueva publicación (desarrollo - sin autenticación)")
    public ResponseEntity<?> createPostDev(
            @RequestBody CreatePostRequestDev request) {
        try {
            System.out.println("=== DEBUG: Creando post (desarrollo) ===");
            System.out.println("Content: " + request.getContent());
            System.out.println("PostType: " + request.getPostType());
            System.out.println("Priority: " + request.getPriority());
            System.out.println("AuthorEmail: " + request.getAuthorEmail());
            
            // Buscar usuario por email
            String userId = postService.getUserIdByEmail(request.getAuthorEmail());
            System.out.println("Usuario encontrado: " + userId);
            
            Post post = postService.createPost(
                userId,
                request.getContent(),
                PostType.valueOf(request.getPostType()),
                PostPriority.valueOf(request.getPriority())
            );
            
            System.out.println("Post creado con ID: " + post.getId());
            
            PostDto postDto = postService.convertToDto(post);
            
            Map<String, Object> response = new HashMap<>();
            response.put("post", postDto);
            response.put("message", "Publicación creada exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== ERROR: Creando post (desarrollo) ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/test")
    @Operation(summary = "Test básico sin base de datos")
    public ResponseEntity<?> testBasic() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Backend funcionando correctamente");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Verificar estado de la base de datos")
    public ResponseEntity<?> healthCheck() {
        try {
            System.out.println("=== DEBUG: Health Check ===");
            
            long totalPosts = postService.countAllPosts();
            System.out.println("Total posts: " + totalPosts);
            
            long totalUsers = postService.countAllUsers();
            System.out.println("Total users: " + totalUsers);
            
            Map<String, Object> health = new HashMap<>();
            health.put("status", "OK");
            health.put("totalPosts", totalPosts);
            health.put("totalUsers", totalUsers);
            health.put("timestamp", java.time.LocalDateTime.now().toString());
            
            System.out.println("Health check exitoso");
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            System.err.println("=== ERROR: Health Check ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener publicación por ID")
    public ResponseEntity<PostDto> getPostById(@PathVariable String id) {
        java.util.Optional<Post> postOpt = postService.getPostById(id);
        if (postOpt.isPresent()) {
            PostDto postDto = postService.convertToDto(postOpt.get());
            return ResponseEntity.ok(postDto);
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/{id}/like")
    @Operation(summary = "Dar like a una publicación")
    @Transactional
    public ResponseEntity<?> likePost(@PathVariable String id, @RequestBody LikeRequest request) {
        try {
            System.out.println("=== DEBUG: Like post ===");
            System.out.println("Post ID: " + id);
            System.out.println("User ID: " + request.getUserId());
            
            Post updatedPost = postService.toggleLike(id, request.getUserId());
            PostDto postDto = postService.convertToDto(updatedPost);
            
            Map<String, Object> response = new HashMap<>();
            response.put("post", postDto);
            response.put("message", "Like actualizado correctamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error en like: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/{id}/dislike")
    @Operation(summary = "Dar dislike a una publicación")
    @Transactional
    public ResponseEntity<?> dislikePost(@PathVariable String id, @RequestBody LikeRequest request) {
        try {
            System.out.println("=== DEBUG: Dislike post ===");
            System.out.println("Post ID: " + id);
            System.out.println("User ID: " + request.getUserId());
            
            Post updatedPost = postService.toggleDislike(id, request.getUserId());
            PostDto postDto = postService.convertToDto(updatedPost);
            
            Map<String, Object> response = new HashMap<>();
            response.put("post", postDto);
            response.put("message", "Dislike actualizado correctamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error en dislike: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping
    @Operation(summary = "Crear nueva publicación")
    public ResponseEntity<?> createPost(@RequestBody CreatePostRequest request) {
        try {
            System.out.println("=== DEBUG: Creando post ===");
            System.out.println("Content: " + request.getContent());
            System.out.println("PostType (string): " + request.getPostType());
            System.out.println("PostType (enum): " + request.getPostTypeEnum());
            System.out.println("Priority (string): " + request.getPriority());
            System.out.println("Priority (enum): " + request.getPriorityEnum());
            
            // Usar usuario por defecto para desarrollo
            String userId = postService.getDefaultUserId();
            System.out.println("Usando usuario por defecto para desarrollo: " + userId);
            
            Post post = postService.createPost(
                userId,
                request.getContent(),
                request.getPostTypeEnum(),
                request.getPriorityEnum()
            );
            
            System.out.println("Post creado con ID: " + post.getId());
            
            PostDto postDto = postService.convertToDto(post);
            
            Map<String, Object> response = new HashMap<>();
            response.put("post", postDto);
            response.put("message", "Publicación creada exitosamente");
            
            System.out.println("=== DEBUG: Post creado exitosamente ===");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== ERROR: Creando post ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar publicación existente")
    public ResponseEntity<?> updatePost(
            @PathVariable String id, 
            @RequestBody UpdatePostRequest request) {
        try {
            System.out.println("=== DEBUG: Actualizando post ===");
            System.out.println("Post ID: " + id);
            System.out.println("New Content: " + request.getContent());
            System.out.println("User ID: " + request.getUserId());
            
            Post updatedPost = postService.updatePost(
                id, 
                request.getContent(), 
                request.getPostType(),
                request.getUserId()
            );
            
            PostDto postDto = postService.convertToDto(updatedPost);
            
            Map<String, Object> response = new HashMap<>();
            response.put("post", postDto);
            response.put("message", "Publicación actualizada exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== ERROR: Actualizando post ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/{id}/dev")
    @Operation(summary = "Actualizar publicación (desarrollo - sin autenticación)")
    public ResponseEntity<?> updatePostDev(
            @PathVariable String id, 
            @RequestBody UpdatePostRequestDev request) {
        try {
            System.out.println("=== DEBUG: Actualizando post (desarrollo) ===");
            System.out.println("Post ID: " + id);
            System.out.println("New Content: " + request.getContent());
            System.out.println("User Email: " + request.getUserEmail());
            
            // Buscar usuario por email
            String userId = postService.getUserIdByEmail(request.getUserEmail());
            
            Post updatedPost = postService.updatePost(
                id, 
                request.getContent(), 
                request.getPostType(),
                userId
            );
            
            PostDto postDto = postService.convertToDto(updatedPost);
            
            Map<String, Object> response = new HashMap<>();
            response.put("post", postDto);
            response.put("message", "Publicación actualizada exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== ERROR: Actualizando post (desarrollo) ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar publicación")
    public ResponseEntity<?> deletePost(
            @PathVariable String id,
            @RequestParam String userId) {
        try {
            System.out.println("=== DEBUG: Eliminando post ===");
            System.out.println("Post ID: " + id);
            System.out.println("User ID: " + userId);
            
            postService.deletePost(id, userId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Publicación eliminada exitosamente");
            response.put("postId", id);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== ERROR: Eliminando post ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @DeleteMapping("/{id}/dev")
    @Operation(summary = "Eliminar publicación (desarrollo - sin autenticación)")
    public ResponseEntity<?> deletePostDev(
            @PathVariable String id,
            @RequestParam String userEmail) {
        try {
            System.out.println("=== DEBUG: Eliminando post (desarrollo) ===");
            System.out.println("Post ID: " + id);
            System.out.println("User Email: " + userEmail);
            
            // Buscar usuario por email
            String userId = postService.getUserIdByEmail(userEmail);
            
            postService.deletePost(id, userId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Publicación eliminada exitosamente");
            response.put("postId", id);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== ERROR: Eliminando post (desarrollo) ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Clases internas para requests
    public static class CreatePostRequest {
        private String content;
        private String postType;
        private String priority;
        
        // Getters y Setters
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getPostType() { return postType; }
        public void setPostType(String postType) { this.postType = postType; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        
        // Métodos para convertir a enums
        public PostType getPostTypeEnum() {
            try {
                return PostType.valueOf(postType);
            } catch (Exception e) {
                return PostType.GENERAL;
            }
        }
        
        public PostPriority getPriorityEnum() {
            try {
                return PostPriority.valueOf(priority);
            } catch (Exception e) {
                return PostPriority.NORMAL;
            }
        }
    }
    
    public static class CreatePostRequestDev {
        private String content;
        private String postType;
        private String priority;
        private String authorEmail;
        
        // Getters y Setters
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getPostType() { return postType; }
        public void setPostType(String postType) { this.postType = postType; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        
        public String getAuthorEmail() { return authorEmail; }
        public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }
    }
    
    public static class LikeRequest {
        private String userId;
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
    
    public static class UpdatePostRequest {
        private String content;
        private String postType;
        private String userId;
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getPostType() { return postType; }
        public void setPostType(String postType) { this.postType = postType; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
    
    public static class UpdatePostRequestDev {
        private String content;
        private String postType;
        private String userEmail;
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getPostType() { return postType; }
        public void setPostType(String postType) { this.postType = postType; }
        
        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    }
    
    public static class DeletePostRequest {
        private String userId;
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
    
    public static class DeletePostRequestDev {
        private String userEmail;
        
        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    }
}
