package com.charity.management.controller;

import com.charity.management.entity.Campaign;
import com.charity.management.entity.User;
import com.charity.management.service.CampaignService;
import com.charity.management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/campaigns")
public class CampaignController {

    private final CampaignService campaignService;
    private final UserService userService;

    @Autowired
    public CampaignController(CampaignService campaignService, UserService userService) {
        this.campaignService = campaignService;
        this.userService = userService;
    }

    @GetMapping
    public String listCampaigns(Model model) {
        try {
            List<Campaign> campaigns = campaignService.getActiveCampaigns();
            model.addAttribute("activeCampaigns", campaigns != null ? campaigns : new ArrayList<>());
            return "campaigns/list";
        } catch (Exception e) {
            System.err.println("Error loading campaigns: " + e.getMessage());
            model.addAttribute("errorMessage", "Error loading campaigns. Please try again later.");
            model.addAttribute("activeCampaigns", new ArrayList<>());
            return "campaigns/list";
        }
    }

    @GetMapping("/{id}")
    public String viewCampaign(@PathVariable Long id, Model model) {
        try {
            Campaign campaign = campaignService.getCampaignById(id);
            if (campaign == null) {
                model.addAttribute("errorMessage", "Campaign not found");
                return "campaigns/list";
            }
            model.addAttribute("campaign", campaign);
            return "campaigns/view";
        } catch (Exception e) {
            System.err.println("Error viewing campaign: " + e.getMessage());
            model.addAttribute("errorMessage", "Error viewing campaign. Please try again later.");
            return "campaigns/list";
        }
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("campaign", new Campaign());
        return "campaigns/form";
    }

    @PostMapping("/create")
    public String createCampaign(@ModelAttribute Campaign campaign,
                              @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            campaign.setEndDate(endDate);
            User user = userService.findByEmail(userDetails.getUsername());
            campaignService.createCampaign(campaign, user);
            redirectAttributes.addFlashAttribute("successMessage", "Campaign created successfully!");
            return "redirect:/campaigns";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating campaign: " + e.getMessage());
            return "redirect:/campaigns/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            Campaign campaign = campaignService.getCampaignById(id);
            if (campaign == null) {
                model.addAttribute("errorMessage", "Campaign not found");
                return "error";
            }
            model.addAttribute("campaign", campaign);
            return "campaigns/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading campaign: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateCampaign(@PathVariable Long id, @ModelAttribute Campaign campaign, RedirectAttributes redirectAttributes) {
        try {
            campaignService.updateCampaign(id, campaign);
            redirectAttributes.addFlashAttribute("successMessage", "Campaign updated successfully");
            return "redirect:/campaigns/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating campaign: " + e.getMessage());
            return "redirect:/campaigns/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteCampaign(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return "redirect:/campaigns";
    }
}
