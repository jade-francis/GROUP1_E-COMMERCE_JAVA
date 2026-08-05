package com.group1.shopease.security;

import com.group1.shopease.service.UserService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class GoogleOidcUserService extends OidcUserService {
    private final UserService userService;

    public GoogleOidcUserService(UserService userService) { this.userService = userService; }

    @Override
    public OidcUser loadUser(OidcUserRequest request) throws OAuth2AuthenticationException {
        OidcUser googleUser = super.loadUser(request);
        String email = googleUser.getEmail();
        if (email == null || !Boolean.TRUE.equals(googleUser.getEmailVerified())) {
            throw new OAuth2AuthenticationException(new OAuth2Error("unverified_email"),
                    "Google did not provide a verified email address");
        }
        var localUser = userService.findOrCreateGoogleUser(googleUser.getFullName(), email);
        return new DefaultOidcUser(
                java.util.List.of(new SimpleGrantedAuthority("ROLE_" + localUser.getRole())),
                googleUser.getIdToken(),
                googleUser.getUserInfo(),
                "email"
        );
    }
}
