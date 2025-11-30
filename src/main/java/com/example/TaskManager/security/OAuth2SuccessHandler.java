package com.example.TaskManager.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse resp, Authentication auth) throws IOException {


        boolean isTempUser = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEMP"));

        if (isTempUser) {

            OAuth2User oauthUser = (OAuth2User) auth.getPrincipal();

            req.getSession().setAttribute("oauthAttributes", oauthUser.getAttributes());
            req.getSession().setAttribute("provider", oauthUser.getAttribute("provider"));

            resp.sendRedirect("/profile-completion");
            return;
        }

        resp.sendRedirect("/dashboard");
    }
}
