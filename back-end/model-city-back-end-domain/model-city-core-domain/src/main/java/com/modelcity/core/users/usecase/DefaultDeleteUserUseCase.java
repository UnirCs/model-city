package com.modelcity.core.users.usecase;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.core.trails.SystemTrailGenerator;
import com.modelcity.core.users.repository.UserRepository;
import com.modelcity.core.users.repository.model.User;
import com.modelcity.core.users.repository.model.UserRole;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Default {@link DeleteUserUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultDeleteUserUseCase implements DeleteUserUseCase {

    private final UserRepository userRepository;
    private final SystemTrailGenerator systemEventGenerator;

    @Override
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.USER_PROFILE, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.CITIZEN_EXISTS, key = "#targetUserId"),
        @CacheEvict(cacheNames = CacheNames.USER_LIST, allEntries = true)
    })
    public void execute(String requestingSub, String targetUserId) {

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));

        if (target.getRole() == UserRole.MODEL_CITY_PLATFORM_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin users cannot be deleted");
        }

        userRepository.delete(target);
        systemEventGenerator.userDeleted(requestingSub, target);
        log.info("User id={} deleted by admin sub={}", targetUserId, requestingSub);
    }
}
