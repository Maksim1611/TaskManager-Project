package com.example.TaskManager.web;

import com.example.TaskManager.security.UserData;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Base64;

@ControllerAdvice
public class GlobalUserController {

    private final UserService userService;

    public GlobalUserController(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("base64Image")
    public String addBase64Image (@AuthenticationPrincipal UserData userData) {
        if (userData == null) {
            return null;
        }
        User user = userService.getById(userData.getId());

        if (user.getImage() != null && user.getImage().length > 0) {
            return Base64.getEncoder().encodeToString(user.getImage());
        }

        return null;
    }
}
