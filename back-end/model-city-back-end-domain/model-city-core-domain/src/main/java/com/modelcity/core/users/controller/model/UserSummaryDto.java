package com.modelcity.core.users.controller.model;

import com.modelcity.core.users.repository.model.UserRole;
import com.modelcity.core.users.repository.model.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Lightweight user projection returned by the admin list endpoint (citizen / worker cards). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {

    private String id;
    private String name;
    private String email;
    private UserRole role;
    private UserStatus status;
    private Long neighbourhoodId;
    private String neighbourhoodName;
    private Instant createdAt;
}
