package com.modelcity.core.users.repository.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * User roles in Model City.
 * Java constants use _ (language constraint); serialized value uses - everywhere (JSON + DB).
 * Default role for new sign-ins: {@code MODEL_CITY_CITIZEN}.
 */
public enum UserRole {
    MODEL_CITY_PLATFORM_ADMIN("MODEL-CITY-PLATFORM-ADMIN"),
    MODEL_CITY_OPERATOR("MODEL-CITY-OPERATOR"),
    MODEL_CITY_BACKOFFICE("MODEL-CITY-BACKOFFICE"),
    MODEL_CITY_MOBILITY_AGENT("MODEL-CITY-MOBILITY-AGENT"),
    MODEL_CITY_CITIZEN("MODEL-CITY-CITIZEN");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UserRole fromValue(String value) {
        for (UserRole role : values()) {
            if (role.value.equals(value)) return role;
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}
