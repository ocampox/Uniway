package com.uniway.service;

import com.uniway.entity.Post;
import com.uniway.entity.PostType;
import com.uniway.entity.PostPriority;
import com.uniway.entity.User;
import com.uniway.entity.Reaction;
import com.uniway.entity.ReactionType;
import com.uniway.repository.PostRepository;
import com.uniway.repository.UserRepository;
import com.uniway.repository.ReactionRepository;
import com.uniway.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PostService - Servicio de lógica de negocio para la gestión de publicaciones del foro
 * 
 * Este servicio maneja toda la lógica relacionada con posts, incluyendo:
 * - Creación, actualización y eliminación de posts
 * - Sistema de reacciones inteligente (likes/dislikes con toggle)
 * - Búsqueda y filtrado de posts por diferentes criterios
 * - Gestión de usuarios por defecto y por email
 * - Actualización automática de contadores (likes, dislikes, comentarios)
 * - Conversión de entidades a DTOs para la API
 * 
 * Características importantes:
 * - Todas las operaciones son transaccionales (@Transactional)
 * - Maneja usuarios por defecto cuando no se encuentra el usuario especificado
 * - Implementa lógica de toggle para reacciones (like/dislike)
 * - Actualiza contadores automáticamente desde las tablas relacionadas
 * - Proporciona métodos seguros con manejo de excepciones
 */
@Service
@Transactional // Todas las operaciones del servicio son transaccionales
public class PostService {
    
    // ==================== DEPENDENCIAS INYECTADAS ====================
    
    /** Repositorio para operaciones con posts */
    @Autowired
    private PostRepository postRepository;
    
    /** Repositorio para operaciones con usuarios */
    @Autowired
    private UserRepository userRepository;
    
    /** Repositorio para operaciones con reacciones (likes/dislikes) */
    @Autowired
    private ReactionRepository reactionRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    public String getDefaultUserId() {
        // Intentar obtener student-001 primero
        Optional<User> student001 = userRepository.findById("student-001");
        if (student001.isPresent()) {
            System.out.println("Usando usuario student-001");
            return "student-001";
        }
        
        // Si no existe, buscar cualquier usuario estudiante
        List<User> students = userRepository.findActiveUsersByRole(com.uniway.entity.UserRole.STUDENT);
        if (!students.isEmpty()) {
            String userId = students.get(0).getId();
            System.out.println("Usando primer estudiante encontrado: " + userId);
            return userId;
        }
        
        // Si no hay estudiantes, buscar cualquier usuario
        List<User> allUsers = userRepository.findAll();
        if (!allUsers.isEmpty()) {
            String userId = allUsers.get(0).getId();
            System.out.println("Usando primer usuario encontrado: " + userId);
            return userId;
        }
        
        // Si no hay usuarios, lanzar excepción
        throw new RuntimeException("No hay usuarios en la base de datos. Ejecuta el script de inicialización.");
    }
    
    public String getUserIdByEmail(String email) {
        System.out.println("Buscando usuario por email: " + email);
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            String userId = userOpt.get().getId();
            System.out.println("Usuario encontrado: " + userId + " (" + userOpt.get().getFullName() + ")");
            return userId;
        }
        
        System.err.println("Usuario no encontrado por email: " + email);
        
