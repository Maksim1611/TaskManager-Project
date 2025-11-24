package com.example.TaskManager.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EditProfileRequest {

    @Size(min = 3, max = 20)
    private String username;

    @Size(min = 3, max = 20)
    private String firstName;

    @Size(min = 3, max = 20)
    private String lastName;

    @Email
    private String email;

}
