package com.modelcity.core.users.controller;

import com.modelcity.core.users.controller.model.SignInRequestDto;
import com.modelcity.core.users.controller.model.UserProfileDto;
import com.modelcity.core.users.controller.model.UserSummaryDto;
import com.modelcity.core.users.usecase.DeleteUserUseCase;
import com.modelcity.core.users.usecase.FindUserUseCase;
import com.modelcity.core.users.usecase.GetUserUseCase;
import com.modelcity.core.users.usecase.ListUsersUseCase;
import com.modelcity.core.users.usecase.RegisterUserUseCase;
import com.modelcity.core.users.usecase.SetUserStatusUseCase;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;

/**
 * Default concrete {@link UsersController}. The component-scanned platform default; disabled at startup when a
 * local deployment provides its own bean for the seam.
 */
@RestController
@ModelCityDisabledIfInherited
public class DefaultUsersController extends UsersController<UserProfileDto, UserSummaryDto, SignInRequestDto> {

    public DefaultUsersController(
            GetUserUseCase<UserProfileDto> getUserUseCase,
            RegisterUserUseCase<SignInRequestDto> registerUserUseCase,
            FindUserUseCase findUserUseCase,
            ListUsersUseCase<UserSummaryDto> listUsersUseCase,
            DeleteUserUseCase deleteUserUseCase,
            SetUserStatusUseCase setUserStatusUseCase) {
        super(getUserUseCase, registerUserUseCase, findUserUseCase, listUsersUseCase, deleteUserUseCase,
                setUserStatusUseCase);
    }
}
