package com.example.TaskManager.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotNull
    private String currentPassword;

    @NotNull
    private String newPassword;

    @NotNull
    private String confirmPassword;

}
