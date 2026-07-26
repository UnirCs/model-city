package com.modelcity.core.users.usecase;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.core.users.controller.model.NeighbourhoodSummaryDto;
import com.modelcity.core.users.controller.model.UserProfileDto;
import com.modelcity.core.users.repository.UserRepository;
import com.modelcity.core.users.repository.model.Neighbourhood;
import com.modelcity.core.users.repository.model.User;
import com.modelcity.core.users.repository.model.UserRole;
import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Default {@link GetUserUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetUserUseCase implements GetUserUseCase<UserProfileDto> {

    private final UserRepository userRepository;

    @Override
    @Cacheable(cacheNames = CacheNames.USER_PROFILE, key = "#locale + '-' + #targetUserId")
    public UserProfileDto execute(String requestingSub, String targetUserId, String locale) {
        if (!requestingSub.equals(targetUserId)) {
            User requester = userRepository.findById(requestingSub)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Requester not found"));
            if (requester.getRole() != UserRole.MODEL_CITY_PLATFORM_ADMIN) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access to other users' profiles requires admin role");
            }
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));

        NeighbourhoodSummaryDto neighbourhoodDto = null;
        if (user.getNeighbourhood() != null) {
            Neighbourhood n = user.getNeighbourhood();
            String displayName = LocalizedText.resolve(n.getDisplayName(), n.getDisplayNameTranslations().get(locale));
            neighbourhoodDto = new NeighbourhoodSummaryDto(n.getId(), displayName, n.getName());
        }
        return new UserProfileDto(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt(), neighbourhoodDto, user.getRole(), user.getStatus());
    }
}
