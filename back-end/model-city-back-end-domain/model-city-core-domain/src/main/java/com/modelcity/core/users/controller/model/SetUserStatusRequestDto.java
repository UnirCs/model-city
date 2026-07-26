package com.modelcity.core.users.controller.model;

import com.modelcity.core.users.repository.model.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for PATCH /users/{userId}. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SetUserStatusRequestDto {

    @NotNull private UserStatus status;
}
