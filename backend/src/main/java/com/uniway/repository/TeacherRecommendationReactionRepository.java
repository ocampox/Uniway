package com.uniway.repository;

import com.uniway.entity.ReactionType;
import com.uniway.entity.TeacherRecommendationReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * TeacherRecommendationReactionRepository - Repositorio para operaciones de base de datos 
 * con las reacciones a recomendaciones de profesores
 * 
 * Proporciona métodos para gestionar likes y dislikes en recomendaciones de profesores,
 * similar al sistema de reacciones de posts pero específico para recomendaciones.
 */
@Repository
public interface TeacherRecommendationReactionRepository extends JpaRepository<TeacherRecommendationReaction, String> {

    /**
     * Busca una reacción específica de un usuario a una recomendación
     * @param userId ID del usuario
     * @param recommendationId ID de la recomendación (StudentTeacher)
     * @return Optional con la reacción si existe
     */
    @Query("SELECT trr FROM TeacherRecommendationReaction trr WHERE trr.user.id = :userId AND trr.recommendation.id = :recommendationId")
    Optional<TeacherRecommendationReaction> findByUserIdAndRecommendationId(@Param("userId") String userId, @Param("recommendationId") String recommendationId);

    /**
     * Cuenta las reacciones de un tipo específico para una recomendación
     * @param recommendationId ID de la recomendación
     * @param reactionType Tipo de reacción (LIKE o DISLIKE)
     * @return Número de reacciones de ese tipo
     */
    @Query("SELECT COUNT(trr) FROM TeacherRecommendationReaction trr WHERE trr.recommendation.id = :recommendationId AND trr.reactionType = :reactionType")
    long countByRecommendationIdAndReactionType(@Param("recommendationId") String recommendationId, @Param("reactionType") ReactionType reactionType);

    /**
     * Cuenta todas las reacciones para una recomendación específica
     * @param recommendationId ID de la recomendación
     * @return Número total de reacciones
     */
    @Query("SELECT COUNT(trr) FROM TeacherRecommendationReaction trr WHERE trr.recommendation.id = :recommendationId")
    long countByRecommendationId(@Param("recommendationId") String recommendationId);

    /**
     * Cuenta las reacciones recibidas por todas las recomendaciones de un usuario
     * @param studentId ID del estudiante (autor de las recomendaciones)
     * @param reactionType Tipo de reacción a contar
     * @return Número de reacciones de ese tipo recibidas
     */
    @Query("SELECT COUNT(trr) FROM TeacherRecommendationReaction trr WHERE trr.recommendation.student.id = :studentId AND trr.reactionType = :reactionType")
    long countReactionsReceivedByStudent(@Param("studentId") String studentId, @Param("reactionType") ReactionType reactionType);

    /**
     * Elimina todas las reacciones de una recomendación específica
     * @param recommendationId ID de la recomendación
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM TeacherRecommendationReaction trr WHERE trr.recommendation.id = :recommendationId")
    void deleteByRecommendationId(@Param("recommendationId") String recommendationId);
}