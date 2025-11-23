package com.example.TaskManager.web;

import com.example.TaskManager.project.model.Project;
import com.example.TaskManager.project.model.ProjectStatus;
import com.example.TaskManager.project.security.ProjectSecurity;
import com.example.TaskManager.security.UserData;
import com.example.TaskManager.project.service.ProjectService;
import com.example.TaskManager.task.model.Task;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.service.UserService;
import com.example.TaskManager.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;
    private final ProjectSecurity projectSecurity;

    public ProjectController(ProjectService projectService, UserService userService, ProjectSecurity projectSecurity) {
        this.projectService = projectService;
        this.userService = userService;
        this.projectSecurity = projectSecurity;
    }

    @GetMapping
    public ModelAndView getProjects(@AuthenticationPrincipal UserData userData, @RequestParam(required = false) String status) {
       ModelAndView mv = new ModelAndView("projects");
        List<Project> projects;
        User user = userService.getById(userData.getId());

        if (status != null) {
            projects = projectService.getAllByUserIdAndDeletedFalseAndStatus(user.getId(), ProjectStatus.valueOf(status));
        } else {
            projects = projectService.getProjectsIncludedIn(user);
        }

        mv.addObject("projects",projects);
        mv.addObject("user",user);
        mv.addObject("status",status);
        mv.addObject("activeProjects", projectService.getAllByUserIdAndDeletedFalseAndStatus(user.getId(), ProjectStatus.ACTIVE));
        mv.addObject("inProgressProjects", projectService.getAllByUserIdAndDeletedFalseAndStatus(user.getId(), ProjectStatus.IN_PROGRESS));
        mv.addObject("completedProjects", projectService.getAllByUserIdAndDeletedFalseAndStatus(user.getId(), ProjectStatus.COMPLETED));

        return mv;
    }

    @GetMapping("/{id}")
    public ModelAndView getProjectPage(@AuthenticationPrincipal UserData userData ,@PathVariable UUID id) {
        ModelAndView mv = new ModelAndView("project");
        User user = userService.getById(userData.getId());
        Project project = projectService.getByIdNotDeleted(id);
        projectService.calculateCompletionPercent(project);
        String members = projectService.getMembersToString(id);
        List<Task> tasks = project.getTasks().stream().sorted(Comparator.comparing(Task::getCreatedOn)).toList();

        mv.addObject("user",user);
        mv.addObject("project",project);
        mv.addObject("tasks",tasks);
        mv.addObject("members",members);
        mv.addObject("isOwner", project.getUser().getId().equals(user.getId()));

        return mv;
    }

    @GetMapping("/new-project")
    public ModelAndView addProjectPage(@AuthenticationPrincipal UserData userData) {
        ModelAndView mv = new ModelAndView("add-project");
        User user = userService.getById(userData.getId());

        mv.addObject("user",user);
        mv.addObject("createProjectRequest",new CreateProjectRequest());

        return mv;
    }

    @PostMapping("/new-project")
    public ModelAndView createProject(@Valid @ModelAttribute CreateProjectRequest createProjectRequest, BindingResult bindingResult, @AuthenticationPrincipal UserData userData) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("add-project");
        }

        User user = userService.getById(userData.getId());

        this.projectService.createProject(createProjectRequest,user);

        return new ModelAndView("redirect:/projects");
    }

    @PreAuthorize("@projectSecurity.isOwner(#id, authentication)")
    @GetMapping("/{id}/project")
    public ModelAndView getEditProjectPage(@AuthenticationPrincipal UserData userData, @PathVariable UUID id) {
        ModelAndView mv = new ModelAndView("edit-project");
        Project project = projectService.getByIdNotDeleted(id);
        User user = userService.getById(userData.getId());

        EditProjectRequest editProjectRequest = DtoMapper.fromProject(project, projectService.getProjectTagsAsString(project));

        mv.addObject("user",user);
        mv.addObject("projectId", project.getId());
        mv.addObject("editProjectRequest",editProjectRequest);

        return mv;
    }


    @PutMapping("/{id}/project")
    public ModelAndView editProject(@Valid @ModelAttribute EditProjectRequest editProjectRequest, BindingResult bindingResult, @AuthenticationPrincipal UserData userData, @PathVariable UUID id) {
        if (bindingResult.hasErrors()) {
            ModelAndView mv = new ModelAndView("edit-project");
            User user = userService.getById(userData.getId());
            mv.addObject("user",user);
            return mv;
        }

        projectService.editProject(editProjectRequest, id);
        return new ModelAndView("redirect:/projects");
    }

    @PatchMapping("/{id}/project")
    public String completeProject(@PathVariable UUID id) {
        projectService.completeProject(id);
        return "redirect:/projects";
    }

    @PreAuthorize("@projectSecurity.isOwner(#id, authentication)")
    @DeleteMapping("/{id}/project")
    public ModelAndView deleteProject(@PathVariable UUID id) {
        projectService.delete(id);
        return new ModelAndView("redirect:/projects");
    }

    @PreAuthorize("@projectSecurity.isOwner(#id, authentication)")
    @GetMapping("/{id}/invitation")
    public ModelAndView getMemberPage(@AuthenticationPrincipal UserData userData, @PathVariable UUID id) {
        ModelAndView mv = new ModelAndView("invite-member");
        mv.addObject("user",userService.getById(userData.getId()));
        mv.addObject("inviteMemberRequest",new InviteMemberRequest());
        return mv;
    }

    @PostMapping("/{id}/invitation")
    public ModelAndView addMemberPage(@AuthenticationPrincipal UserData userData, @PathVariable UUID id,@ModelAttribute InviteMemberRequest inviteMemberRequest, BindingResult bindingResult) {
        User user = userService.getById(userData.getId());

        if (bindingResult.hasErrors()) {
            ModelAndView mv = new ModelAndView();
            mv.addObject("user", user);
            mv.setViewName("invite-member");
            return mv;
        }

        projectService.inviteMember(inviteMemberRequest, id, user);
        return new ModelAndView("redirect:/projects/%s".formatted(id));
    }

    @PreAuthorize("@projectSecurity.isOwner(#id, authentication)")
    @GetMapping("/{id}/member")
    public ModelAndView removeMemberPage(@PathVariable UUID id, @AuthenticationPrincipal UserData userData) {
        ModelAndView mv = new ModelAndView("remove-member");
        mv.addObject("user", userService.getById(userData.getId()));
        mv.addObject("removeMemberRequest",new RemoveMemberRequest());
        return mv;
    }

    @PreAuthorize("@projectSecurity.isOwner(#id, authentication)")
    @PostMapping("/{id}/member")
    public ModelAndView removeMember(@PathVariable UUID id, @Valid @ModelAttribute RemoveMemberRequest removeMemberRequest,
                               @AuthenticationPrincipal UserData userData ,BindingResult bindingResult) {
        User user = userService.getById(userData.getId());

        if (bindingResult.hasErrors()) {
            ModelAndView mv = new ModelAndView();
            mv.addObject("user", user);
            mv.setViewName("remove-member");
            return mv;
        }

        projectService.removeMember(id, user, removeMemberRequest);
        return new ModelAndView("redirect:/projects/%s".formatted(id));
    }

}
