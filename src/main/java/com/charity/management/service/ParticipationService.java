package com.charity.management.service;

import com.charity.management.entity.CharityAction;
import com.charity.management.entity.Participation;
import com.charity.management.entity.User;
import com.charity.management.exception.ResourceNotFoundException;
import com.charity.management.repository.CharityActionRepository;
import com.charity.management.repository.ParticipationRepository;
import com.charity.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import com.charity.management.entity.ParticipationStatus;

@Service
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final CharityActionRepository charityActionRepository;

    @Autowired
    public ParticipationService(ParticipationRepository participationRepository,
                               UserRepository userRepository,
                               CharityActionRepository charityActionRepository) {
        this.participationRepository = participationRepository;
        this.userRepository = userRepository;
        this.charityActionRepository = charityActionRepository;
    }

    public List<Participation> findAll() {
        return participationRepository.findAll();
    }

    public Participation findById(Long id) {
        return participationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Participation not found with id: " + id));
    }

    public List<Participation> findByUserId(Long userId) {
        return participationRepository.findByUserId(userId);
    }

    public List<Participation> findByCharityActionId(Long charityActionId) {
        return participationRepository.findByCharityActionId(charityActionId);
    }

    public List<Participation> findByStatus(ParticipationStatus status) {
        return participationRepository.findByStatus(status);
    }

    public Optional<Participation> findByUserIdAndCharityActionId(Long userId, Long charityActionId) {
        return participationRepository.findByUserIdAndCharityActionId(userId, charityActionId);
    }

    @Transactional
    public Participation save(Participation participation, Long userId, Long charityActionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        CharityAction charityAction = charityActionRepository.findById(charityActionId)
                .orElseThrow(() -> new ResourceNotFoundException("Charity action not found with id: " + charityActionId));
        
        // Check if user already has a participation for this charity action
        Optional<Participation> existingParticipation = participationRepository.findByUserIdAndCharityActionId(userId, charityActionId);
        if (existingParticipation.isPresent()) {
            throw new RuntimeException("User already has a participation for this charity action");
        }
        
        participation.setUser(user);
        participation.setCharityAction(charityAction);
        
        return participationRepository.save(participation);
    }

    @Transactional
    public Participation updateStatus(Long id, ParticipationStatus status) {
        Participation participation = participationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Participation not found with id: " + id));
        
        participation.setStatus(status);
        return participationRepository.save(participation);
    }

    @Transactional
    public void delete(Long id) {
        Participation participation = participationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Participation not found with id: " + id));
        
        participationRepository.delete(participation);
    }
}
