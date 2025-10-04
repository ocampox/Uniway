package com.uniway.repository;

import com.uniway.entity.StudentTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentTeacherRepository extends JpaRepository<StudentTeacher, String> {
    
    @Query("SELECT st FROM StudentTeacher st JOIN FETCH st.student WHERE st.student.id = :studentId AND st.isActive = true")
    List<StudentTeacher> findByStudentIdAndActiveTrue(@Param("studentId") String studentId);
    
    @Query("SELECT st FROM StudentTeacher st WHERE st.teacherName = :teacherName AND st.isActive = true")
    List<StudentTeacher> findByTeacherNameAndActiveTrue(@Param("teacherName") String teacherName);
    
    @Query("SELECT st FROM StudentTeacher st WHERE st.student.id = :studentId AND st.teacherName = :teacherName AND st.subject = :subject AND st.isActive = true")
    Optional<StudentTeacher> findByStudentIdAndTeacherNameAndSubject(
        @Param("studentId") String studentId, 
        @Param("teacherName") String teacherName, 
        @Param("subject") String subject
    );
    
    @Query("SELECT st FROM StudentTeacher st WHERE st.student.id = :studentId AND st.teacherName = :teacherName AND st.subject = :subject AND st.semester = :semester AND st.isActive = true")
    Optional<StudentTeacher> findByStudentIdAndTeacherNameAndSubjectAndSemester(
        @Param("studentId") String studentId, 
        @Param("teacherName") String teacherName, 
        @Param("subject") String subject,
        @Param("semester") String semester
    );
    
    @Query("SELECT DISTINCT st.subject FROM StudentTeacher st WHERE st.isActive = true ORDER BY st.subject")
    List<String> findAllActiveSubjects();

    // Nuevos métodos para el sistema de recomendaciones
    
    /**
     * Encuentra recomendaciones activas ordenadas por fecha de creación
     */
    @Query("SELECT st FROM StudentTeacher st WHERE st.isActive = true ORDER BY st.createdAt DESC")
    List<StudentTeacher> findByActiveTrueOrderByCreatedAtDesc();
    
    /**
     * Encuentra recomendaciones activas por materia
     */
    @Query("SELECT st FROM StudentTeacher st WHERE st.subject = :subject AND st.isActive = true")
    List<StudentTeacher> findBySubjectAndActiveTrue(@Param("subject") String subject);
    
    /**
     * Cuenta recomendaciones activas de un estudiante
     */
    @Query("SELECT COUNT(st) FROM StudentTeacher st WHERE st.student.id = :studentId AND st.isActive = true")
    long countByStudentIdAndActiveTrue(@Param("studentId") String studentId);
    
    /**
     * Cuenta todas las recomendaciones activas
     */
    @Query("SELECT COUNT(st) FROM StudentTeacher st WHERE st.isActive = true")
    long countByActiveTrue();
    
    /**
     * Obtiene todas las materias distintas que tienen recomendaciones activas
     */
    @Query("SELECT DISTINCT st.subject FROM StudentTeacher st WHERE st.isActive = true ORDER BY st.subject")
    List<String> findDistinctSubjectsByActiveTrue();
    
    /**
     * Obtiene todos los nombres de profesores distintos que tienen recomendaciones activas
     */
    @Query("SELECT DISTINCT st.teacherName FROM StudentTeacher st WHERE st.isActive = true ORDER BY st.teacherName")
    List<String> findDistinctTeacherNames();
}
