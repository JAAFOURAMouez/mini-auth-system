package com.example.demo.service;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialisation des rôles
        userRole = new Role("ROLE_USER");
        userRole.setId(2L);
        
        adminRole = new Role("ROLE_ADMIN");
        adminRole.setId(1L);

        // Initialisation d'un utilisateur de test
        testUser = new User();
        testUser.setId(1L);
        testUser.setNom("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(userRole);

        // Configuration des mocks
        when(passwordEncoder.encode(any(String.class))).thenReturn("encoded_password");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    }

    @Test
    void testUpdateUserRole() {
        // Configuration pour le test
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Exécution du service à tester
        User updatedUser = userService.updateUserRole(1L, "ROLE_ADMIN");

        // Vérifications
        assertEquals(adminRole, updatedUser.getRole());
        verify(userRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).findByName("ROLE_ADMIN");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUserRoleWithNonExistingUser() {
        // Configuration pour simuler un utilisateur non trouvé
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Vérification que l'exception est bien lancée
        assertThrows(EntityNotFoundException.class, () -> {
            userService.updateUserRole(99L, "ROLE_ADMIN");
        });

        verify(userRepository, times(1)).findById(99L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUserRoleWithNonExistingRole() {
        // Configuration pour simuler un rôle non trouvé
        when(roleRepository.findByName("ROLE_NONEXISTENT")).thenReturn(Optional.empty());

        // Vérification que l'exception est bien lancée
        assertThrows(EntityNotFoundException.class, () -> {
            userService.updateUserRole(1L, "ROLE_NONEXISTENT");
        });

        verify(userRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).findByName("ROLE_NONEXISTENT");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCreateUser() {
        // Préparation des données
        User newUser = new User();
        newUser.setNom("New User");
        newUser.setEmail("new@example.com");
        newUser.setPassword("password");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(2L);
            return savedUser;
        });

        // Exécution du service
        User createdUser = userService.createUser(newUser, "ROLE_USER");

        // Vérifications
        assertNotNull(createdUser);
        assertEquals(2L, createdUser.getId());
        assertEquals("New User", createdUser.getNom());
        assertEquals("new@example.com", createdUser.getEmail());
        assertEquals("encoded_password", createdUser.getPassword());
        assertEquals(userRole, createdUser.getRole());

        verify(userRepository, times(1)).existsByEmail("new@example.com");
        verify(passwordEncoder, times(1)).encode("password");
        verify(roleRepository, times(1)).findByName("ROLE_USER");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUserWithExistingEmail() {
        // Préparation
        User newUser = new User();
        newUser.setNom("Duplicate User");
        newUser.setEmail("test@example.com");
        newUser.setPassword("password");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Vérification que l'exception est bien lancée
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser(newUser, "ROLE_USER");
        });

        assertEquals("Email déjà utilisé", exception.getMessage());
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }
}