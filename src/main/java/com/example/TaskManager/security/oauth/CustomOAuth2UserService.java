package com.example.TaskManager.security.oauth;

import com.example.TaskManager.security.UserData;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.model.UserRole;
import com.example.TaskManager.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {


    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauth = super.loadUser(request);

        String registrationId = request.getClientRegistration().getRegistrationId();
        String email = null;

        if (registrationId.equals("google")) {
            email = oauth.getAttribute("email");
        }

        if (registrationId.equals("github")) {

            email = oauth.getAttribute("email");

            if (email == null) {
                String username = oauth.getAttribute("login");
                email = username + "@github-user.com";
            }
        }

        Optional<User> existing = userRepository.findByEmail(email);

        if (existing.isEmpty()) {

            return new DefaultOAuth2User(
                    List.of(new SimpleGrantedAuthority("ROLE_TEMP")),
                    oauth.getAttributes(),
                    "email"
            );
        }
        User user = existing.get();

        if (!user.isActive()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("blocked"));
        }

        return new UserData(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                true,
                user.getRole(),
                oauth.getAttributes()
        );
    }
}

