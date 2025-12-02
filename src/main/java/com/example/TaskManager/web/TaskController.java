package com.example.TaskManager.web;

import com.example.TaskManager.project.service.ProjectService;
import com.example.TaskManager.security.UserData;
import com.example.TaskManager.task.model.Task;
import com.example.TaskManager.task.service.TaskService;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.service.UserService;
import com.example.TaskManager.web.dto.CreateTaskRequest;
import com.example.TaskManager.web.dto.DtoMapper;
import com.example.TaskManager.web.dto.EditTaskRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;
    private final ProjectService projectService;

    public TaskController(TaskService taskService, UserService userService, ProjectService projectService) {
        this.taskService = taskService;
        this.userService = userService;
        this.projectService = projectService;
    }

    @GetMapping
    public ModelAndView getTasksPage(@AuthenticationPrincipal UserData userData) {
        User user = userService.getById(userData.getId());

        return taskService.buildTasksPageView(user);

    }

    @GetMapping("/new-task")
    public ModelAndView getCreateTaskPage(@AuthenticationPrincipal UserData userData) {
        ModelAndView mv = new ModelAndView("add-task");
        mv.addObject("createTaskRequest", new CreateTaskRequest());
        User user = userService.getById(userData.getId());
        mv.addObject("user", user);

        return mv;
    }

    @PreAuthorize("@projectSecurity.isOwner(#projectId, authentication)")
    @GetMapping("/new-task/{projectId}")
    public ModelAndView getCreateTaskPageForProject(@AuthenticationPrincipal UserData userData, @PathVariable UUID projectId) {
        ModelAndView mv = new ModelAndView("add-task-project");
        mv.addObject("createTaskRequest", new CreateTaskRequest());
        User user = userService.getById(userData.getId());
        mv.addObject("user", user);
        mv.addObject("projectId", projectId);

        return mv;
    }

    @PostMapping("/new-task/{projectId}")
    public ModelAndView createTaskWithProject(@Valid CreateTaskRequest createTaskRequest, BindingResult bindingResult, @AuthenticationPrincipal UserData userData, @PathVariable(required = false) UUID projectId) {
        User user = this.userService.getById(userData.getId());

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("add-task-project");
            modelAndView.addObject("user", user);
            return modelAndView;
        }

        Task task = this.taskService.createTask(createTaskRequest, user.getId(), projectService.getByIdNotDeleted(projectId));

        return new ModelAndView("redirect:/projects/" + task.getProject().getId());
    }

    @PostMapping("/new-task")
    public ModelAndView createTaskWithoutProject(@Valid CreateTaskRequest createTaskRequest, BindingResult bindingResult, @AuthenticationPrincipal UserData userData) {
        User user = this.userService.getById(userData.getId());

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("add-task");
            modelAndView.addObject("user", user);
            return modelAndView;
        }

        Task task = this.taskService.createTask(createTaskRequest, user.getId(), null);

        return new ModelAndView("redirect:/tasks");
    }

    @PatchMapping("/{id}/status")
    public String changeStatus(@PathVariable UUID id) {
        taskService.changeStatus(id);
        return taskService.checkIfTaskHasProjectRedirect(id);
    }

    @PatchMapping("/{id}/task")
    public String completeTask(@PathVariable UUID id) {
        taskService.completeTask(id);
        return taskService.checkIfTaskHasProjectRedirect(id);
    }

    @DeleteMapping("/{id}/task")
    public String deleteTask(@PathVariable UUID id) {
        String path = taskService.checkDeletedTaskHasProjectRedirect(id);
        taskService.deleteTask(id);
        return path;
    }

    @GetMapping("/{id}/task")
    public ModelAndView getEditPage(@PathVariable UUID id, @AuthenticationPrincipal UserData userData) {
        Task task = taskService.getByIdNotDeleted(id);
        EditTaskRequest editTaskRequest = DtoMapper.fromTask(task);

        ModelAndView mv = new ModelAndView("edit-task");

        mv.addObject("user", userService.getById(userData.getId()));
        mv.addObject("editTaskRequest", editTaskRequest);
        mv.addObject("taskId", id);

        return mv;
    }

    @PutMapping("/{id}/task")
    public ModelAndView editTask(@PathVariable UUID id, @Valid @ModelAttribute EditTaskRequest editTaskRequest, BindingResult bindingResult, @AuthenticationPrincipal UserData userData) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("edit-task");
            User user = userService.getById(userData.getId());
            modelAndView.addObject("user", user);
            return modelAndView;
        }

        taskService.editTask(id, editTaskRequest);
        String redirect = taskService.checkIfTaskHasProjectRedirect(id);

        return new ModelAndView(redirect);
    }

}
