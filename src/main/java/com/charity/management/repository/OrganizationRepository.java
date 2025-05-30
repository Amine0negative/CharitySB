package com.charity.management.repository;

import com.charity.management.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import com.charity.management.entity.OrganizationStatus;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByName(String name);
    List<Organization> findByStatus(OrganizationStatus status);
    
    @Query("SELECT o FROM Organization o JOIN o.users u WHERE u.id = :userId")
    List<Organization> findByUserId(Long userId);
}
