package com.uniway.controller;

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
import java.util.stream.Collectors;

/**
 * StudentTeacherCompatibilityController - Controlador de compatibilidad
 * 
 * Mantiene las rutas anteriores para compatibilidad con la app Android,
 * pero internamente usa el nuevo sistema de recomendaciones simplificado.
 */
@RestController
@RequestMapping("/student-teachers")
@Tag(name = "Student-Teachers (Compatibility)", description = "Controlador de compatibilidad para app Android")
@CrossOrigin(origins = "*")
public class StudentTeacherCompatibilityController {

    @Autowired
    private TeacherRecommendationService recommendationService;

    @PostMapping
    @Operation(summary = "Agregar profesor (crear recomendación)")
    public ResponseEntity<?> addTeacherToStudent(@RequestBody AddTeacherRequest request) {
        try {
            // Usar el nuevo sistema de recomendaciones
            StudentTeacher recommendation = recommendationService.createRecommendation(
                request.studentId,
                extractTeacherNameFromEmail(request.teacherEmail),
                request.subject,
                request.semester,
                request.year,
                "Recomendación creada desde la app móvil" // Referencia por defecto
            );

            // Convertir a formato compatible
            StudentTeacherResponse response = convertToCompatibleResponse(recommendation);

            Map<String, Object> result = new HashMap<>();
            result.put("studentTeacher", response);
            result.put("message", "Profesor agregado exitosamente");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Obtener profesores de un estudiante (recomendaciones)")
    public ResponseEntity<List<StudentTeacherResponse>> getStudentTeachers(@PathVariable String studentId) {
        try {
            List<TeacherRecommendationDto> recommendations = recommendationService.getUserRecommendations(studentId);
            
            List<StudentTeacherResponse> response = recommendations.stream()
                .map(this::convertToCompatibleResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    @DeleteMapping
    @Operation(summary = "Remover profesor (eliminar recomendación)")
    public ResponseEntity<?> removeTeacherFromStudent(@RequestBody RemoveTeacherRequest request) {
        try {
            recommendationService.deleteRecommendation(request.recommendationId, request.studentId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Profesor removido exitosamente");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/subjects")
    @Operation(summary = "Obtener materias disponibles")
    public ResponseEntity<List<String>> getAllSubjects() {
        List<String> subjects = recommendationService.getSubjectsWithRecommendations();
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/test")
    @Operation(summary = "Endpoint de prueba")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Student-Teacher compatibility controller is working!");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    // Métodos de utilidad
    private String extractTeacherNameFromEmail(String email) {
        // Extraer nombre del email: maria.gonzalez@pascualbravo.edu.co -> María González
        String localPart = email.split("@")[0];
        String[] parts = localPart.split("\\.");
        
        StringBuilder name = new StringBuilder();
        for (String part : parts) {
            if (name.length() > 0) name.append(" ");
            name.append(part.substring(0, 1).toUpperCase()).append(part.substring(1));
        }
        
        return name.toString();
    }

    private StudentTeacherResponse convertToCompatibleResponse(StudentTeacher recommendation) {
        return new StudentTeacherResponse(
            recommendation.getId(),
            recommendation.getStudent().getId(),
            null, // teacherId ya no existe
            recommendation.getTeacherName(),
            null, // teacherEmail ya no existe
            recommendation.getSubject(),
            recommendation.getSemester(),
            recommendation.getYear(),
            recommendation.getIsActive(),
            recommendation.getCreatedAt().toString()
        );
    }

    private StudentTeacherResponse convertToCompatibleResponse(TeacherRecommendationDto dto) {
        return new StudentTeacherResponse(
            dto.getId(),
            dto.getStudentId(),
            null, // teacherId ya no existe
            dto.getTeacherName(),
            null, // teacherEmail ya no existe
            dto.getSubject(),
            dto.getSemester(),
            dto.getYear(),
            dto.getIsActive(),
            dto.getCreatedAt().toString()
        );
    }

    // Request/Response classes para compatibilidad
    public static class AddTeacherRequest {
        public String studentId;
        public String teacherEmail;
        public String subject;
        public String semester;
        public Integer year;
    }

    public static class RemoveTeacherRequest {
        public String studentId;
        public String recommendationId;
        public String subject;
    }

    public static class StudentTeacherResponse {
        public String id;
        public String studentId;
        public String teacherId;
        public String teacherName;
        public String teacherEmail;
        public String subject;
        public String semester;
        public Integer year;
        public Boolean isActive;
        public String createdAt;

        public StudentTeacherResponse(String id, String studentId, String teacherId, 
                                    String teacherName, String teacherEmail, String subject, 
                                    String semester, Integer year, Boolean isActive, String createdAt) {
            this.id = id;
            this.studentId = studentId;
            this.teacherId = teacherId;
            this.teacherName = teacherName;
            this.teacherEmail = teacherEmail;
            this.subject = subject;
            this.semester = semester;
            this.year = year;
            this.isActive = isActive;
            this.createdAt = createdAt;
        }
    }
}