        // Fallback: usar usuario por defecto
        return getDefaultUserId();
    }
    
    public Post createPost(Post post) {
        // Generar ID único si no existe
        if (post.getId() == null || post.getId().isEmpty()) {
            post.setId(UUID.randomUUID().toString());
        }
        
        // Establecer valores por defecto
        if (post.getIsApproved() == null) {
            post.setIsApproved(true);
        }
        
        if (post.getIsPinned() == null) {
            post.setIsPinned(false);
        }
        
        if (post.getIsAlert() == null) {
            post.setIsAlert(false);
        }
        
        if (post.getPriority() == null) {
            post.setPriority(PostPriority.NORMAL);
        }
        
        if (post.getPostType() == null) {
            post.setPostType(PostType.GENERAL);
        }
        
        if (post.getLikeCount() == null) {
            post.setLikeCount(0);
        }
        
        if (post.getDislikeCount() == null) {
            post.setDislikeCount(0);
        }
        
        if (post.getCommentCount() == null) {
            post.setCommentCount(0);
        }
        
        return postRepository.save(post);
    }
    
    public Optional<Post> findById(String id) {
        return postRepository.findById(id);
    }
    
    public List<Post> findAllApprovedPosts() {
        return postRepository.findAllApprovedPostsOrderByPinnedAndDate();
    }
    
    public List<Post> findApprovedPostsByType(PostType type) {
        return postRepository.findApprovedPostsByType(type);
    }
    
    public List<Post> findApprovedPostsByPriority(PostPriority priority) {
        return postRepository.findApprovedPostsByPriority(priority);
    }
    
    public List<Post> findApprovedPostsByAuthor(String authorId) {
        return postRepository.findApprovedPostsByAuthor(authorId);
    }
    
    public List<Post> findAllAlertPosts() {
        return postRepository.findAllAlertPosts();
    }
    
    public List<Post> findAllPinnedPosts() {
        return postRepository.findAllPinnedPosts();
    }
    
    public List<Post> findPostsSince(LocalDateTime since) {
        return postRepository.findPostsSince(since);
    }
    
    public List<Post> findAllPendingPosts() {
        return postRepository.findAllPendingPosts();
    }
    
    public Post updatePost(Post post) {
        return postRepository.save(post);
    }
    
    public void deletePost(String id) {
        postRepository.deleteById(id);
    }
    
    public Post approvePost(String id) {
        Optional<Post> postOpt = postRepository.findById(id);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            post.setIsApproved(true);
            return postRepository.save(post);
        }
        throw new RuntimeException("Post no encontrado con ID: " + id);
    }
    
    public Post rejectPost(String id) {
        Optional<Post> postOpt = postRepository.findById(id);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            post.setIsApproved(false);
            return postRepository.save(post);
        }
        throw new RuntimeException("Post no encontrado con ID: " + id);
    }
    
    public Post pinPost(String id) {
        Optional<Post> postOpt = postRepository.findById(id);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            post.setIsPinned(true);
            return postRepository.save(post);
        }
        throw new RuntimeException("Post no encontrado con ID: " + id);
    }
    
    public Post unpinPost(String id) {
        Optional<Post> postOpt = postRepository.findById(id);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            post.setIsPinned(false);
            return postRepository.save(post);
        }
        throw new RuntimeException("Post no encontrado con ID: " + id);
    }
    
    public long countApprovedPostsByAuthor(String authorId) {
        return postRepository.countApprovedPostsByAuthor(authorId);
    }
    
    public Optional<Post> getPostById(String id) {
        return postRepository.findById(id);
    }
    
    public List<Post> getAllPosts(PostType type, PostPriority priority, Boolean isAlert, org.springframework.data.domain.Pageable pageable) {
        try {
            System.out.println("=== DEBUG PostService.getAllPosts ===");
            System.out.println("Parámetros - type: " + type + ", priority: " + priority + ", isAlert: " + isAlert);
            
            // Implementación simplificada - en producción usarías paginación
            if (type != null) {
                System.out.println("Buscando posts por tipo: " + type);
                return findApprovedPostsByType(type);
            }
            if (priority != null) {
                System.out.println("Buscando posts por prioridad: " + priority);
                return findApprovedPostsByPriority(priority);
            }
            if (isAlert != null && isAlert) {
                System.out.println("Buscando posts de alerta");
                return findAllAlertPosts();
            }
            
            System.out.println("Buscando todos los posts aprobados");
            return findAllApprovedPostsSafe();
            
        } catch (Exception e) {
            System.err.println("Error en getAllPosts: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: devolver lista vacía en lugar de lanzar excepción
            return new java.util.ArrayList<>();
        }
    }
    
    public List<Post> findAllApprovedPostsSafe() {
        try {
            System.out.println("Intentando obtener posts con query personalizada...");
            return postRepository.findAllApprovedPostsOrderByPinnedAndDate();
        } catch (Exception e) {
            System.err.println("Error con query personalizada, intentando findAllWithAuthor: " + e.getMessage());
            try {
                // Fallback: usar findAllWithAuthor y filtrar en Java
                List<Post> allPosts = postRepository.findAllWithAuthor();
                System.out.println("Posts totales en la base de datos: " + allPosts.size());
                
                return allPosts.stream()
                    .filter(post -> post.getIsApproved() != null && post.getIsApproved())
                    .sorted((p1, p2) -> {
                        // Ordenar por isPinned primero, luego por fecha
                        if (p1.getIsPinned() != p2.getIsPinned()) {
                            return Boolean.compare(p2.getIsPinned(), p1.getIsPinned());
                        }
                        return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                    })
                    .collect(java.util.stream.Collectors.toList());
            } catch (Exception e2) {
                System.err.println("Error con findAllWithAuthor: " + e2.getMessage());
                return new java.util.ArrayList<>();
            }
        }
    }
    
    public Post createPost(String authorId, String content, PostType postType, PostPriority priority) {
        System.out.println("=== DEBUG PostService.createPost ===");
        System.out.println("Buscando usuario con ID: " + authorId);
        
        Optional<User> authorOpt = userRepository.findById(authorId);
        if (!authorOpt.isPresent()) {
            System.err.println("Usuario no encontrado: " + authorId);
            
            // Mostrar usuarios disponibles para debugging
            List<User> allUsers = userRepository.findAll();
            System.err.println("Usuarios disponibles en la base de datos:");
            for (User user : allUsers) {
                System.err.println("- ID: " + user.getId() + ", Email: " + user.getEmail() + ", Nombre: " + user.getFullName());
            }
            
            throw new RuntimeException("Usuario no encontrado con ID: " + authorId + ". Usuarios disponibles: " + allUsers.size());
        }
        
        User author = authorOpt.get();
        System.out.println("Usuario encontrado: " + author.getFullName() + " (" + author.getEmail() + ")");
        
        Post post = new Post();
        post.setId(UUID.randomUUID().toString());
        post.setAuthor(author);
        post.setContent(content);
        post.setPostType(postType != null ? postType : PostType.GENERAL);
        post.setPriority(priority != null ? priority : PostPriority.NORMAL);
        
        System.out.println("Creando post con ID: " + post.getId());
        
        return createPost(post);
    }
    
    /**
     * Actualiza un post existente con validación de permisos
     * 
     * Permisos:
     * - El autor del post puede editarlo
     * - Los usuarios con rol ADMINISTRATION pueden editar cualquier post
     * 
     * @param id ID del post a actualizar
     * @param content Nuevo contenido del post
     * @param postType Nuevo tipo de post (opcional)
     * @param userId ID del usuario que intenta actualizar
     * @return Post actualizado
     * @throws RuntimeException si no tiene permisos o el post no existe
     */
    @Transactional
    public Post updatePost(String id, String content, String postType, String userId) {
        System.out.println("=== DEBUG: updatePost ===");
        System.out.println("Post ID: " + id + ", User ID: " + userId);
        
        // Buscar el post con su autor
        Optional<Post> postOpt = postRepository.findByIdWithAuthor(id);
        if (!postOpt.isPresent()) {
            throw new RuntimeException("Post no encontrado con ID: " + id);
        }
        
        // Buscar el usuario que intenta actualizar
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
        }
        
        Post post = postOpt.get();
        User user = userOpt.get();
        
        // Verificar permisos
        boolean canEdit = canUserEditPost(post, user);
        if (!canEdit) {
            throw new RuntimeException("No tienes permisos para editar este post. Solo el autor o administradores pueden editarlo.");
        }
        
        System.out.println("Usuario autorizado para editar el post");
        
        // Actualizar contenido
        post.setContent(content);
        
        // Actualizar tipo de post si se proporciona
        if (postType != null && !postType.trim().isEmpty()) {
            try {
                post.setPostType(PostType.valueOf(postType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                System.err.println("Tipo de post inválido: " + postType + ", manteniendo el actual");
            }
        }
        
        Post updatedPost = postRepository.save(post);
        System.out.println("Post actualizado exitosamente");
        
        return updatedPost;
    }
    
    /**
     * Elimina un post con validación de permisos
     * 
     * Permisos:
     * - El autor del post puede eliminarlo
     * - Los usuarios con rol ADMINISTRATION pueden eliminar cualquier post
     * 
     * @param id ID del post a eliminar
     * @param userId ID del usuario que intenta eliminar
     * @throws RuntimeException si no tiene permisos o el post no existe
     */
    @Transactional
    public void deletePost(String id, String userId) {
        System.out.println("=== DEBUG: deletePost ===");
        System.out.println("Post ID: " + id + ", User ID: " + userId);
        
        // Buscar el post con su autor
        Optional<Post> postOpt = postRepository.findByIdWithAuthor(id);
        if (!postOpt.isPresent()) {
            throw new RuntimeException("Post no encontrado con ID: " + id);
        }
        
        // Buscar el usuario que intenta eliminar
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
        }
        
        Post post = postOpt.get();
        User user = userOpt.get();
        
        // Verificar permisos
        boolean canDelete = canUserDeletePost(post, user);
        if (!canDelete) {
            throw new RuntimeException("No tienes permisos para eliminar este post. Solo el autor o administradores pueden eliminarlo.");
        }
        
        System.out.println("Usuario autorizado para eliminar el post");
        
        // Eliminar el post (las reacciones y comentarios se eliminan automáticamente por CASCADE)
        postRepository.deleteById(id);
        System.out.println("Post eliminado exitosamente");
    }
    
    /**
     * Verifica si un usuario puede editar un post específico
     * 
     * @param post Post a verificar
     * @param user Usuario que intenta editar
     * @return true si puede editar, false en caso contrario
     */
    private boolean canUserEditPost(Post post, User user) {
        // El autor siempre puede editar su propio post
        if (post.getAuthor().getId().equals(user.getId())) {
            System.out.println("Usuario es el autor del post - permitido");
            return true;
        }
        
        // Administradores pueden editar cualquier post
        if (user.getRole() == com.uniway.entity.UserRole.ADMINISTRATION) {
            System.out.println("Usuario es administrador - permitido");
            return true;
        }
        
        System.out.println("Usuario no tiene permisos para editar");
        return false;
    }
    
    /**
     * Verifica si un usuario puede eliminar un post específico
     * 
     * @param post Post a verificar
     * @param user Usuario que intenta eliminar
     * @return true si puede eliminar, false en caso contrario
     */
    private boolean canUserDeletePost(Post post, User user) {
        // El autor siempre puede eliminar su propio post
        if (post.getAuthor().getId().equals(user.getId())) {
            System.out.println("Usuario es el autor del post - permitido");
            return true;
        }
        
        // Administradores pueden eliminar cualquier post
        if (user.getRole() == com.uniway.entity.UserRole.ADMINISTRATION) {
            System.out.println("Usuario es administrador - permitido");
            return true;
        }
        
        System.out.println("Usuario no tiene permisos para eliminar");
        return false;
    }
    
    /**
     * Implementa el sistema de toggle para likes en posts
     * 
     * Lógica del toggle:
     * - Si el usuario no ha reaccionado: crea un LIKE
     * - Si el usuario ya tenía LIKE: elimina la reacción (toggle off)
     * - Si el usuario tenía DISLIKE: cambia a LIKE
     * 
     * Después de cualquier cambio, actualiza automáticamente los contadores
     * del post (like_count, dislike_count) basándose en la tabla reactions.
     * 
     * @param postId ID del post al que se va a dar/quitar like
     * @param userId ID del usuario que hace la acción
     * @return Post actualizado con los nuevos contadores
     * @throws RuntimeException si el post o usuario no existen
     */
    @Transactional
    public Post toggleLike(String postId, String userId) {
        System.out.println("=== DEBUG: toggleLike ===");
        System.out.println("Post ID: " + postId + ", User ID: " + userId);
        
        // Verificar que el post existe y cargar su autor para evitar LazyInitializationException
        Optional<Post> postOpt = postRepository.findByIdWithAuthor(postId);
        if (!postOpt.isPresent()) {
            throw new RuntimeException("Post no encontrado con ID: " + postId);
        }
        
        // Verificar que el usuario existe
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
        }
        
        Post post = postOpt.get();
        User user = userOpt.get();
        
        // Buscar si el usuario ya tiene una reacción para este post
        Optional<Reaction> existingReaction = reactionRepository.findByUserIdAndPostId(userId, postId);
        
        if (existingReaction.isPresent()) {
            Reaction reaction = existingReaction.get();
            
            if (reaction.getType() == ReactionType.LIKE) {
                // Usuario ya dio like, remover el like
                System.out.println("Removiendo like existente");
                reactionRepository.delete(reaction);
            } else {
                // Usuario tenía dislike, cambiar a like
                System.out.println("Cambiando dislike a like");
                reaction.setType(ReactionType.LIKE);
                reactionRepository.save(reaction);
            }
        } else {
            // Usuario no ha reaccionado, crear nuevo like
            System.out.println("Creando nuevo like");
            Reaction newReaction = new Reaction();
            newReaction.setId(UUID.randomUUID().toString());
            newReaction.setUser(user);
            newReaction.setPost(post);
            newReaction.setType(ReactionType.LIKE);
            reactionRepository.save(newReaction);
        }
        
        // Actualizar contadores del post
        updatePostCounters(post);
        
        return postRepository.save(post);
    }
    
    @Transactional
    public Post toggleDislike(String postId, String userId) {
        System.out.println("=== DEBUG: toggleDislike ===");
        System.out.println("Post ID: " + postId + ", User ID: " + userId);
        
        // Verificar que el post existe
        Optional<Post> postOpt = postRepository.findByIdWithAuthor(postId);
        if (!postOpt.isPresent()) {
            throw new RuntimeException("Post no encontrado con ID: " + postId);
        }
        
        // Verificar que el usuario existe
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
        }
        
        Post post = postOpt.get();
        User user = userOpt.get();
        
        // Buscar reacción existente del usuario para este post
        Optional<Reaction> existingReaction = reactionRepository.findByUserIdAndPostId(userId, postId);
        
        if (existingReaction.isPresent()) {
            Reaction reaction = existingReaction.get();
            
            if (reaction.getType() == ReactionType.DISLIKE) {
                // Usuario ya dio dislike, remover el dislike
                System.out.println("Removiendo dislike existente");
                reactionRepository.delete(reaction);
            } else {
                // Usuario tenía like, cambiar a dislike
                System.out.println("Cambiando like a dislike");
                reaction.setType(ReactionType.DISLIKE);
                reactionRepository.save(reaction);
            }
        } else {
            // Usuario no ha reaccionado, crear nuevo dislike
            System.out.println("Creando nuevo dislike");
            Reaction newReaction = new Reaction();
            newReaction.setId(UUID.randomUUID().toString());
            newReaction.setUser(user);
            newReaction.setPost(post);
            newReaction.setType(ReactionType.DISLIKE);
            reactionRepository.save(newReaction);
        }
        
        // Actualizar contadores del post
        updatePostCounters(post);
        
        return postRepository.save(post);
    }
    
    private void updatePostCounters(Post post) {
        // Contar likes, dislikes y comentarios desde las tablas correspondientes
        long likeCount = reactionRepository.countByPostIdAndType(post.getId(), ReactionType.LIKE);
        long dislikeCount = reactionRepository.countByPostIdAndType(post.getId(), ReactionType.DISLIKE);
        long commentCount = commentRepository.countApprovedCommentsByPostId(post.getId());
        
        post.setLikeCount((int) likeCount);
        post.setDislikeCount((int) dislikeCount);
        post.setCommentCount((int) commentCount);
        
        System.out.println("Contadores actualizados - Likes: " + likeCount + ", Dislikes: " + dislikeCount + ", Comments: " + commentCount);
    }
    
    public Post toggleSave(String postId, String userId) {
        // Implementación simplificada - en producción manejarías los posts guardados
        Optional<Post> postOpt = postRepository.findById(postId);
        if (!postOpt.isPresent()) {
            throw new RuntimeException("Post no encontrado con ID: " + postId);
        }
        
        return postOpt.get();
    }
    
    public Post togglePin(String postId, String userId) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (!postOpt.isPresent()) {
            throw new RuntimeException("Post no encontrado con ID: " + postId);
        }
        
        Post post = postOpt.get();
        post.setIsPinned(!post.getIsPinned());
        return postRepository.save(post);
    }
    
    public long countAllPosts() {
        try {
            return postRepository.count();
        } catch (Exception e) {
            System.err.println("Error contando posts: " + e.getMessage());
            return 0;
        }
    }
    
    public long countAllUsers() {
        try {
            return userRepository.count();
        } catch (Exception e) {
            System.err.println("Error contando usuarios: " + e.getMessage());
            return 0;
        }
    }
    
    public List<Post> getAllPostsSimple() {
        try {
            System.out.println("=== DEBUG: getAllPostsSimple ===");
            
            // Usar el método con JOIN FETCH para evitar LazyInitializationException
            List<Post> allPosts = postRepository.findAllWithAuthor();
            System.out.println("Total posts en DB: " + allPosts.size());
            
            if (allPosts.isEmpty()) {
                System.out.println("No hay posts en la base de datos");
                return new java.util.ArrayList<>();
            }
            
            // Filtrar solo posts aprobados
            List<Post> approvedPosts = allPosts.stream()
                .filter(post -> post.getIsApproved() != null && post.getIsApproved())
                .collect(java.util.stream.Collectors.toList());
                
            System.out.println("Posts aprobados: " + approvedPosts.size());
            return approvedPosts;
            
        } catch (Exception e) {
            System.err.println("Error en getAllPostsSimple: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
    
    public com.uniway.dto.PostDto convertToDto(Post post) {
        return convertToDto(post, null);
    }
    
    public com.uniway.dto.PostDto convertToDto(Post post, String currentUserId) {
        com.uniway.dto.PostDto dto = new com.uniway.dto.PostDto();
        dto.setId(post.getId());
        dto.setAuthorId(post.getAuthor().getId());
        dto.setAuthorName(post.getAuthor().getFullName());
        dto.setAuthorRole(post.getAuthor().getRole().toString());
        dto.setContent(post.getContent());
        dto.setPostType(post.getPostType());
        dto.setPriority(post.getPriority());
        dto.setIsPinned(post.getIsPinned());
        dto.setIsAlert(post.getIsAlert());
        dto.setIsApproved(post.getIsApproved());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setLikeCount(post.getLikeCount() != null ? post.getLikeCount().longValue() : 0L);
        dto.setDislikeCount(post.getDislikeCount() != null ? post.getDislikeCount().longValue() : 0L);
        dto.setCommentCount(post.getCommentCount() != null ? post.getCommentCount().longValue() : 0L);
        
        // Verificar si el usuario actual ha reaccionado al post
        if (currentUserId != null) {
            Optional<Reaction> userReaction = reactionRepository.findByUserIdAndPostId(currentUserId, post.getId());
            if (userReaction.isPresent()) {
                ReactionType reactionType = userReaction.get().getType();
                dto.setIsLiked(reactionType == ReactionType.LIKE);
                dto.setIsDisliked(reactionType == ReactionType.DISLIKE);
            } else {
                dto.setIsLiked(false);
                dto.setIsDisliked(false);
            }
        } else {
            dto.setIsLiked(false);
            dto.setIsDisliked(false);
        }
        

        return dto;
    }
}
