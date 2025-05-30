package com.charity.management.repository;

import com.charity.management.entity.CharityAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CharityActionRepository extends JpaRepository<CharityAction, Long> {
    List<CharityAction> findByOrganizationId(Long organizationId);
    List<CharityAction> findByCategoryId(Long categoryId);
    List<CharityAction> findByStatus(String status);
    
    @Query("SELECT ca FROM CharityAction ca WHERE ca.title LIKE %:keyword% OR ca.description LIKE %:keyword% OR ca.shortDescription LIKE %:keyword%")
    List<CharityAction> searchByKeyword(String keyword);
    
    @Query("SELECT ca FROM CharityAction ca JOIN ca.participations p WHERE p.user.id = :userId")
    List<CharityAction> findByParticipantId(Long userId);
}
