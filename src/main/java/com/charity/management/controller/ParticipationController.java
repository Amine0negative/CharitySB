package com.charity.management.controller;

import com.charity.management.entity.CharityAction;
import com.charity.management.entity.Participation;
import com.charity.management.entity.User;
import com.charity.management.service.CharityActionService;
import com.charity.management.service.ParticipationService;
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

import java.util.List;
import java.util.Optional;
import com.charity.management.entity.ParticipationStatus;

@Controller
@RequestMapping("/participation")
public class ParticipationController {

    private final ParticipationService participationService;
    private final CharityActionService charityActionService;
    private final UserService userService;

    @Autowired
    public ParticipationController(ParticipationService participationService,
                                  CharityActionService charityActionService,
                                  UserService userService) {
        this.participationService = participationService;
        this.charityActionService = charityActionService;
        this.userService = userService;
    }

    @GetMapping("/my-participations")
    public String myParticipations(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        
        List<Participation> participations = participationService.findByUserId(user.getId());
        model.addAttribute("participations", participations);
        
        return "participation/my-participations";
    }

    @GetMapping("/join/{charityActionId}")
    public String showJoinForm(@PathVariable("charityActionId") Long charityActionId, Model model) {
        CharityAction charityAction = charityActionService.findById(charityActionId);
        model.addAttribute("charityAction", charityAction);
        model.addAttribute("participation", new Participation());
        
        // Check if user already has a participation for this charity action
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        Optional<Participation> existingParticipation = participationService.findByUserIdAndCharityActionId(user.getId(), charityActionId);
        
        if (existingParticipation.isPresent()) {
            model.addAttribute("existingParticipation", existingParticipation.get());
        }
        
        return "participation/join";
    }

    @PostMapping("/join/{charityActionId}")
    public String joinCharityAction(@PathVariable("charityActionId") Long charityActionId,
                                   @ModelAttribute("participation") @Valid Participation participation,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "participation/join";
        }
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByEmail(auth.getName());
            
            // Check if user already has a participation for this charity action
            Optional<Participation> existingParticipation = participationService.findByUserIdAndCharityActionId(user.getId(), charityActionId);
            if (existingParticipation.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "You have already joined this charity action");
                return "redirect:/participation/join/" + charityActionId;
            }
            
            participation.setStatus(ParticipationStatus.PENDING);
            Participation savedParticipation = participationService.save(participation, user.getId(), charityActionId);
            
            redirectAttributes.addFlashAttribute("successMessage", "Your participation request has been submitted and is pending approval");
            return "redirect:/charity-action/" + charityActionId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/participation/join/" + charityActionId;
        }
    }

    @GetMapping("/charity-action/{charityActionId}")
    public String listParticipationsForCharityAction(@PathVariable("charityActionId") Long charityActionId, Model model) {
        List<Participation> participations = participationService.findByCharityActionId(charityActionId);
        CharityAction charityAction = charityActionService.findById(charityActionId);
        
        model.addAttribute("participations", participations);
        model.addAttribute("charityAction", charityAction);
        
        return "participation/charity-action-participations";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable("id") Long id,
                              @RequestParam("status") ParticipationStatus status,
                              @RequestParam("charityActionId") Long charityActionId,
                              RedirectAttributes redirectAttributes) {
        try {
            participationService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Participation status updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/participation/charity-action/" + charityActionId;
    }

    @PostMapping("/{id}/cancel")
    public String cancelParticipation(@PathVariable("id") Long id,
                                     RedirectAttributes redirectAttributes) {
        try {
            Participation participation = participationService.findById(id);
            Long charityActionId = participation.getCharityAction().getId();
            
            participationService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Participation cancelled successfully");
            return "redirect:/charity-action/" + charityActionId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/participation/my-participations";
        }
    }
}
