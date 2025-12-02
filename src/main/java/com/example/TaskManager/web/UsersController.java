package com.example.TaskManager.web;

import com.example.TaskManager.security.UserData;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.service.UserService;
import com.example.TaskManager.web.dto.ChangePasswordRequest;
import com.example.TaskManager.web.dto.DtoMapper;
import com.example.TaskManager.web.dto.EditProfileRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UsersController {

    private final UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getUsersPage(@AuthenticationPrincipal UserData userData) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("users");
        List<User> users = userService.getSortedUsers();

        User user = userService.getById(userData.getId());

        modelAndView.addObject("user", user);
        modelAndView.addObject("users", users);

        return modelAndView;
    }

    @PutMapping(value = "/{id}/profile", consumes = "multipart/form-data")
    public ModelAndView editProfile(@RequestParam(value = "image", required = false) MultipartFile image,
                                    @RequestParam(value = "removeImage", required = false) Boolean removeImage,
                                    @Valid @ModelAttribute EditProfileRequest editProfileRequest,
                                    BindingResult bindingResult,
                                    @PathVariable UUID id) throws IOException {

        if (bindingResult.hasErrors()) {
            User user = userService.getById(id);
            ModelAndView modelAndView = new ModelAndView("settings");
            modelAndView.addObject("user", user);
            modelAndView.addObject("changePasswordRequest", new ChangePasswordRequest());
            return modelAndView;
        }

        User user = userService.getById(id);

        if (Boolean.TRUE.equals(removeImage)) {
            user.setImage(null);
        } else if (image != null && !image.isEmpty()) {
            user.setImage(image.getBytes());
        }
        userService.update(user);

        userService.updateProfile(id, editProfileRequest);
        return new ModelAndView("redirect:/dashboard");
    }

    @GetMapping("/{id}/profile")
    public ModelAndView profilePage(@PathVariable UUID id) {
        ModelAndView modelAndView = new ModelAndView("settings");
        User user = userService.getById(id);
        EditProfileRequest editProfileRequest = DtoMapper.fromUser(user);

        if (user.getImage() != null && user.getImage().length > 0) {
            String base64Image = Base64.getEncoder().encodeToString(user.getImage());
            modelAndView.addObject("base64Image", base64Image);
        }

        modelAndView.addObject("user", user);
        modelAndView.addObject("editProfileRequest", editProfileRequest);
        modelAndView.addObject("changePasswordRequest", new ChangePasswordRequest());

        return modelAndView;
    }

    @PutMapping("/{id}/password")
    public ModelAndView changePassword(@PathVariable UUID id,@Valid @ModelAttribute ChangePasswordRequest changePasswordRequest, BindingResult bindingResult) {
        User user = userService.getById(id);

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("settings");
            modelAndView.addObject("user", user);
            modelAndView.addObject("changePasswordRequest", changePasswordRequest);
            modelAndView.addObject("editProfileRequest", DtoMapper.fromUser(user));
            return modelAndView;
        }

        userService.changePassword(changePasswordRequest, user);
        return new ModelAndView("redirect:/dashboard");
    }

    @DeleteMapping("/{id}/user")
    public String deleteUser(@PathVariable UUID id, HttpSession session) {
        userService.deleteUser(id);
        session.invalidate();

        return "redirect:/logout";
    }


    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeRole(@PathVariable UUID id ) {
        User user = userService.getById(id);
        userService.changeRole(user.getId());
        return "redirect:/users";
    }


    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public String blockUserAccount(@PathVariable UUID id) {
        User user = userService.getById(id);
        userService.blockAccount(id);
        return "redirect:/users";
    }

}
