package com.charity.management.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.charity.management.entity.User;
import com.charity.management.security.CustomOAuth2User;
import com.charity.management.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;

    public OAuth2LoginSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
            Authentication authentication) throws IOException, ServletException {
        
        CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();
        
        String email = oauthUser.getEmail();
        String name = oauthUser.getName();
        String provider = oauthUser.getClientName();
        String providerId = oauthUser.getId();
        
        User user = userService.processOAuthPostLogin(email, name, provider, providerId);
        
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
