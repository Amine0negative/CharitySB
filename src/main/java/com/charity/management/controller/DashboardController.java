package com.charity.management.controller;

import com.charity.management.entity.User;
import com.charity.management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final UserService userService;

    @Autowired
    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        
        // Add user details
        model.addAttribute("user", user);
        model.addAttribute("totalDonations", 0);  // TODO: Implement when donation system is ready
        model.addAttribute("totalParticipations", 0);  // TODO: Implement when participation system is ready
        model.addAttribute("totalOrganizations", 0);  // TODO: Implement when organization system is ready
        model.addAttribute("recentCharityActions", null);  // TODO: Implement when charity actions are ready
        
        return "dashboard";
    }
}
