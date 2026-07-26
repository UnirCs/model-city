package com.modelcity.core.users.controller;

import com.modelcity.core.users.controller.model.SetUserStatusRequestDto;
import com.modelcity.core.users.controller.model.SignInRequestDto;
import com.modelcity.core.users.controller.model.UserProfileDto;
import com.modelcity.core.users.controller.model.UserSummaryDto;
import com.modelcity.core.users.repository.model.UserRole;
import com.modelcity.core.users.repository.model.UserStatus;
import com.modelcity.core.users.usecase.DeleteUserUseCase;
import com.modelcity.core.users.usecase.FindUserUseCase;
import com.modelcity.core.users.usecase.GetUserUseCase;
import com.modelcity.core.users.usecase.ListUsersUseCase;
import com.modelcity.core.users.usecase.RegisterUserUseCase;
import com.modelcity.core.users.usecase.SetUserStatusUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsersControllerTest {

    @Mock GetUserUseCase<UserProfileDto> getUserUseCase;
    @Mock RegisterUserUseCase<SignInRequestDto> registerUserUseCase;
    @Mock FindUserUseCase findUserUseCase;
    @Mock ListUsersUseCase<UserSummaryDto> listUsersUseCase;
    @Mock DeleteUserUseCase deleteUserUseCase;
    @Mock SetUserStatusUseCase setUserStatusUseCase;

    DefaultUsersController controller;

    @BeforeEach
    void setUp() {
        controller = new DefaultUsersController(getUserUseCase, registerUserUseCase, findUserUseCase,
                listUsersUseCase, deleteUserUseCase, setUserStatusUseCase);
    }

    @Test
    void listUsers_withoutRoleFilter_passesNullRole() {
        controller.listUsers("admin-sub", true, "Ana", 5L, null, PageRequest.of(0, 20));
        verify(listUsersUseCase).execute("admin-sub", true, "Ana", 5L, null, PageRequest.of(0, 20));
    }

    @Test
    void listUsers_withBlankRole_passesNullRole() {
        controller.listUsers("admin-sub", null, null, null, "  ", PageRequest.of(0, 20));
        verify(listUsersUseCase).execute("admin-sub", null, null, null, null, PageRequest.of(0, 20));
    }

    @Test
    void listUsers_withRoleFilter_resolvesUserRole() {
        controller.listUsers("admin-sub", false, null, null, "MODEL-CITY-BACKOFFICE", PageRequest.of(0, 20));
        verify(listUsersUseCase).execute("admin-sub", false, null, null,
                UserRole.MODEL_CITY_BACKOFFICE, PageRequest.of(0, 20));
    }

    @Test
    void listUsers_withInvalidRole_throwsIllegalArgument() {
        assertThatThrownBy(() -> controller.listUsers("admin-sub", null, null, null, "NOT-A-ROLE", PageRequest.of(0, 20)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void setUserStatus_delegatesToUseCase() {
        SetUserStatusRequestDto request = new SetUserStatusRequestDto(UserStatus.DISABLED);
        controller.setUserStatus("admin-sub", "target-sub", request);
        verify(setUserStatusUseCase).execute("admin-sub", "target-sub", UserStatus.DISABLED);
    }

    @Test
    void getProfile_returnsOkWithResolvedLocale() {
        UserProfileDto profile = new UserProfileDto();
        when(getUserUseCase.execute("user-sub", "user-sub", "en")).thenReturn(profile);

        ResponseEntity<UserProfileDto> result = controller.getProfile("user-sub", "user-sub", Locale.ENGLISH);

        assertThat(result.getBody()).isEqualTo(profile);
    }

    @Test
    void signIn_delegatesToUseCase() {
        SignInRequestDto request = new SignInRequestDto("Nombre", "email@example.com", "el-recreo");
        controller.signIn("user-sub", request);
        verify(registerUserUseCase).execute("user-sub", request);
    }

    @Test
    void citizenExists_returnsOkWhenFound() {
        when(findUserUseCase.execute("user-sub")).thenReturn(true);

        ResponseEntity<Void> result = controller.citizenExists("user-sub");

        assertThat(result.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void citizenExists_returnsNotFoundWhenMissing() {
        when(findUserUseCase.execute("missing-sub")).thenReturn(false);

        ResponseEntity<Void> result = controller.citizenExists("missing-sub");

        assertThat(result.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void deleteUser_delegatesToUseCase() {
        controller.deleteUser("admin-sub", "target-sub");
        verify(deleteUserUseCase).execute("admin-sub", "target-sub");
    }
}
