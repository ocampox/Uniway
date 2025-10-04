package com.uniway.controller;

import com.uniway.dto.RecommendationStatsResponse;
import com.uniway.dto.TeacherRecommendationDto;
import com.uniway.entity.StudentTeacher;
import com.uniway.service.TeacherRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teacher-recommendations")
@Tag(name = "Teacher Recommendations", description = "Sistema de recomendaciones de profesores con reacciones")
@CrossOrigin(origins = "*")
public class TeacherRecommendationController {

    @Autowired
    private TeacherRecommendationService recommendationService;

    @GetMapping
    @Operation(summary = "Obtener todas las recomendaciones con reacciones")
    public ResponseEntity<List<TeacherRecommendationDto>> getAllRecommendations(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String subject) {
        
        List<TeacherRecommendationDto> recommendations = recommendationService.getAllRecommendationsWithReactions(userId, subject);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtener recomendaciones de un usuario específico")
    public ResponseEntity<List<TeacherRecommendationDto>> getUserRecommendations(@PathVariable String userId) {
        List<TeacherRecommendationDto> recommendations = recommendationService.getUserRecommendations(userId);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping
    @Operation(summary = "Crear nueva recomendación de profesor")
    public ResponseEntity<?> createRecommendation(@RequestBody CreateRecommendationRequest request) {
        try {
            StudentTeacher recommendation = recommendationService.createRecommendation(
                request.studentId,
                request.teacherName,
                request.subject,
                request.semester,
                request.year,
                request.reference
            );

            TeacherRecommendationDto dto = recommendationService.convertToDto(recommendation, request.studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("recommendation", dto);
            response.put("message", "Recomendación creada exitosamente");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/with-rating")
    @Operation(summary = "Crear nueva recomendación de profesor con calificación")
    public ResponseEntity<?> createRecommendationWithRating(@RequestBody CreateRecommendationWithRatingRequest request) {
        try {
            StudentTeacher recommendation = recommendationService.createRecommendationWithRating(
                request.studentId,
                request.teacherName,
                request.subject,
                request.semester,
                request.year,
                request.reference,
                request.rating
            );

            TeacherRecommendationDto dto = recommendationService.convertToDto(recommendation, request.studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("recommendation", dto);
            response.put("message", "Recomendación con calificación creada exitosamente");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{recommendationId}/like")
    @Operation(summary = "Dar/quitar like a una recomendación")
    public ResponseEntity<?> likeRecommendation(
            @PathVariable String recommendationId,
            @RequestBody ReactionRequest request) {
        try {
            TeacherRecommendationDto updatedRecommendation = recommendationService.toggleLike(recommendationId, request.userId);

            Map<String, Object> response = new HashMap<>();
            response.put("recommendation", updatedRecommendation);
            response.put("message", "Like actualizado correctamente");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{recommendationId}/dislike")
    @Operation(summary = "Dar/quitar dislike a una recomendación")
    public ResponseEntity<?> dislikeRecommendation(
            @PathVariable String recommendationId,
            @RequestBody ReactionRequest request) {
        try {
            TeacherRecommendationDto updatedRecommendation = recommendationService.toggleDislike(recommendationId, request.userId);

            Map<String, Object> response = new HashMap<>();
            response.put("recommendation", updatedRecommendation);
            response.put("message", "Dislike actualizado correctamente");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{recommendationId}")
    @Operation(summary = "Eliminar recomendación")
    public ResponseEntity<?> deleteRecommendation(
            @PathVariable String recommendationId,
            @RequestParam String userId) {
        try {
            recommendationService.deleteRecommendation(recommendationId, userId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Recomendación eliminada exitosamente");
            response.put("recommendationId", recommendationId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/stats/{userId}")
    @Operation(summary = "Obtener estadísticas de recomendaciones")
    public ResponseEntity<RecommendationStatsResponse> getRecommendationStats(@PathVariable String userId) {
        try {
            RecommendationStatsResponse stats = recommendationService.getRecommendationStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/subjects")
    @Operation(summary = "Obtener todas las materias con recomendaciones")
    public ResponseEntity<List<String>> getSubjectsWithRecommendations() {
        List<String> subjects = recommendationService.getSubjectsWithRecommendations();
        return ResponseEntity.ok(subjects);
    }

    // Request/Response classes
    public static class CreateRecommendationRequest {
        public String studentId;
        public String teacherName;
        public String subject;
        public String semester;
        public Integer year;
        public String reference;
    }

    public static class CreateRecommendationWithRatingRequest {
        public String studentId;
        public String teacherName;
        public String subject;
        public String semester;
        public Integer year;
        public String reference;
        public Integer rating;
    }

    public static class ReactionRequest {
        public String userId;
    }

    public static class DeleteRecommendationRequest {
        public String userId;
    }
}