package com.charity.management.service;

import com.charity.management.entity.CharityAction;
import com.charity.management.entity.Donation;
import com.charity.management.entity.User;
import com.charity.management.exception.ResourceNotFoundException;
import com.charity.management.repository.CharityActionRepository;
import com.charity.management.repository.DonationRepository;
import com.charity.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import com.charity.management.entity.PaymentStatus;

@Service
public class DonationService {

    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final CharityActionRepository charityActionRepository;

    @Autowired
    public DonationService(DonationRepository donationRepository,
                          UserRepository userRepository,
                          CharityActionRepository charityActionRepository) {
        this.donationRepository = donationRepository;
        this.userRepository = userRepository;
        this.charityActionRepository = charityActionRepository;
    }

    public List<Donation> findAll() {
        return donationRepository.findAll();
    }

    public Donation findById(Long id) {
        return donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found with id: " + id));
    }

    public List<Donation> findByUserId(Long userId) {
        return donationRepository.findByUserId(userId);
    }

    public List<Donation> findByCharityActionId(Long charityActionId) {
        return donationRepository.findByCharityActionId(charityActionId);
    }

    public List<Donation> findByPaymentStatus(PaymentStatus paymentStatus) {
        return donationRepository.findByPaymentStatus(paymentStatus);
    }

    public Double getTotalDonationAmountByCharityActionId(Long charityActionId) {
        return donationRepository.getTotalDonationAmountByCharityActionId(charityActionId);
    }

    public List<Donation> getDonationsByUser(Long userId) {
        return donationRepository.findByUserId(userId);
    }

    public BigDecimal getTotalDonationsByUser(Long userId) {
        List<Donation> donations = getDonationsByUser(userId);
        return donations.stream()
                .filter(d -> PaymentStatus.COMPLETED.equals(d.getPaymentStatus()))
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public Donation save(Donation donation, Long userId, Long charityActionId) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        }
        
        CharityAction charityAction = charityActionRepository.findById(charityActionId)
                .orElseThrow(() -> new ResourceNotFoundException("Charity action not found with id: " + charityActionId));
        
        donation.setUser(user);
        donation.setCharityAction(charityAction);
        
        Donation savedDonation = donationRepository.save(donation);
        
        // Update collected amount in charity action if payment is completed
        if (PaymentStatus.COMPLETED.equals(donation.getPaymentStatus())) {
            updateCharityActionCollectedAmount(charityAction);
        }
        
        return savedDonation;
    }

    @Transactional
    public Donation updatePaymentStatus(Long id, PaymentStatus paymentStatus, String transactionId) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found with id: " + id));
        
        PaymentStatus oldStatus = donation.getPaymentStatus();
        donation.setPaymentStatus(paymentStatus);
        donation.setTransactionId(transactionId);
        
        Donation updatedDonation = donationRepository.save(donation);
        
        // Update collected amount in charity action if payment status changed to or from COMPLETED
        if ((PaymentStatus.COMPLETED.equals(paymentStatus) && !PaymentStatus.COMPLETED.equals(oldStatus)) ||
            (!PaymentStatus.COMPLETED.equals(paymentStatus) && PaymentStatus.COMPLETED.equals(oldStatus))) {
            updateCharityActionCollectedAmount(donation.getCharityAction());
        }
        
        return updatedDonation;
    }

    private void updateCharityActionCollectedAmount(CharityAction charityAction) {
        Double totalAmount = donationRepository.getTotalDonationAmountByCharityActionId(charityAction.getId());
        if (totalAmount == null) {
            totalAmount = 0.0;
        }
        
        charityAction.setCollectedAmount(BigDecimal.valueOf(totalAmount));
        charityActionRepository.save(charityAction);
    }
}
