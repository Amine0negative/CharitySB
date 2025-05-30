package com.charity.management.service;

import com.charity.management.entity.Campaign;
import com.charity.management.entity.User;
import com.charity.management.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;

    @Autowired
    public CampaignService(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    public List<Campaign> getAllCampaigns() {
        return campaignRepository.findAll();
    }

    public List<Campaign> getActiveCampaigns() {
        return campaignRepository.findActiveCampaigns();
    }

    public Campaign getCampaignById(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
    }

    public List<Campaign> getCampaignsByOrganizer(Long organizerId) {
        return campaignRepository.findByOrganizerId(organizerId);
    }

    @Transactional
    public Campaign createCampaign(Campaign campaign, User organizer) {
        campaign.setOrganizer(organizer);
        campaign.setStatus("ACTIVE");
        campaign.setCurrentAmount(BigDecimal.ZERO);
        campaign.setStartDate(LocalDateTime.now());
        return campaignRepository.save(campaign);
    }

    @Transactional
    public Campaign updateCampaign(Long id, Campaign updatedCampaign) {
        Campaign existingCampaign = getCampaignById(id);
        
        existingCampaign.setTitle(updatedCampaign.getTitle());
        existingCampaign.setDescription(updatedCampaign.getDescription());
        existingCampaign.setImageUrl(updatedCampaign.getImageUrl());
        existingCampaign.setGoalAmount(updatedCampaign.getGoalAmount());
        existingCampaign.setEndDate(updatedCampaign.getEndDate());
        
        return campaignRepository.save(existingCampaign);
    }

    @Transactional
    public void updateCampaignAmount(Long campaignId, BigDecimal amount) {
        Campaign campaign = getCampaignById(campaignId);
        campaign.setCurrentAmount(campaign.getCurrentAmount().add(amount));
        
        if (campaign.getCurrentAmount().compareTo(campaign.getGoalAmount()) >= 0) {
            campaign.setStatus("COMPLETED");
        }
        
        campaignRepository.save(campaign);
    }

    @Transactional
    public void deleteCampaign(Long id) {
        Campaign campaign = getCampaignById(id);
        campaign.setStatus("CANCELLED");
        campaignRepository.save(campaign);
    }
}
