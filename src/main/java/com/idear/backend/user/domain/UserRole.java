package com.idear.backend.user.domain;

public enum UserRole {
    USER,
    ADMIN;

    public String toRoleString() {
        return "ROLE_"+this.name();
    }
}