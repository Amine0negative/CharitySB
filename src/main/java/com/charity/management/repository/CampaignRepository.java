package com.charity.management.repository;

import com.charity.management.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByStatus(String status);
    
    @Query("SELECT c FROM Campaign c WHERE c.status = 'ACTIVE' ORDER BY c.endDate ASC")
    List<Campaign> findActiveCampaigns();
    
    List<Campaign> findByOrganizerId(Long organizerId);
}
