package io.lin.read.config.jwt.aUserDetails;

import io.lin.read.security.AuthenticatedUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final AuthenticatedUser user;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(AuthenticatedUser user) {
        this.user = user;
        this.authorities = user.roles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .toList();
    }

    public AuthenticatedUser getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return user.username();
    }
}
