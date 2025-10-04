package com.uniway.service;

import com.uniway.entity.User;
import com.uniway.entity.UserRole;
import com.uniway.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    public User register(String email, String password, UserRole role, String fullName, String studentId, String program) {
        // Verificar si el usuario ya existe
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("El correo electrónico ya está registrado");
        }
        
        // Verificar si el studentId ya existe (solo para estudiantes)
        if (role == UserRole.STUDENT && studentId != null && !studentId.isEmpty()) {
            if (userRepository.findByStudentId(studentId).isPresent()) {
                throw new RuntimeException("El número de estudiante ya está registrado");
            }
        }
        
        // Crear nuevo usuario
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setFullName(fullName);
        user.setStudentId(studentId);
        user.setProgram(program);
        user.setIsActive(true);
        
        return userRepository.save(user);
    }
    
    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));
        
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }
        
        if (!user.getIsActive()) {
            throw new RuntimeException("Usuario inactivo");
        }
        
        return user;
    }
    
    public String generateToken(User user) {
        return jwtService.generateToken(user);
    }
    
    public void updatePassword(User user, String newPassword) {
        // Encriptar la nueva contraseña
        String encryptedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(encryptedPassword);
        userRepository.save(user);
    }
}










