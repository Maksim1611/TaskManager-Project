package com.example.TaskManager.web;

import com.example.TaskManager.activity.model.Activity;
import com.example.TaskManager.activity.service.ActivityService;
import com.example.TaskManager.security.UserData;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/activity")
public class ActivityController {

    private final UserService userService;
    private final ActivityService activityService;

    public ActivityController(UserService userService, ActivityService activityService) {
        this.userService = userService;
        this.activityService = activityService;
    }

    @GetMapping
    public ModelAndView activity(@AuthenticationPrincipal UserData userData) {
        ModelAndView mv = new ModelAndView("activity");
        User user = userService.getById(userData.getId());
        List<Activity> activities = activityService.getActivityByTypeAndUserId(user.getId(), null);

        // Calculate counts
        long taskCount = activities.stream()
                .filter(a -> !a.getType().name().contains("PROJECT"))
                .count();

        long projectCount = activities.stream()
                .filter(activity -> activity.getType().name().contains("PROJECT"))
                .count();

        mv.addObject("user", user);
        mv.addObject("activities", activities);
        mv.addObject("taskCount", taskCount);
        mv.addObject("projectCount", projectCount);

        return mv;
    }

    @DeleteMapping("/{id}")
    public String clearActivity(@PathVariable UUID id) {
        activityService.deleteActivity(id);
        return "redirect:/activity";
    }
}
