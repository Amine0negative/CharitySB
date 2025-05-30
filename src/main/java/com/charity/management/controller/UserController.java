package com.charity.management.controller;

import com.charity.management.entity.User;
import com.charity.management.entity.UserPreference;
import com.charity.management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
public class UserController {

    private final UserService userService;
    private final String UPLOAD_DIR = "./uploads/profiles/";

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
        // Create upload directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("firstName") String firstName,
                               @RequestParam("lastName") String lastName,
                               @RequestParam("phone") String phone,
                               @RequestParam("address") String address,
                               @RequestParam("bio") String bio,
                               @RequestParam("interests") String interests,
                               @RequestParam(value = "notificationsEnabled", defaultValue = "false") boolean notificationsEnabled,
                               @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                               RedirectAttributes redirectAttributes) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        User updatedUser = new User();
        updatedUser.setFirstName(firstName);
        updatedUser.setLastName(lastName);
        updatedUser.setPhone(phone);
        updatedUser.setAddress(address);
        updatedUser.setBio(bio);
        updatedUser.setInterests(interests);
        updatedUser.setNotificationsEnabled(notificationsEnabled);
        
        // Handle profile image upload
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + profileImage.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + fileName);
                Files.write(path, profileImage.getBytes());
                updatedUser.setProfileImageUrl("/uploads/profiles/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload profile image");
                return "redirect:/profile";
            }
        }
        
        userService.updateUser(user.getId(), updatedUser);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
        return "redirect:/profile";
    }

    @GetMapping("/preferences")
    public String showPreferences(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        model.addAttribute("user", user);
        return "preferences";
    }

    @PostMapping("/preferences/update")
    public String updatePreferences(@RequestParam("language") String language,
                                   @RequestParam(value = "emailNotifications", required = false) Boolean emailNotifications,
                                   @RequestParam("interests") String interests,
                                   RedirectAttributes redirectAttributes) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        UserPreference preference = user.getPreference();
        if (preference == null) {
            preference = new UserPreference();
            preference.setUser(user);
        }
        
        preference.setLanguage(language);
        preference.setEmailNotifications(emailNotifications != null && emailNotifications);
        preference.setInterests(interests);
        
        user.setPreference(preference);
        userService.updateUser(user.getId(), user);
        
        redirectAttributes.addFlashAttribute("successMessage", "Preferences updated successfully");
        return "redirect:/preferences";
    }
}
