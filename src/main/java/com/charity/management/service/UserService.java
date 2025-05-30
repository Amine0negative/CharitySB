package com.charity.management.service;

import com.charity.management.entity.User;
import com.charity.management.entity.Role;
import com.charity.management.entity.UserPreference;
import com.charity.management.repository.UserRepository;
import com.charity.management.repository.RoleRepository;
import com.charity.management.repository.UserPreferenceRepository;
import com.charity.management.dto.UserRegistrationDto;
import com.charity.management.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, 
                      RoleRepository roleRepository,
                      UserPreferenceRepository userPreferenceRepository,
                      BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userPreferenceRepository = userPreferenceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("\n=== Authentication Attempt ===\nAttempting to load user: " + email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        System.out.println("Found user: " + user.getEmail());
        System.out.println("Stored password hash: " + user.getPassword());
        System.out.println("User roles: " + user.getRoles());
        System.out.println("User enabled: " + user.isEnabled());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                getAuthorities(user.getRoles()));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Set<Role> roles) {
        System.out.println("Getting authorities for roles: " + roles);
        Collection<? extends GrantedAuthority> authorities = roles.stream()
                .map(role -> {
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role.getName());
                    System.out.println("Created authority: " + authority.getAuthority());
                    return authority;
                })
                .collect(Collectors.toList());
        System.out.println("Final authorities: " + authorities);
        return authorities;
    }

    @Transactional
    public User registerNewUser(UserRegistrationDto registrationDto) {
        System.out.println("Registering new user with email: " + registrationDto.getEmail());
        System.out.println("Raw password: " + registrationDto.getPassword());
        // Check if user already exists
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        // Create new user
        User user = new User();
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setEmail(registrationDto.getEmail());
        String rawPassword = registrationDto.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println("Encoded password during registration: " + encodedPassword);
        user.setPassword(encodedPassword);
        user.setPhone(registrationDto.getPhone());
        user.setAddress(registrationDto.getAddress());
        user.setEnabled(true);
        user.setProvider("LOCAL");

        // Assign default role
        System.out.println("Looking for ROLE_USER...");
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        System.out.println("Found role: " + userRole.getName());
        
        // Initialize roles set if null
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        user.getRoles().add(userRole);
        System.out.println("Added role to user. User roles: " + user.getRoles());

        // Save user
        User savedUser = userRepository.save(user);
        System.out.println("Saved user with ID: " + savedUser.getId());
        System.out.println("Saved user password hash: " + savedUser.getPassword());
        System.out.println("Saved user roles: " + savedUser.getRoles());

        // Create user preferences
        UserPreference preference = new UserPreference();
        preference.setUser(savedUser);
        preference.setLanguage("en");
        preference.setEmailNotifications(true);
        userPreferenceRepository.save(preference);

        return savedUser;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User updateUser(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setAddress(updatedUser.getAddress());
        existingUser.setBio(updatedUser.getBio());
        existingUser.setInterests(updatedUser.getInterests());
        existingUser.setNotificationsEnabled(updatedUser.isNotificationsEnabled());

        return userRepository.save(existingUser);
    }

    public Optional<User> findByProviderAndProviderId(String provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId);
    }

    @Transactional
    public User processOAuthPostLogin(String email, String name, String provider, String providerId) {
        System.out.println("\n=== Processing OAuth2 Login ===\n");
        System.out.println("Email: " + email);
        System.out.println("Name: " + name);
        System.out.println("Provider: " + provider);
        System.out.println("Provider ID: " + providerId);
        
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            System.out.println("Found existing user");
            User user = existingUser.get();
            // Always update provider info for OAuth users
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setEnabled(true); // Ensure user is enabled
            User savedUser = userRepository.save(user);
            System.out.println("Updated existing user: " + savedUser.getEmail());
            return savedUser;
        } else {
            System.out.println("Creating new user from OAuth2 login");
            // Create new user
            User user = new User();
            user.setEmail(email);
            String[] nameParts = name.split(" ", 2);
            user.setFirstName(nameParts[0]);
            user.setLastName(nameParts.length > 1 ? nameParts[1] : "");
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setEnabled(true);
            
            // Generate a random password for OAuth users
            String randomPassword = java.util.UUID.randomUUID().toString();
            user.setPassword(passwordEncoder.encode(randomPassword));
            System.out.println("Generated random password and encoded it");
            
            // Initialize roles set if null
            if (user.getRoles() == null) {
                user.setRoles(new HashSet<>());
            }
            
            // Assign default role
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Default role not found"));
            user.getRoles().add(userRole);
            System.out.println("Added ROLE_USER to new user");
            
            User savedUser = userRepository.save(user);
            System.out.println("Saved new user: " + savedUser.getEmail());
            
            // Create user preferences
            UserPreference preference = new UserPreference();
            preference.setUser(savedUser);
            preference.setLanguage("en");
            preference.setEmailNotifications(true);
            userPreferenceRepository.save(preference);
            System.out.println("Created user preferences");
            
            return savedUser;
        }
    }

    public User registerUser(User user, String rawPassword) {
        System.out.println("=== User Registration Details ===");
        System.out.println("Email: " + user.getEmail());
        System.out.println("Raw password: " + rawPassword);
        
        // Encode the password only once
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println("Encoded password: " + encodedPassword);
        
        user.setPassword(encodedPassword);
        // ... rest of your registration logic
        return userRepository.save(user);
    }
}
