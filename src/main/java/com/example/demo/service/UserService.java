package com.example.demo.service;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(User user, String roleName) {
        // Vérifier si l'utilisateur existe déjà
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }


        // Encoder le mot de passe
        // Utilisez le mot de passe brut ou le mot de passe normal selon ce qui est disponible
        String passwordToEncode = user.getPassword();
        if (passwordToEncode != null) {
            user.setPassword(passwordEncoder.encode(passwordToEncode));
        }
        
        // Assigner le rôle par défaut si non spécifié
        final String finalRoleName;
        if (roleName == null || roleName.isEmpty()) {
            finalRoleName = "ROLE_USER";
        } else {
            finalRoleName = roleName;
        }

        // Récupérer le rôle
        Role role = roleRepository.findByName(finalRoleName)
                .orElseThrow(() -> new EntityNotFoundException("Rôle non trouvé: " + finalRoleName));

        user.setRole(role);

        // Sauvegarder l'utilisateur
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec ID: " + id));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec email: " + email));
    }

    @Transactional
    public User updateUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec ID: " + userId));

        final String finalRoleName = roleName; // Rendre finalRoleName final pour l'utiliser dans le lambda
        
        Role role = roleRepository.findByName(finalRoleName)
                .orElseThrow(() -> new EntityNotFoundException("Rôle non trouvé: " + finalRoleName));

        user.setRole(role);
        return userRepository.save(user);
    }

    @Transactional
    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }
}