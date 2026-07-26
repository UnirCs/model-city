package com.modelcity.core.users.usecase;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.core.trails.SystemTrailGenerator;
import com.modelcity.core.users.controller.model.SignInRequestDto;
import com.modelcity.core.users.facade.Auth0ManagementFacade;
import com.modelcity.core.users.repository.NeighbourhoodRepository;
import com.modelcity.core.users.repository.UserRepository;
import com.modelcity.core.users.repository.model.Neighbourhood;
import com.modelcity.core.users.repository.model.User;
import com.modelcity.core.users.repository.model.UserRole;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/** Default {@link RegisterUserUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultRegisterUserUseCase implements RegisterUserUseCase<SignInRequestDto> {

    private final UserRepository userRepository;
    private final NeighbourhoodRepository neighbourhoodRepository;
    private final Auth0ManagementFacade auth0ManagementFacade;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.CITIZEN_EXISTS, key = "#sub"),
        @CacheEvict(cacheNames = CacheNames.USER_PROFILE, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.USER_LIST, allEntries = true)
    })
    public void execute(String sub, SignInRequestDto request) {
        Neighbourhood neighbourhood = neighbourhoodRepository
                .findByName(request.getNeighbourhoodName())
                .orElseThrow(() -> new ResourceNotFoundException("Neighbourhood", request.getNeighbourhoodName()));

        User user = userRepository.findById(sub).orElse(null);
        boolean isNew = user == null;
        if (isNew) {
            UserRole role = resolveRole(sub);
            log.info("New user registered via JIT provisioning: sub={} role={}", sub, role);
            user = new User();
            user.setId(sub);
            user.setRole(role);
        } else {
            log.debug("Sign-in from existing user: sub={}", sub);
        }
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setNeighbourhood(neighbourhood);
        userRepository.save(user);

        if (isNew) {
            systemEventGenerator.userRegistered(user);
        } else {
            systemEventGenerator.userUpdated(user);
        }
    }

    /** Resolves the application role from Auth0. Falls back to CITIZEN if none matches. */
    private UserRole resolveRole(String sub) {
        try {
            List<String> auth0Roles = auth0ManagementFacade.getUserRoles(sub);
            return auth0Roles.stream()
                    .map(name -> {
                        try {
                            return UserRole.fromValue(name);
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(UserRole.MODEL_CITY_CITIZEN);
        } catch (Exception e) {
            log.warn("Could not fetch Auth0 roles for sub={}, defaulting to CITIZEN. Reason: {}", sub, e.getMessage());
            return UserRole.MODEL_CITY_CITIZEN;
        }
    }
}
