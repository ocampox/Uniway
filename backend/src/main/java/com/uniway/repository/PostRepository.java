package com.uniway.repository;

import com.uniway.entity.Post;
import com.uniway.entity.PostType;
import com.uniway.entity.PostPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * PostRepository - Repositorio para operaciones de base de datos con la entidad Post
 * 
 * Este repositorio extiende JpaRepository para obtener operaciones CRUD básicas
 * y define consultas personalizadas para el manejo de publicaciones del foro.
 * 
 * Características importantes:
 * - Todas las consultas usan JOIN FETCH p.author para evitar LazyInitializationException
 * - Se filtran posts aprobados para mostrar solo contenido moderado
 * - Se ordenan por prioridad (posts fijados primero) y fecha de creación
 * - Incluye consultas para diferentes tipos y prioridades de posts
 * 
 * Consultas disponibles:
 * - Obtener posts aprobados ordenados
 * - Filtrar por tipo, prioridad, autor
 * - Obtener posts de alerta y fijados
 * - Contar posts por autor
 * - Obtener posts pendientes de moderación
 */
@Repository
public interface PostRepository extends JpaRepository<Post, String> {
    
    /** 
     * Obtiene todos los posts aprobados ordenados por prioridad y fecha
     * Los posts fijados (isPinned=true) aparecen primero, luego por fecha descendente
     * Usa JOIN FETCH para cargar el autor y evitar LazyInitializationException
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.isApproved = true ORDER BY p.isPinned DESC, p.createdAt DESC")
    List<Post> findAllApprovedPostsOrderByPinnedAndDate();
    
    /** 
     * Obtiene posts aprobados filtrados por tipo específico
     * @param type Tipo de post (GENERAL, NEWS, ALERT, ANNOUNCEMENT)
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.postType = :type AND p.isApproved = true ORDER BY p.createdAt DESC")
    List<Post> findApprovedPostsByType(@Param("type") PostType type);
    
    /** 
     * Obtiene posts aprobados filtrados por prioridad específica
     * @param priority Prioridad del post (NORMAL, HIGH, URGENT)
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.priority = :priority AND p.isApproved = true ORDER BY p.createdAt DESC")
    List<Post> findApprovedPostsByPriority(@Param("priority") PostPriority priority);
    
    /** 
     * Obtiene posts aprobados de un autor específico
     * @param authorId ID del usuario autor de los posts
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.author.id = :authorId AND p.isApproved = true ORDER BY p.createdAt DESC")
    List<Post> findApprovedPostsByAuthor(@Param("authorId") String authorId);
    
    /** 
     * Obtiene todos los posts marcados como alerta (isAlert=true)
     * Útil para mostrar alertas de seguridad en sección especial
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.isAlert = true AND p.isApproved = true ORDER BY p.createdAt DESC")
    List<Post> findAllAlertPosts();
    
    /** 
     * Obtiene todos los posts fijados (isPinned=true)
     * Los posts fijados aparecen destacados en la parte superior
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.isPinned = true AND p.isApproved = true ORDER BY p.createdAt DESC")
    List<Post> findAllPinnedPosts();
    
    /** 
     * Obtiene posts creados desde una fecha específica
     * @param since Fecha desde la cual buscar posts
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.createdAt >= :since AND p.isApproved = true ORDER BY p.createdAt DESC")
    List<Post> findPostsSince(@Param("since") LocalDateTime since);
    
    /** 
     * Cuenta el número de posts aprobados de un autor específico
     * @param authorId ID del usuario autor
     * @return Número de posts aprobados del autor
     */
    @Query("SELECT COUNT(p) FROM Post p WHERE p.author.id = :authorId AND p.isApproved = true")
    long countApprovedPostsByAuthor(@Param("authorId") String authorId);
    
    /** 
     * Obtiene posts pendientes de aprobación (isApproved=false)
     * Ordenados por fecha ascendente para moderar los más antiguos primero
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.isApproved = false ORDER BY p.createdAt ASC")
    List<Post> findAllPendingPosts();
    
    /** 
     * Obtiene todos los posts con sus autores cargados
     * Útil para operaciones que necesitan acceso al autor sin restricciones de aprobación
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.author")
    List<Post> findAllWithAuthor();
    
    /** 
     * Obtiene un post específico por ID con su autor cargado
     * @param id ID del post a buscar
     * @return Optional con el post si existe, vacío si no existe
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.id = :id")
    Optional<Post> findByIdWithAuthor(@Param("id") String id);
}







