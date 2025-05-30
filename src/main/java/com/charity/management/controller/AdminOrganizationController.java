package com.charity.management.controller;

import com.charity.management.entity.Organization;
import com.charity.management.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import com.charity.management.entity.OrganizationStatus;

@Controller
@RequestMapping("/admin/organizations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrganizationController {

    private final OrganizationService organizationService;

    @Autowired
    public AdminOrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    public String listOrganizations(Model model) {
        List<Organization> organizations = organizationService.findAll();
        model.addAttribute("organizations", organizations);
        return "admin/organizations/list";
    }

    @GetMapping("/pending")
    public String listPendingOrganizations(Model model) {
        List<Organization> pendingOrganizations = organizationService.findByStatus(OrganizationStatus.PENDING);
        model.addAttribute("organizations", pendingOrganizations);
        return "admin/organizations/pending";
    }

    @GetMapping("/{id}")
    public String viewOrganization(@PathVariable("id") Long id, Model model) {
        Organization organization = organizationService.findById(id);
        model.addAttribute("organization", organization);
        return "admin/organizations/view";
    }

    @PostMapping("/{id}/approve")
    public String approveOrganization(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            organizationService.approveOrganization(id);
            redirectAttributes.addFlashAttribute("successMessage", "Organization approved successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/organizations/pending";
    }

    @PostMapping("/{id}/reject")
    public String rejectOrganization(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            organizationService.rejectOrganization(id);
            redirectAttributes.addFlashAttribute("successMessage", "Organization rejected successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/organizations/pending";
    }
}
