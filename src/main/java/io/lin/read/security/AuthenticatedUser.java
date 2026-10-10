package io.lin.read.security;

import java.util.Set;

public record AuthenticatedUser(
        Long id,
        String username,
        String displayName,
        Set<Role> roles
) {
    public boolean hasRole(Role role) {
        return roles != null && roles.contains(role);
    }
}
