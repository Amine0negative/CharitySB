package com.charity.management.service;

import com.charity.management.entity.Organization;
import com.charity.management.entity.Role;
import com.charity.management.entity.User;
import com.charity.management.exception.ResourceNotFoundException;
import com.charity.management.repository.OrganizationRepository;
import com.charity.management.repository.RoleRepository;
import com.charity.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import com.charity.management.entity.OrganizationStatus;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository,
                              UserRepository userRepository,
                              RoleRepository roleRepository) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<Organization> findAll() {
        return organizationRepository.findAll();
    }

    public Organization findById(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));
    }

    public List<Organization> findByStatus(OrganizationStatus status) {
        return organizationRepository.findByStatus(status);
    }

    public List<Organization> findByUserId(Long userId) {
        return organizationRepository.findByUserId(userId);
    }

    @Transactional
    public Organization save(Organization organization) {
        return organizationRepository.save(organization);
    }

    @Transactional
    public Organization update(Organization organization) {
        // Ensure the organization exists
        organizationRepository.findById(organization.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + organization.getId()));
        
        return organizationRepository.save(organization);
    }

    @Transactional
    public void delete(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));
        
        organizationRepository.delete(organization);
    }

    @Transactional
    public void approveOrganization(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));
        
        organization.setStatus(OrganizationStatus.APPROVED);
        organizationRepository.save(organization);
    }

    @Transactional
    public void rejectOrganization(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));
        
        organization.setStatus(OrganizationStatus.REJECTED);
        organizationRepository.save(organization);
    }

    @Transactional
    public void addUserToOrganization(Long organizationId, Long userId, String role) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + organizationId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Add organization role to user if not already present
        Role orgRole = roleRepository.findByName("ROLE_ORGANIZATION")
                .orElseThrow(() -> new RuntimeException("Organization role not found"));
        
        Set<Role> userRoles = user.getRoles();
        if (!userRoles.contains(orgRole)) {
            userRoles.add(orgRole);
            userRepository.save(user);
        }
        
        // Add user to organization
        organization.getUsers().add(user);
        organizationRepository.save(organization);
    }

    @Transactional
    public void removeUserFromOrganization(Long organizationId, Long userId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + organizationId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Remove user from organization
        organization.getUsers().remove(user);
        organizationRepository.save(organization);
        
        // Check if user is still part of any organization
        List<Organization> userOrganizations = organizationRepository.findByUserId(userId);
        if (userOrganizations.isEmpty()) {
            // Remove organization role from user if not part of any organization
            Role orgRole = roleRepository.findByName("ROLE_ORGANIZATION")
                    .orElseThrow(() -> new RuntimeException("Organization role not found"));
            
            user.getRoles().remove(orgRole);
            userRepository.save(user);
        }
    }
}
