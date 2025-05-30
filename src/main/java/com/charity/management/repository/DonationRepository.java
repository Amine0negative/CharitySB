package com.charity.management.repository;

import com.charity.management.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.charity.management.entity.PaymentStatus;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByUserId(Long userId);
    List<Donation> findByCharityActionId(Long charityActionId);
    List<Donation> findByPaymentStatus(PaymentStatus paymentStatus);
    
    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.charityAction.id = :charityActionId AND d.paymentStatus = com.charity.management.entity.PaymentStatus.COMPLETED")
    Double getTotalDonationAmountByCharityActionId(Long charityActionId);
}
