package com.example.TaskManager.web;

import com.example.TaskManager.analytics.client.AnalyticsClient;
import com.example.TaskManager.analytics.client.dto.ProjectAnalyticsResponse;
import com.example.TaskManager.analytics.client.dto.TaskAnalyticsResponse;
import com.example.TaskManager.security.UserData;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsClient analyticsClient;
    private final UserService userService;

    public AnalyticsController(AnalyticsClient analyticsClient, UserService userService) {
        this.analyticsClient = analyticsClient;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getAnalytics(@AuthenticationPrincipal UserData userData)  {
        ModelAndView modelAndView = new ModelAndView("analytics");
        User user = userService.getById(userData.getId());

        TaskAnalyticsResponse tasksAnalytics = analyticsClient.getTaskAnalytics(user.getId());
        ProjectAnalyticsResponse projectAnalytics = analyticsClient.getProjectAnalytics(user.getId());

        modelAndView.addObject("user", user);
        modelAndView.addObject("tasksAnalytics", tasksAnalytics);
        modelAndView.addObject("projectAnalytics", projectAnalytics);

        return modelAndView;
    }

    @GetMapping("/lifetime")
    public ModelAndView getLifetimeAnalytics(@AuthenticationPrincipal UserData userData) {
        ModelAndView modelAndView = new ModelAndView("analytics-lifetime");
        User user = userService.getById(userData.getId());

        TaskAnalyticsResponse tasksAnalytics = analyticsClient.getTaskAnalytics(user.getId());
        ProjectAnalyticsResponse projectAnalytics = analyticsClient.getProjectAnalytics(user.getId());

        modelAndView.addObject("user", user);
        modelAndView.addObject("tasksAnalytics", tasksAnalytics);
        modelAndView.addObject("projectAnalytics", projectAnalytics);

        return modelAndView;
    }
}
