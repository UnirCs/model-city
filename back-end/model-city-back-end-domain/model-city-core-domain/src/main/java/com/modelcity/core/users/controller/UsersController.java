package com.modelcity.core.users.controller;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.common.i18n.SupportedLocale;
import com.modelcity.common.security.ModelCityAccess;
import com.modelcity.core.users.controller.model.SetUserStatusRequestDto;
import com.modelcity.core.users.controller.model.SignInRequestDto;
import com.modelcity.core.users.controller.model.UserProfileDto;
import com.modelcity.core.users.controller.model.UserSummaryDto;
import com.modelcity.core.users.repository.model.UserRole;
import com.modelcity.core.users.usecase.DeleteUserUseCase;
import com.modelcity.core.users.usecase.FindUserUseCase;
import com.modelcity.core.users.usecase.GetUserUseCase;
import com.modelcity.core.users.usecase.ListUsersUseCase;
import com.modelcity.core.users.usecase.RegisterUserUseCase;
import com.modelcity.core.users.usecase.SetUserStatusUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

/**
 * User profile, listing and sign-in endpoints.
 *
 * <p>Overridable base controller (abstract): the platform registers {@link DefaultUsersController} as the
 * default bean. A local deployment overrides by declaring its own {@code @RestController} that
 * {@code extends UsersController}; the default then backs off.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@ModelCityExtensionPoint
public abstract class UsersController<T extends UserProfileDto, S extends UserSummaryDto, R extends SignInRequestDto> {

    protected final GetUserUseCase<T> getUserUseCase;
    protected final RegisterUserUseCase<R> registerUserUseCase;
    protected final FindUserUseCase findUserUseCase;
    protected final ListUsersUseCase<S> listUsersUseCase;
    protected final DeleteUserUseCase deleteUserUseCase;
    protected final SetUserStatusUseCase setUserStatusUseCase;

    /**
     * Lists users. Admin only. Optional filters: {@code citizen} (true=citizens, false=workers),
     * {@code name} (case-insensitive substring), {@code neighbourhoodId} (citizens) and {@code role} (workers).
     */
    @GetMapping
    @ModelCityAccess.PlatformAdmin
    public Page<S> listUsers(
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestParam(required = false) Boolean citizen,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long neighbourhoodId,
            @RequestParam(required = false) String role,
            @PageableDefault(size = 20) Pageable pageable) {
        UserRole roleFilter = (role == null || role.isBlank()) ? null : UserRole.fromValue(role);
        return listUsersUseCase.execute(sub, citizen, name, neighbourhoodId, roleFilter, pageable);
    }

    /** Enables or disables a user account. Admin only. */
    @PatchMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ModelCityAccess.PlatformAdmin
    public void setUserStatus(
            @RequestHeader("X-Auth-Sub") String sub,
            @PathVariable String userId,
            @RequestBody @Valid SetUserStatusRequestDto request) {
        setUserStatusUseCase.execute(sub, userId, request.getStatus());
    }

    /** Returns the profile of the given user. Admins may request any profile; citizens only their own. */
    @GetMapping("/{userId}")
    public ResponseEntity<T> getProfile(
            @RequestHeader("X-Auth-Sub") String sub,
            @PathVariable String userId,
            Locale locale) {
        return ResponseEntity.ok(getUserUseCase.execute(sub, userId, SupportedLocale.from(locale).code()));
    }

    /** Registers or updates a citizen on sign-in (JIT provisioning). */
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void signIn(
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid R request) {
        registerUserUseCase.execute(sub, request);
    }

    /** Checks whether a citizen with the given sub exists. */
    @RequestMapping(value = "/{sub}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> citizenExists(@PathVariable String sub) {
        return findUserUseCase.execute(sub)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    /** Permanently removes a citizen record. Admin only. */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ModelCityAccess.PlatformAdmin
    public void deleteUser(
            @RequestHeader("X-Auth-Sub") String sub,
            @PathVariable String userId) {
        deleteUserUseCase.execute(sub, userId);
    }
}
