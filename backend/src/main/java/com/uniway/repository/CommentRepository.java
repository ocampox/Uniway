package com.uniway.repository;

import com.uniway.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CommentRepository - Repositorio para operaciones de base de datos con la entidad Comment
 * 
 * Este repositorio maneja todas las operaciones relacionadas con comentarios en el foro.
 * Proporciona consultas optimizadas para obtener comentarios con sus autores cargados.
 * 
 * Características importantes:
 * - Todas las consultas usan JOIN FETCH c.author para evitar LazyInitializationException
 * - Se filtran comentarios aprobados para mostrar solo contenido moderado
 * - Se ordenan por fecha de creación (ascendente para mostrar cronológicamente)
 * - Incluye contadores para actualizar automáticamente los posts
 * 
 * Consultas disponibles:
 * - Obtener comentarios de un post específico
 * - Contar comentarios aprobados por post
 * - Obtener comentarios por autor
 * - Obtener comentarios pendientes de moderación
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    
    /** 
     * Obtiene comentarios aprobados de un post específico ordenados cronológicamente
     * @param postId ID del post del cual obtener comentarios
     * @return Lista de comentarios aprobados con autores cargados
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.post.id = :postId AND c.isApproved = true ORDER BY c.createdAt ASC")
    List<Comment> findApprovedCommentsByPostId(@Param("postId") String postId);
    
    /** 
     * Obtiene todos los comentarios de un post (aprobados y no aprobados)
     * @param postId ID del post del cual obtener comentarios
     * @return Lista de todos los comentarios con autores cargados
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.post.id = :postId ORDER BY c.createdAt ASC")
    List<Comment> findAllCommentsByPostId(@Param("postId") String postId);
    
    /** 
     * Cuenta el número de comentarios aprobados de un post específico
     * Usado para actualizar automáticamente el contador comment_count en la tabla posts
     * @param postId ID del post del cual contar comentarios
     * @return Número de comentarios aprobados
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.isApproved = true")
    long countApprovedCommentsByPostId(@Param("postId") String postId);
    
    /** 
     * Obtiene todos los comentarios de un autor específico
     * @param authorId ID del usuario autor de los comentarios
     * @return Lista de comentarios del autor ordenados por fecha descendente
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.author.id = :authorId ORDER BY c.createdAt DESC")
    List<Comment> findCommentsByAuthorId(@Param("authorId") String authorId);
    
    /** 
     * Obtiene comentarios pendientes de aprobación (isApproved=false)
     * Ordenados por fecha ascendente para moderar los más antiguos primero
     * @return Lista de comentarios pendientes con autores cargados
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.isApproved = false ORDER BY c.createdAt ASC")
    List<Comment> findPendingComments();
}