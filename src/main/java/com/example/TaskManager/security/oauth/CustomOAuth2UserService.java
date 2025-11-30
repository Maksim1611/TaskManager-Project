package com.example.TaskManager.security.oauth;

import com.example.TaskManager.security.UserData;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {


    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    public CustomOAuth2UserService(UserRepository userRepository, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauth = super.loadUser(request);

        String registrationId = request.getClientRegistration().getRegistrationId();
        String email = null;
        Map<String, Object> attributes = new HashMap<>(oauth.getAttributes());

        if (registrationId.equals("google")) {
            email = oauth.getAttribute("email");
            attributes.put("provider", "Google");

        }

        if (registrationId.equals("github")) {

            email = oauth.getAttribute("email");

            if (email == null) {
                email = fetchGithubPrimaryEmail(request);
                attributes.put("email", email);
            }
            attributes.put("provider", "Github");
        }

        Optional<User> existing = userRepository.findByEmail(email);

        if (existing.isEmpty()) {

            return new DefaultOAuth2User(
                    List.of(new SimpleGrantedAuthority("ROLE_TEMP")),
                    attributes,
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

    private String fetchGithubPrimaryEmail(OAuth2UserRequest request) {
        String token = request.getAccessToken().getTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "token " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response =
                restTemplate.exchange(
                        "https://api.github.com/user/emails",
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<>() {}
                );

        List<Map<String, Object>> emails = response.getBody();

        return emails.stream()
                .filter(e -> Boolean.TRUE.equals(e.get("primary")))
                .map(e -> (String) e.get("email"))
                .findFirst()
                .orElse(null);
    }
}

