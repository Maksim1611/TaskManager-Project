package com.example.TaskManager.web;

import com.example.TaskManager.exception.MemberAlreadyExistException;
import com.example.TaskManager.exception.task.TaskAlreadyExistException;
import com.example.TaskManager.exception.user.EmailAlreadyExistException;
import com.example.TaskManager.exception.user.MemberNotFoundException;
import com.example.TaskManager.exception.user.UserNotFoundException;
import com.example.TaskManager.exception.user.UsernameAlreadyExistException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException.class)
    public String handleException() {
        return "not-found";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            NoResourceFoundException.class,
            AccessDeniedException.class,
            AuthorizationDeniedException.class
    })
    public ModelAndView handleSpringException() {

        return new ModelAndView("not-found");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleLeftoverException() {
        return new ModelAndView("internal-server-error");
    }

    @ExceptionHandler(TaskAlreadyExistException.class)
    public String handleTaskAlreadyExists(TaskAlreadyExistException ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {

        String url = request.getRequestURI();

        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());

        if (url.equals("/tasks/new-task")) {
            return "redirect:/add-task";
        }

        if (url.startsWith("/tasks/new-task/")) {
            return "redirect:/add-task-project";
        }

        if (url.matches("/tasks/[0-9a-fA-F\\-]{36}")) {
            return "redirect:/edit-task";
        }

        return "redirect:/tasks";
    }

    @ExceptionHandler(UsernameAlreadyExistException.class)
    public String handleUsernameAlreadyExist(UsernameAlreadyExistException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessageUsername", ex.getMessage());
        return "redirect:/register";
    }

    @ExceptionHandler(EmailAlreadyExistException.class)
    public String handleEmailAlreadyExist(EmailAlreadyExistException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessageEmail", ex.getMessage());
        return "redirect:/register";
    }

    @ExceptionHandler(MemberAlreadyExistException.class)
    public String handleMemberAlreadyExistException(MemberAlreadyExistException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessageMember", ex.getMessage());
        return "redirect:/projects/{id}/invitation";
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public String handleMemberNotFoundException(MemberNotFoundException ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {

        String uri = request.getRequestURI();
        redirectAttributes.addFlashAttribute("errorMessageMember", ex.getMessage());

        if (uri.endsWith("/invitation")) {
            return "redirect:/projects/{id}/invitation";
        } else {
            return "redirect:/projects/{id}/member";
        }
    }
}
