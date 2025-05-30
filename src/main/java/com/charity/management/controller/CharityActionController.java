package com.charity.management.controller;

import com.charity.management.entity.Category;
import com.charity.management.entity.CharityAction;
import com.charity.management.entity.Organization;
import com.charity.management.entity.User;
import com.charity.management.service.CategoryService;
import com.charity.management.service.CharityActionService;
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

@Controller
@RequestMapping("/charity-action")
public class CharityActionController {

    private final CharityActionService charityActionService;
    private final OrganizationService organizationService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final String UPLOAD_DIR = "./uploads/charity-actions/";

    @Autowired
    public CharityActionController(CharityActionService charityActionService,
                                  OrganizationService organizationService,
                                  CategoryService categoryService,
                                  UserService userService) {
        this.charityActionService = charityActionService;
        this.organizationService = organizationService;
        this.categoryService = categoryService;
        this.userService = userService;
        // Create upload directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/list")
    public String listCharityActions(Model model) {
        List<CharityAction> charityActions = charityActionService.findByStatus("ACTIVE");
        model.addAttribute("charityActions", charityActions);
        return "charity-action/list";
    }

    @GetMapping("/search")
    public String searchCharityActions(@RequestParam(value = "keyword", required = false) String keyword,
                                      @RequestParam(value = "category", required = false) Long categoryId,
                                      Model model) {
        List<CharityAction> charityActions;
        
        if (keyword != null && !keyword.isEmpty()) {
            charityActions = charityActionService.searchByKeyword(keyword);
        } else if (categoryId != null) {
            charityActions = charityActionService.findByCategoryId(categoryId);
        } else {
            charityActions = charityActionService.findByStatus("ACTIVE");
        }
        
        List<Category> categories = categoryService.findAll();
        
        model.addAttribute("charityActions", charityActions);
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", categoryId);
        
        return "charity-action/search";
    }

    @GetMapping("/{id}")
    public String viewCharityAction(@PathVariable("id") Long id, Model model) {
        CharityAction charityAction = charityActionService.findById(id);
        model.addAttribute("charityAction", charityAction);
        return "charity-action/view";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        List<Organization> organizations = organizationService.findByUserId(user.getId());
        List<Category> categories = categoryService.findAll();
        
        model.addAttribute("charityAction", new CharityAction());
        model.addAttribute("organizations", organizations);
        model.addAttribute("categories", categories);
        
        return "charity-action/create";
    }

    @PostMapping("/create")
    public String createCharityAction(@ModelAttribute("charityAction") @Valid CharityAction charityAction,
                                     BindingResult result,
                                     @RequestParam("organizationId") Long organizationId,
                                     @RequestParam("categoryId") Long categoryId,
                                     @RequestParam(value = "image", required = false) MultipartFile image,
                                     RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "charity-action/create";
        }
        
        try {
            // Handle image upload
            if (image != null && !image.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + fileName);
                Files.write(path, image.getBytes());
                charityAction.setImageUrl("/uploads/charity-actions/" + fileName);
            }
            
            CharityAction savedAction = charityActionService.save(charityAction, organizationId, categoryId);
            redirectAttributes.addFlashAttribute("successMessage", "Charity action created successfully");
            return "redirect:/charity-action/" + savedAction.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/charity-action/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String editCharityAction(@PathVariable("id") Long id, Model model) {
        CharityAction charityAction = charityActionService.findById(id);
        List<Category> categories = categoryService.findAll();
        
        model.addAttribute("charityAction", charityAction);
        model.addAttribute("categories", categories);
        
        return "charity-action/edit";
    }

    @PostMapping("/{id}/update")
    public String updateCharityAction(@PathVariable("id") Long id,
                                     @ModelAttribute("charityAction") @Valid CharityAction charityAction,
                                     BindingResult result,
                                     @RequestParam("categoryId") Long categoryId,
                                     @RequestParam(value = "image", required = false) MultipartFile image,
                                     RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "charity-action/edit";
        }
        
        try {
            CharityAction existingAction = charityActionService.findById(id);
            
            // Update fields
            existingAction.setTitle(charityAction.getTitle());
            existingAction.setDescription(charityAction.getDescription());
            existingAction.setShortDescription(charityAction.getShortDescription());
            existingAction.setTargetAmount(charityAction.getTargetAmount());
            existingAction.setStartDate(charityAction.getStartDate());
            existingAction.setEndDate(charityAction.getEndDate());
            existingAction.setLocation(charityAction.getLocation());
            existingAction.setStatus(charityAction.getStatus());
            
            // Update category
            Category category = categoryService.findById(categoryId);
            existingAction.setCategory(category);
            
            // Handle image upload
            if (image != null && !image.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + fileName);
                Files.write(path, image.getBytes());
                existingAction.setImageUrl("/uploads/charity-actions/" + fileName);
            }
            
            charityActionService.update(existingAction);
            redirectAttributes.addFlashAttribute("successMessage", "Charity action updated successfully");
            return "redirect:/charity-action/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/charity-action/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable("id") Long id,
                              @RequestParam("status") String status,
                              RedirectAttributes redirectAttributes) {
        try {
            charityActionService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Status updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/charity-action/" + id;
    }

    @GetMapping("/organization/{organizationId}")
    public String listOrganizationCharityActions(@PathVariable("organizationId") Long organizationId, Model model) {
        List<CharityAction> charityActions = charityActionService.findByOrganizationId(organizationId);
        Organization organization = organizationService.findById(organizationId);
        
        model.addAttribute("charityActions", charityActions);
        model.addAttribute("organization", organization);
        
        return "charity-action/organization-list";
    }
}
