package com.charity.management.repository;

import com.charity.management.entity.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import com.charity.management.entity.ParticipationStatus;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    List<Participation> findByUserId(Long userId);
    List<Participation> findByCharityActionId(Long charityActionId);
    List<Participation> findByStatus(ParticipationStatus status);
    Optional<Participation> findByUserIdAndCharityActionId(Long userId, Long charityActionId);
}
