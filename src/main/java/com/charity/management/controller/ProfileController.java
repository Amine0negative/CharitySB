package com.charity.management.controller;

import com.charity.management.entity.User;
import com.charity.management.entity.Donation;
import com.charity.management.entity.Campaign;
import com.charity.management.exception.ResourceNotFoundException;
import com.charity.management.service.CampaignService;
import com.charity.management.service.DonationService;
import com.charity.management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final CampaignService campaignService;
    private final DonationService donationService;

    @Autowired
    public ProfileController(UserService userService, CampaignService campaignService, DonationService donationService) {
        this.userService = userService;
        this.campaignService = campaignService;
        this.donationService = donationService;
    }

    @GetMapping("/")
    public String viewProfile(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return "redirect:/login";
            }

            User user = userService.findByEmail(auth.getName());
            if (user == null) {
                model.addAttribute("errorMessage", "User profile not found.");
                return "profile/view";
            }

            List<Campaign> myCampaigns = new ArrayList<>();
            List<Donation> myDonations = new ArrayList<>();
            Double totalDonations = 0.0;

            try {
                myCampaigns = campaignService.getCampaignsByOrganizer(user.getId());
            } catch (Exception e) {
                System.err.println("Error loading campaigns: " + e.getMessage());
                model.addAttribute("campaignError", "Unable to load campaigns.");
            }

            try {
                myDonations = donationService.getDonationsByUser(user.getId());
                totalDonations = donationService.getTotalDonationsByUser(user.getId()).doubleValue();
            } catch (Exception e) {
                System.err.println("Error loading donations: " + e.getMessage());
                model.addAttribute("donationError", "Unable to load donations.");
            }

            model.addAttribute("user", user);
            model.addAttribute("myCampaigns", myCampaigns);
            model.addAttribute("myDonations", myDonations);
            model.addAttribute("totalDonations", totalDonations);
            
            return "profile/view";
            
        } catch (Exception e) {
            System.err.println("Error viewing profile: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "An error occurred while loading your profile.");
            return "profile/view";
        }
    }

    @GetMapping("/edit")
    public String showEditForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            if (user == null) {
                model.addAttribute("errorMessage", "User not found");
                return "error";
            }
            model.addAttribute("user", user);
            return "profile/edit";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading profile: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/edit")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails, @ModelAttribute User updatedUser, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            if (user == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found");
                return "redirect:/profile";
            }
            userService.updateUser(user.getId(), updatedUser);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile: " + e.getMessage());
            return "redirect:/profile/edit";
        }
    }
}
