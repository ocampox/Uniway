package com.uniway.service;

import com.uniway.dto.RecommendationStatsResponse;
import com.uniway.dto.TeacherRecommendationDto;
import com.uniway.entity.*;
import com.uniway.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * TeacherRecommendationService - Servicio para gestión de recomendaciones de profesores
 * 
 * Este servicio maneja toda la lógica de negocio relacionada con recomendaciones
 * de profesores, incluyendo creación, reacciones (likes/dislikes), y consultas.
 */
@Service
@Transactional
public class TeacherRecommendationService {

    @Autowired
    private StudentTeacherRepository studentTeacherRepository;

    @Autowired
    private TeacherRecommendationReactionRepository reactionRepository;

    // TeacherRepository eliminado - Sistema simplificado

    @Autowired
    private UserRepository userRepository;

    /**
     * Obtiene todas las recomendaciones con sus reacciones
     */
    public List<TeacherRecommendationDto> getAllRecommendationsWithReactions(String currentUserId, String subjectFilter) {
        List<StudentTeacher> recommendations;
        
        if (subjectFilter != null && !subjectFilter.trim().isEmpty()) {
            recommendations = studentTeacherRepository.findBySubjectAndActiveTrue(subjectFilter);
        } else {
            recommendations = studentTeacherRepository.findByActiveTrueOrderByCreatedAtDesc();
        }

        return recommendations.stream()
                .map(rec -> convertToDto(rec, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las recomendaciones de un usuario específico
     */
    public List<TeacherRecommendationDto> getUserRecommendations(String userId) {
        List<StudentTeacher> recommendations = studentTeacherRepository.findByStudentIdAndActiveTrue(userId);
        
        return recommendations.stream()
                .map(rec -> convertToDto(rec, userId))
                .collect(Collectors.toList());
    }

    /**
     * Crea una nueva recomendación de profesor
     */
    public StudentTeacher createRecommendation(String studentId, String teacherName, String subject, 
                                             String semester, Integer year, String reference) {
        // Buscar estudiante
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        
        
        Optional<StudentTeacher> existing = studentTeacherRepository.findByStudentIdAndTeacherNameAndSubjectAndSemester(
                studentId, teacherName, subject, semester
        );
        
        if (existing.isPresent()) {
            throw new RuntimeException("Ya has publicado una recomendación para este profesor en esta materia y semestre");
        }
        

        // Crear nueva recomendación directamente
        StudentTeacher recommendation = new StudentTeacher();
        recommendation.setId(UUID.randomUUID().toString());
        recommendation.setStudent(student);
        recommendation.setTeacherName(teacherName);
        recommendation.setSubject(subject);
        recommendation.setSemester(semester);
        recommendation.setYear(year);
        recommendation.setReference(reference);
        recommendation.setIsActive(true);

        return studentTeacherRepository.save(recommendation);
    }

    /**
     * Crea una nueva recomendación de profesor con calificación
     */
    public StudentTeacher createRecommendationWithRating(String studentId, String teacherName, String subject, 
                                                       String semester, Integer year, String reference, Integer rating) {
        // Validar rating
        if (rating == null || rating < 1 || rating > 5) {
            throw new RuntimeException("La calificación debe estar entre 1 y 5 estrellas");
        }

        // Buscar estudiante
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        // TEMPORAL: Validación de duplicados comentada hasta corregir constraint de BD
        /*
        Optional<StudentTeacher> existing = studentTeacherRepository.findByStudentIdAndTeacherNameAndSubjectAndSemester(
                studentId, teacherName, subject, semester
        );
        
        if (existing.isPresent()) {
            throw new RuntimeException("Ya has publicado una recomendación para este profesor en esta materia y semestre");
        }
        */

        // Crear nueva recomendación con rating
        StudentTeacher recommendation = new StudentTeacher();
        recommendation.setId(UUID.randomUUID().toString());
        recommendation.setStudent(student);
        recommendation.setTeacherName(teacherName);
        recommendation.setSubject(subject);
        recommendation.setSemester(semester);
        recommendation.setYear(year);
        recommendation.setReference(reference);
        recommendation.setRating(rating); // Agregar rating
        recommendation.setIsActive(true);

        return studentTeacherRepository.save(recommendation);
    }

    /**
     * Toggle like en una recomendación
     */
    public TeacherRecommendationDto toggleLike(String recommendationId, String userId) {
        return toggleReaction(recommendationId, userId, ReactionType.LIKE);
    }

    /**
     * Toggle dislike en una recomendación
     */
    public TeacherRecommendationDto toggleDislike(String recommendationId, String userId) {
        return toggleReaction(recommendationId, userId, ReactionType.DISLIKE);
    }

    /**
     * Lógica común para toggle de reacciones
     */
    private TeacherRecommendationDto toggleReaction(String recommendationId, String userId, ReactionType newReactionType) {
        // Verificar que la recomendación existe
        StudentTeacher recommendation = studentTeacherRepository.findById(recommendationId)
                .orElseThrow(() -> new RuntimeException("Recomendación no encontrada"));

        // Verificar que el usuario existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar que el usuario no esté reaccionando a su propia recomendación
        if (recommendation.getStudent().getId().equals(userId)) {
            throw new RuntimeException("No puedes reaccionar a tus propias recomendaciones");
        }

        // Buscar reacción existente
        Optional<TeacherRecommendationReaction> existingReaction = 
                reactionRepository.findByUserIdAndRecommendationId(userId, recommendationId);

        if (existingReaction.isPresent()) {
            TeacherRecommendationReaction reaction = existingReaction.get();
            
            if (reaction.getReactionType() == newReactionType) {
                // Usuario ya tiene esta reacción, eliminarla (toggle off)
                reactionRepository.delete(reaction);
            } else {
                // Usuario tiene reacción diferente, cambiarla
                reaction.setReactionType(newReactionType);
                reactionRepository.save(reaction);
            }
        } else {
            // Usuario no ha reaccionado, crear nueva reacción
            TeacherRecommendationReaction newReaction = new TeacherRecommendationReaction();
            newReaction.setId(UUID.randomUUID().toString());
            newReaction.setRecommendation(recommendation);
            newReaction.setUser(user);
            newReaction.setReactionType(newReactionType);
            reactionRepository.save(newReaction);
        }

        // Retornar la recomendación actualizada con nuevos contadores
        return convertToDto(recommendation, userId);
    }

    /**
     * Elimina una recomendación con validación de permisos
     * Solo el autor de la recomendación o un administrador pueden eliminarla
     */
    public void deleteRecommendation(String recommendationId, String userId) {
        // Buscar la recomendación
        StudentTeacher recommendation = studentTeacherRepository.findById(recommendationId)
                .orElseThrow(() -> new RuntimeException("Recomendación no encontrada"));
        
        // Buscar el usuario que intenta eliminar
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Validar permisos: solo el autor o un administrador pueden eliminar
        boolean isAuthor = recommendation.getStudent().getId().equals(userId);
        boolean isAdmin = user.getRole() == UserRole.ADMINISTRATION;
        
        if (!isAuthor && !isAdmin) {
            throw new RuntimeException("No tienes permisos para eliminar esta recomendación");
        }
        
        // Eliminar todas las reacciones asociadas primero
        reactionRepository.deleteByRecommendationId(recommendationId);
        
        // Eliminar la recomendación (hard delete)
        studentTeacherRepository.delete(recommendation);
    }

    /**
     * Obtiene estadísticas de recomendaciones para un usuario
     */
    public RecommendationStatsResponse getRecommendationStats(String userId) {
        // Contar recomendaciones del usuario
        int myRecommendationsCount = (int) studentTeacherRepository.countByStudentIdAndActiveTrue(userId);
        
        // Contar likes y dislikes recibidos
        int myTotalLikesReceived = (int) reactionRepository.countReactionsReceivedByStudent(userId, ReactionType.LIKE);
        int myTotalDislikesReceived = (int) reactionRepository.countReactionsReceivedByStudent(userId, ReactionType.DISLIKE);
        
        // Estadísticas globales
        int totalRecommendationsCount = (int) studentTeacherRepository.countByActiveTrue();
        int totalReactionsCount = (int) reactionRepository.count();

        return new RecommendationStatsResponse(
                myRecommendationsCount,
                myTotalLikesReceived,
                myTotalDislikesReceived,
                totalRecommendationsCount,
                totalReactionsCount
        );
    }

    /**
     * Obtiene todas las materias que tienen recomendaciones
     */
    public List<String> getSubjectsWithRecommendations() {
        return studentTeacherRepository.findDistinctSubjectsByActiveTrue();
    }

    // Métodos eliminados: findOrCreateTeacher y generateEmailFromName
    // Sistema simplificado sin auto-creación de profesores

    /**
     * Convierte StudentTeacher a TeacherRecommendationDto con información completa
     */
    public TeacherRecommendationDto convertToDto(StudentTeacher recommendation, String currentUserId) {
        TeacherRecommendationDto dto = new TeacherRecommendationDto();
        
        // Información básica
        dto.setId(recommendation.getId());
        dto.setStudentId(recommendation.getStudent().getId());
        dto.setStudentName(recommendation.getStudent().getFullName());
        dto.setTeacherName(recommendation.getTeacherName());
        dto.setSubject(recommendation.getSubject());
        dto.setSemester(recommendation.getSemester());
        dto.setYear(recommendation.getYear());
        dto.setReference(recommendation.getReference());
        dto.setRating(recommendation.getRating());
        dto.setIsActive(recommendation.getIsActive());
        dto.setCreatedAt(recommendation.getCreatedAt());
        
        // Contadores de reacciones
        dto.setLikeCount(reactionRepository.countByRecommendationIdAndReactionType(recommendation.getId(), ReactionType.LIKE));
        dto.setDislikeCount(reactionRepository.countByRecommendationIdAndReactionType(recommendation.getId(), ReactionType.DISLIKE));
        dto.setTotalReactions(reactionRepository.countByRecommendationId(recommendation.getId()));
        
        // Estado de reacción del usuario actual
        if (currentUserId != null) {
            Optional<TeacherRecommendationReaction> userReaction = 
                    reactionRepository.findByUserIdAndRecommendationId(currentUserId, recommendation.getId());
            
            if (userReaction.isPresent()) {
                ReactionType reactionType = userReaction.get().getReactionType();
                dto.setIsLiked(reactionType == ReactionType.LIKE);
                dto.setIsDisliked(reactionType == ReactionType.DISLIKE);
                dto.setUserReaction(reactionType.toString());
            } else {
                dto.setIsLiked(false);
                dto.setIsDisliked(false);
                dto.setUserReaction(null);
            }
        } else {
            dto.setIsLiked(false);
            dto.setIsDisliked(false);
            dto.setUserReaction(null);
        }
        
        return dto;
    }
}