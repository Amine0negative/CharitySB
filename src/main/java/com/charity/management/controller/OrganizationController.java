package com.charity.management.controller;

import com.charity.management.entity.Organization;
import com.charity.management.entity.User;
import com.charity.management.service.OrganizationService;
import com.charity.management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import com.charity.management.entity.OrganizationStatus;

@Controller
@RequestMapping("/organization")
public class OrganizationController {

    private final OrganizationService organizationService;
    private final UserService userService;
    private final String UPLOAD_DIR = "./uploads/organizations/";

    @Autowired
    public OrganizationController(OrganizationService organizationService, UserService userService) {
        this.organizationService = organizationService;
        this.userService = userService;
        // Create upload directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("organization", new Organization());
        return "organization/register";
    }

    @PostMapping("/register")
    public String registerOrganization(@ModelAttribute("organization") @Valid Organization organization,
                                      BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "organization/register";
        }

        try {
            // Set initial status
            organization.setStatus(OrganizationStatus.PENDING);
            
            // Save organization
            Organization savedOrg = organizationService.save(organization);
            
            // Add current user as admin
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByEmail(auth.getName());
            organizationService.addUserToOrganization(savedOrg.getId(), user.getId(), "ADMIN");
            
            redirectAttributes.addFlashAttribute("successMessage", "Organization registration submitted successfully. It is pending approval.");
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/organization/register";
        }
    }

    @GetMapping("/list")
    public String listOrganizations(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        List<Organization> organizations = organizationService.findByUserId(user.getId());
        model.addAttribute("organizations", organizations);
        
        return "organization/list";
    }

    @GetMapping("/{id}")
    public String viewOrganization(@PathVariable("id") Long id, Model model) {
        Organization organization = organizationService.findById(id);
        model.addAttribute("organization", organization);
        return "organization/view";
    }

    @GetMapping("/{id}/edit")
    public String editOrganization(@PathVariable("id") Long id, Model model) {
        Organization organization = organizationService.findById(id);
        model.addAttribute("organization", organization);
        return "organization/edit";
    }

    @PostMapping("/{id}/update")
    public String updateOrganization(@PathVariable("id") Long id,
                                    @ModelAttribute("organization") @Valid Organization organization,
                                    BindingResult result,
                                    @RequestParam(value = "logo", required = false) MultipartFile logo,
                                    @RequestParam(value = "coverImage", required = false) MultipartFile coverImage,
                                    RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "organization/edit";
        }
        
        try {
            Organization existingOrg = organizationService.findById(id);
            
            // Update fields
            existingOrg.setName(organization.getName());
            existingOrg.setDescription(organization.getDescription());
            existingOrg.setEmail(organization.getEmail());
            existingOrg.setPhone(organization.getPhone());
            existingOrg.setWebsite(organization.getWebsite());
            existingOrg.setLocation(organization.getLocation());
            existingOrg.setFacebookUrl(organization.getFacebookUrl());
            existingOrg.setTwitterUrl(organization.getTwitterUrl());
            existingOrg.setInstagramUrl(organization.getInstagramUrl());
            existingOrg.setLinkedinUrl(organization.getLinkedinUrl());
            
            // Handle logo upload
            if (logo != null && !logo.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + logo.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + fileName);
                Files.write(path, logo.getBytes());
                existingOrg.setLogoUrl("/uploads/organizations/" + fileName);
            }
            
            // Handle cover image upload
            if (coverImage != null && !coverImage.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + coverImage.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + fileName);
                Files.write(path, coverImage.getBytes());
                existingOrg.setCoverImageUrl("/uploads/organizations/" + fileName);
            }
            
            organizationService.update(existingOrg);
            redirectAttributes.addFlashAttribute("successMessage", "Organization updated successfully");
            return "redirect:/organization/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/organization/" + id + "/edit";
        }
    }

    @GetMapping("/{id}/members")
    public String listMembers(@PathVariable("id") Long id, Model model) {
        Organization organization = organizationService.findById(id);
        model.addAttribute("organization", organization);
        model.addAttribute("members", organization.getUsers());
        return "organization/members";
    }

    @PostMapping("/{id}/members/add")
    public String addMember(@PathVariable("id") Long id,
                           @RequestParam("email") String email,
                           @RequestParam("role") String role,
                           RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(email);
            organizationService.addUserToOrganization(id, user.getId(), role);
            redirectAttributes.addFlashAttribute("successMessage", "Member added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/organization/" + id + "/members";
    }

    @PostMapping("/{id}/members/{userId}/remove")
    public String removeMember(@PathVariable("id") Long id,
                              @PathVariable("userId") Long userId,
                              RedirectAttributes redirectAttributes) {
        try {
            organizationService.removeUserFromOrganization(id, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Member removed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/organization/" + id + "/members";
    }
}
