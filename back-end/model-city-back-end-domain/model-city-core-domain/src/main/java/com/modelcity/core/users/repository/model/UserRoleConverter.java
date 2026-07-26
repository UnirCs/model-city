package com.modelcity.core.users.repository.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Persists {@link UserRole} using its dash-formatted value (e.g. {@code MODEL-CITY-CITIZEN}). */
@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole role) {
        return role == null ? null : role.getValue();
    }

    @Override
    public UserRole convertToEntityAttribute(String value) {
        return value == null ? null : UserRole.fromValue(value);
    }
}

