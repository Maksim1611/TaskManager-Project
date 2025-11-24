package com.example.TaskManager.security;

import com.example.TaskManager.user.model.UserRole;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
public class UserData implements UserDetails, OAuth2User  {

    private UUID id;
    private String email;
    private String password;
    private boolean isActive;
    private UserRole role;
    private Map<String, Object> attributes;

    public UserData(UUID id, String email, String password, boolean isActive, UserRole role, Map<String, Object> attributes ) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.isActive = isActive;
        this.role = role;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority role = new SimpleGrantedAuthority("ROLE_" + this.role.name());

        return List.of(role);
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.isActive;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.isActive;
    }

    @Override
    public boolean isEnabled() {
        return this.isActive;
    }

    @Override
    public String getName() {
        return email;
    }
}
