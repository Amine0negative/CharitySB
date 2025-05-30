package com.charity.management.service;

import com.charity.management.entity.Category;
import com.charity.management.entity.CharityAction;
import com.charity.management.entity.Organization;
import com.charity.management.exception.ResourceNotFoundException;
import com.charity.management.repository.CategoryRepository;
import com.charity.management.repository.CharityActionRepository;
import com.charity.management.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CharityActionService {

    private final CharityActionRepository charityActionRepository;
    private final OrganizationRepository organizationRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public CharityActionService(CharityActionRepository charityActionRepository,
                               OrganizationRepository organizationRepository,
                               CategoryRepository categoryRepository) {
        this.charityActionRepository = charityActionRepository;
        this.organizationRepository = organizationRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<CharityAction> findAll() {
        return charityActionRepository.findAll();
    }

    public CharityAction findById(Long id) {
        return charityActionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Charity action not found with id: " + id));
    }

    public List<CharityAction> findByOrganizationId(Long organizationId) {
        return charityActionRepository.findByOrganizationId(organizationId);
    }

    public List<CharityAction> findByCategoryId(Long categoryId) {
        return charityActionRepository.findByCategoryId(categoryId);
    }

    public List<CharityAction> findByStatus(String status) {
        return charityActionRepository.findByStatus(status);
    }

    public List<CharityAction> searchByKeyword(String keyword) {
        return charityActionRepository.searchByKeyword(keyword);
    }

    public List<CharityAction> findByParticipantId(Long userId) {
        return charityActionRepository.findByParticipantId(userId);
    }

    @Transactional
    public CharityAction save(CharityAction charityAction, Long organizationId, Long categoryId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + organizationId));
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        
        charityAction.setOrganization(organization);
        charityAction.setCategory(category);
        
        return charityActionRepository.save(charityAction);
    }

    @Transactional
    public CharityAction update(CharityAction charityAction) {
        // Ensure the charity action exists
        charityActionRepository.findById(charityAction.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Charity action not found with id: " + charityAction.getId()));
        
        return charityActionRepository.save(charityAction);
    }

    @Transactional
    public void delete(Long id) {
        CharityAction charityAction = charityActionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Charity action not found with id: " + id));
        
        charityActionRepository.delete(charityAction);
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        CharityAction charityAction = charityActionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Charity action not found with id: " + id));
        
        charityAction.setStatus(status);
        charityActionRepository.save(charityAction);
    }
}
