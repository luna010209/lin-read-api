package io.lin.read.security;

import lombok.Getter;

public enum Role {
    ROLE_TEACHER,
    ROLE_ADMIN;

    public String getAuthority() {
        return name();
    }
}
