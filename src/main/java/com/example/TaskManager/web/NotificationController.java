package com.example.TaskManager.web;

import com.example.TaskManager.notification.service.NotificationService;
import com.example.TaskManager.security.UserData;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.service.UserService;
import com.example.TaskManager.web.dto.DtoMapper;
import com.example.TaskManager.web.dto.EditPreferenceRequest;
import com.example.TaskManager.web.dto.GlobalNotificationRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @Autowired
    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getNotificationPage(@AuthenticationPrincipal UserData userData) {
        ModelAndView modelAndView = new ModelAndView("notifications");
        User user = userService.getById(userData.getId());
        modelAndView.addObject("user", user);
        EditPreferenceRequest editPreferenceRequest = DtoMapper.fromUserPreference(user);

        modelAndView.addObject("preference", notificationService.getPreferenceByUserId(userData.getId()));
        modelAndView.addObject("notifications", notificationService.getUserNotifications(userData.getId()));
        modelAndView.addObject("editPreferenceRequest", editPreferenceRequest);

        return modelAndView;
    }

    @PatchMapping("/{id}/preferences")
    public ModelAndView updatePreference(@PathVariable UUID id, @ModelAttribute @Valid EditPreferenceRequest editPreferenceRequest, BindingResult result) {
        if (result.hasErrors()) {
            return new ModelAndView("redirect:/notifications");
        }
        User user = userService.getById(id);

        notificationService.updatePreferences(user, editPreferenceRequest);
        userService.editPreferences(editPreferenceRequest, user.getId());

        return new ModelAndView("redirect:/notifications");
    }

    @DeleteMapping("/history")
    public String clearHistory(@AuthenticationPrincipal UserData userData) {
        notificationService.clearHistory(userData.getId());
        return "redirect:/notifications";
    }

    @PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
    @GetMapping("/sender")
    public ModelAndView getNotificationSenderPage(@AuthenticationPrincipal UserData userData) {
        ModelAndView modelAndView = new ModelAndView("notification-sender");
        User user = userService.getById(userData.getId());

        modelAndView.addObject("user", user);
        modelAndView.addObject("notificationRequest", new GlobalNotificationRequest());
        return modelAndView;
    }

    @PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
    @PostMapping("/sender")
    public ModelAndView sendGlobalNotification(@Valid @ModelAttribute GlobalNotificationRequest globalNotificationRequest,
                                               BindingResult result, @AuthenticationPrincipal UserData userData) {
        if (result.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("notification-sender");
            User user = userService.getById(userData.getId());
            modelAndView.addObject("user", user);
            modelAndView.addObject("notificationRequest", new GlobalNotificationRequest());
            return modelAndView;
        }

        notificationService.sendGlobalNotification(globalNotificationRequest, userService.getAllUsers());

        return new ModelAndView("redirect:/notifications");
    }
}
