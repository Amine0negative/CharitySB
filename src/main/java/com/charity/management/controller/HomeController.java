package com.charity.management.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        return "home";
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    
    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
