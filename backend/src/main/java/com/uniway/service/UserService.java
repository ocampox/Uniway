package com.uniway.service;

import com.uniway.entity.User;
import com.uniway.entity.UserRole;
import com.uniway.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User createUser(User user) {
        // Generar ID único si no existe
        if (user.getId() == null || user.getId().isEmpty()) {
            user.setId(UUID.randomUUID().toString());
        }
        
        // Encriptar contraseña
        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        
        // Establecer valores por defecto
        if (user.getRole() == null) {
            user.setRole(UserRole.STUDENT);
        }
        
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }
        
        return userRepository.save(user);
    }
    
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findByStudentId(String studentId) {
        return userRepository.findByStudentId(studentId);
    }
    
    public Optional<User> findActiveUserByEmail(String email) {
        return userRepository.findActiveUserByEmail(email);
    }
    
    public List<User> findActiveUsersByRole(UserRole role) {
        return userRepository.findActiveUsersByRole(role);
    }
    
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public boolean existsByStudentId(String studentId) {
        return userRepository.existsByStudentId(studentId);
    }
    
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    
    public User deactivateUser(String id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsActive(false);
            return userRepository.save(user);
        }
        throw new RuntimeException("Usuario no encontrado con ID: " + id);
    }
    
    public User activateUser(String id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsActive(true);
            return userRepository.save(user);
        }
        throw new RuntimeException("Usuario no encontrado con ID: " + id);
    }
    
    public com.uniway.dto.UserDto convertToDto(User user) {
        return new com.uniway.dto.UserDto(
            user.getId(),
            user.getEmail(),
            user.getRole(),
            user.getFullName(),
            user.getStudentId(),
            user.getProgram(),
            user.getProfileImageUrl(),
            user.getPhone(),
            user.getAddress(),
            user.getIsActive(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
