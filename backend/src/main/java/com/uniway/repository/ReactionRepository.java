package com.uniway.repository;

import com.uniway.entity.Reaction;
import com.uniway.entity.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ReactionRepository - Repositorio para operaciones de base de datos con la entidad Reaction
 * 
 * Este repositorio maneja el sistema de reacciones inteligente del foro, donde cada usuario
 * puede dar solo una reacción por post (like o dislike).
 * 
 * Funcionalidades del sistema de reacciones:
 * - Verificar si un usuario ya reaccionó a un post
 * - Contar likes y dislikes por post para actualizar contadores
 * - Obtener todas las reacciones de un tipo específico
 * - Eliminar reacciones para implementar el toggle
 * 
 * La restricción única (user_id, post_id) en la base de datos garantiza que
 * un usuario solo pueda tener una reacción por post.
 */
@Repository
public interface ReactionRepository extends JpaRepository<Reaction, String> {
    
    /** 
     * Busca la reacción existente de un usuario específico a un post específico
     * Esta consulta es clave para el sistema de toggle de reacciones
     * @param userId ID del usuario
     * @param postId ID del post
     * @return Optional con la reacción si existe, vacío si el usuario no ha reaccionado
     */
    @Query("SELECT r FROM Reaction r WHERE r.user.id = :userId AND r.post.id = :postId")
    Optional<Reaction> findByUserIdAndPostId(@Param("userId") String userId, @Param("postId") String postId);
    
    /** 
     * Cuenta el número de reacciones de un tipo específico para un post
     * Usado para actualizar automáticamente los contadores like_count y dislike_count en posts
     * @param postId ID del post
     * @param type Tipo de reacción (LIKE o DISLIKE)
     * @return Número de reacciones del tipo especificado
     */
    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.post.id = :postId AND r.type = :type")
    long countByPostIdAndType(@Param("postId") String postId, @Param("type") ReactionType type);
    
    /** 
     * Obtiene todas las reacciones de un tipo específico para un post
     * Útil para análisis o para obtener la lista de usuarios que reaccionaron
     * @param postId ID del post
     * @param type Tipo de reacción (LIKE o DISLIKE)
     * @return Lista de reacciones del tipo especificado
     */
    @Query("SELECT r FROM Reaction r WHERE r.post.id = :postId AND r.type = :type")
    java.util.List<Reaction> findByPostIdAndType(@Param("postId") String postId, @Param("type") ReactionType type);
    
    /** 
     * Elimina la reacción de un usuario específico a un post específico
     * Usado cuando el usuario hace toggle para quitar su reacción
     * @param userId ID del usuario
     * @param postId ID del post
     */
    void deleteByUserIdAndPostId(String userId, String postId);
}