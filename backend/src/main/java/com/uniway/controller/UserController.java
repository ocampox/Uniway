package com.uniway.controller;

import com.uniway.dto.UserDto;
import com.uniway.entity.User;
import com.uniway.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@Tag(name = "Usuarios", description = "Endpoints para gestión de usuarios")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<UserDto> getUserById(@PathVariable String id) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isPresent()) {
            UserDto userDto = userService.convertToDto(userOpt.get());
            return ResponseEntity.ok(userDto);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/profile")
    @Operation(summary = "Obtener perfil del usuario actual")
    public ResponseEntity<UserDto> getCurrentUserProfile(@RequestHeader("Authorization") String token) {
        try {
            // Obtener el primer usuario disponible (modo desarrollo)
            List<User> users = userService.findAllUsers();
            if (!users.isEmpty()) {
                User user = users.get(0); // Usar el primer usuario
                UserDto userDto = userService.convertToDto(user);
                return ResponseEntity.ok(userDto);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Error obteniendo perfil: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/profile")
    @Operation(summary = "Actualizar perfil del usuario")
    public ResponseEntity<?> updateUserProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UpdateProfileRequest request) {
        try {
            System.out.println("=== DEBUG: Actualizando perfil ===");
            System.out.println("FullName: " + request.getFullName());
            System.out.println("Phone: " + request.getPhone());
            System.out.println("Address: " + request.getAddress());
            System.out.println("Program: " + request.getProgram());
            
            // Obtener el primer usuario disponible (modo desarrollo)
            List<User> users = userService.findAllUsers();
            if (users.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = users.get(0); // Usar el primer usuario
            System.out.println("Usuario encontrado: " + user.getFullName() + " (" + user.getId() + ")");
            
            // Actualizar campos si se proporcionan
            if (request.getFullName() != null) {
                user.setFullName(request.getFullName());
                System.out.println("Actualizando nombre: " + request.getFullName());
            }
            if (request.getPhone() != null) {
                user.setPhone(request.getPhone());
                System.out.println("Actualizando teléfono: " + request.getPhone());
            }
            if (request.getAddress() != null) {
                user.setAddress(request.getAddress());
                System.out.println("Actualizando dirección: " + request.getAddress());
            }
            if (request.getProgram() != null) {
                user.setProgram(request.getProgram());
                System.out.println("Actualizando programa: " + request.getProgram());
            }
            
            User updatedUser = userService.updateUser(user);
            UserDto userDto = userService.convertToDto(updatedUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("user", userDto);
            response.put("message", "Perfil actualizado exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping
    @Operation(summary = "Obtener todos los usuarios (solo admin)")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        List<UserDto> userDtos = users.stream()
            .map(userService::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }
    
    // Clases internas para requests
    public static class UpdateProfileRequest {
        private String fullName;
        private String phone;
        private String address;
        private String program;
        
        // Getters y Setters
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getProgram() { return program; }
        public void setProgram(String program) { this.program = program; }
    }
}