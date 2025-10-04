package com.uniway.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad StudentTeacher - Representa la relación entre estudiante, profesor y materia
 * 
 * Esta clase mapea la tabla 'student_teachers' que almacena qué profesores
 * tiene cada estudiante y en qué materias.
 */
@Entity
@Table(name = "student_teachers")
@EntityListeners(AuditingEntityListener.class)
public class StudentTeacher {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "teacher_name", nullable = false)
    private String teacherName;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "semester")
    private String semester;

    @Column(name = "year")
    private Integer year;

    @Column(name = "reference", columnDefinition = "TEXT")
    private String reference;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public StudentTeacher() {}

    public StudentTeacher(String id, User student, String teacherName, String subject, String semester, Integer year, String reference) {
        this.id = id;
        this.student = student;
        this.teacherName = teacherName;
        this.subject = subject;
        this.semester = semester;
        this.year = year;
        this.reference = reference;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}
