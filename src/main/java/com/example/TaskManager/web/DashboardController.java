package com.example.TaskManager.web;

import com.example.TaskManager.activity.model.Activity;
import com.example.TaskManager.activity.service.ActivityService;
import com.example.TaskManager.security.UserData;
import com.example.TaskManager.project.service.ProjectService;
import com.example.TaskManager.task.model.TaskStatus;
import com.example.TaskManager.task.service.TaskService;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;
    private final TaskService taskService;
    private final ProjectService projectService;
    private final ActivityService activityService;

    public DashboardController(UserService userService, TaskService taskService, ProjectService projectService, ActivityService activityService) {
        this.userService = userService;
        this.taskService = taskService;
        this.projectService = projectService;
        this.activityService = activityService;
    }

    @GetMapping
    public ModelAndView getDashboardPage(@AuthenticationPrincipal UserData userData) {
        ModelAndView modelAndView = new ModelAndView("dashboard");
        User user = userService.getById(userData.getId());
        List<Activity> recentActivity = activityService.getActivityByTypeAndUserId(user.getId(), null).stream().limit(3).toList();

        long activeTasksCount = taskService.getAllTasksByUserIdAndProjectNull(user.getId()).stream().filter(task -> !task.getStatus().equals(TaskStatus.OVERDUE)).count();
        long activeProjectsCount = projectService.getAllByUserIdAndDeletedFalse(user.getId()).stream().filter(project -> project.getStatus().getDisplayName().equals("Active")).count();

        modelAndView.addObject("dueThisWeek", taskService.getTasksDueThisWeek(user.getId()));
        modelAndView.addObject("completionRate", taskService.getAllTimeCompletionRate(user.getId()));
        modelAndView.addObject("activeTasksCount", activeTasksCount);
        modelAndView.addObject("user", user);
        modelAndView.addObject("completedTasks", taskService.getAllTasksByUserIdAndStatusNotDeleted(user.getId(),TaskStatus.COMPLETED).size());
        modelAndView.addObject("overDueTasks", taskService.getOverDueTasksByUser(user).size());
        modelAndView.addObject("activeProjectsCount", activeProjectsCount);
        modelAndView.addObject("recentTasks", taskService.getRecentTasks(user));
        modelAndView.addObject("recentProjects", projectService.getRecentProjects(user));
        modelAndView.addObject("recentActivity", recentActivity);
        modelAndView.addObject("upcomingTasks", taskService.getUpcomingTasks(user.getId()));

        return modelAndView;
    }

}
