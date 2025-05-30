package com.charity.management.controller;

import com.charity.management.entity.CharityAction;
import com.charity.management.entity.Donation;
import com.charity.management.entity.User;
import com.charity.management.exception.ResourceNotFoundException;
import com.charity.management.service.CharityActionService;
import com.charity.management.service.DonationService;
import com.charity.management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import com.charity.management.entity.PaymentStatus;

@Controller
@RequestMapping(DonationController.DONATIONS_BASE_PATH)
public class DonationController {
    public static final String DONATIONS_BASE_PATH = "/donations";

    private final DonationService donationService;
    private final CharityActionService charityActionService;
    private final UserService userService;

    @Autowired
    public DonationController(DonationService donationService,
                             CharityActionService charityActionService,
                             UserService userService) {
        this.donationService = donationService;
        this.charityActionService = charityActionService;
        this.userService = userService;
    }

    @GetMapping
    public String redirectToHistory() {
        return "redirect:/donations/history";
    }


    @GetMapping("/history")
    public String donationList(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return "redirect:/login";
            }
            
            String userEmail = auth.getName();
            User user = userService.findByEmail(userEmail);
            
            if (user == null) {
                model.addAttribute("errorMessage", "User account not found.");
                return "donations/history";
            }
            
            List<Donation> donations = new ArrayList<>();
            try {
                donations = donationService.findByUserId(user.getId());
            } catch (Exception e) {
                System.err.println("Error getting donations: " + e.getMessage());
            }
            
            model.addAttribute("donations", donations);
            model.addAttribute("user", user);
            
            return "donations/history";
            
        } catch (Exception e) {
            System.err.println("Error in donationList: " + e.getMessage());
            model.addAttribute("errorMessage", "An error occurred while retrieving your donations.");
            model.addAttribute("donations", new ArrayList<>());
            return "donations/history";
        }
    }

    @GetMapping(DONATIONS_BASE_PATH + "/history")
    public String donationHistory(Model model) {
        return donationList(model);
    }

    @GetMapping("/make/{charityActionId}")
    public String showDonationForm(@PathVariable("charityActionId") Long charityActionId, Model model) {
        CharityAction charityAction = charityActionService.findById(charityActionId);
        model.addAttribute("charityAction", charityAction);
        model.addAttribute("donation", new Donation());
        
        return "donations/make";
    }

    @PostMapping("/make/{charityActionId}")
    public String makeDonation(@PathVariable("charityActionId") Long charityActionId,
                              @ModelAttribute("donation") @Valid Donation donation,
                              BindingResult result,
                              @RequestParam(value = "anonymous", required = false) Boolean anonymous,
                              RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "donations/make";
        }
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByEmail(auth.getName());
            
            donation.setAnonymous(anonymous != null && anonymous);
            donation.setPaymentMethod("CREDIT_CARD"); // Default payment method
            donation.setPaymentStatus(PaymentStatus.PENDING);
            
            Donation savedDonation = donationService.save(donation, user.getId(), charityActionId);
            
            // Redirect to payment processing
            return "redirect:/donations/payment/" + savedDonation.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/donations/make/" + charityActionId;
        }
    }

    @GetMapping("/payment/{donationId}")
    public String showPaymentForm(@PathVariable("donationId") Long donationId, Model model) {
        Donation donation = donationService.findById(donationId);
        model.addAttribute("donation", donation);
        
        return "donations/payment";
    }

    @PostMapping("/payment/{donationId}/process")
    public String processPayment(@PathVariable("donationId") Long donationId,
                                @RequestParam("cardNumber") String cardNumber,
                                @RequestParam("cardName") String cardName,
                                @RequestParam("expiryDate") String expiryDate,
                                @RequestParam("cvv") String cvv,
                                RedirectAttributes redirectAttributes) {
        
        try {
            // In a real application, this would integrate with a payment gateway
            // For this demo, we'll simulate a successful payment
            String transactionId = "TX" + System.currentTimeMillis();
            Donation updatedDonation = donationService.updatePaymentStatus(donationId, PaymentStatus.COMPLETED, transactionId);
            
            redirectAttributes.addFlashAttribute("successMessage", "Payment processed successfully. Thank you for your donation!");
            return "redirect:/donations/confirmation/" + donationId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Payment processing failed: " + e.getMessage());
            return "redirect:/donations/payment/" + donationId;
        }
    }

    @GetMapping("/confirmation/{donationId}")
    public String showConfirmation(@PathVariable("donationId") Long donationId, Model model) {
        Donation donation = donationService.findById(donationId);
        model.addAttribute("donation", donation);
        
        return "donations/confirmation";
    }

    @GetMapping("/charity-action/{charityActionId}")
    public String listDonationsForCharityAction(@PathVariable("charityActionId") Long charityActionId, Model model) {
        List<Donation> donations = donationService.findByCharityActionId(charityActionId);
        CharityAction charityAction = charityActionService.findById(charityActionId);
        
        model.addAttribute("donations", donations);
        model.addAttribute("charityAction", charityAction);
        
        return "donations/charity-action-donations";
    }
}
