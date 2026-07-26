package com.modelcity.core.users.controller.model;

import com.modelcity.core.users.repository.model.UserRole;
import com.modelcity.core.users.repository.model.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private String id;
    private String name;
    private String email;
    private Instant createdAt;
    private NeighbourhoodSummaryDto neighbourhood;
    private UserRole role;
    private UserStatus status;
}
