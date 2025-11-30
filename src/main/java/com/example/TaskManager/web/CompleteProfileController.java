package com.example.TaskManager.web;

import com.example.TaskManager.security.UserData;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.service.UserService;
import com.example.TaskManager.web.dto.UserCreateDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
@RequestMapping("/profile-completion")
public class CompleteProfileController {

    private final UserService userService;

    public CompleteProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getPage() {
        ModelAndView mv = new ModelAndView("complete-profile");
        mv.addObject("userCreateDto", new UserCreateDto());

        return mv;
    }

    @PostMapping
    public String completeProfile(@ModelAttribute UserCreateDto dto, HttpSession session) {

        Map<String, Object> attributes = (Map<String, Object>) session.getAttribute("oauthAttributes");

        String email = attributes.get("email").toString();

        String provider = (String) session.getAttribute("provider");

        User saved = userService.registerViaOauth2(dto, email, provider);

        UserData principal =  new UserData(
                saved.getId(),
                saved.getEmail(),
                saved.getPassword(),
                saved.isActive(),
                saved.getRole(),
                attributes
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(auth);

        return "redirect:/dashboard";
    }

}
