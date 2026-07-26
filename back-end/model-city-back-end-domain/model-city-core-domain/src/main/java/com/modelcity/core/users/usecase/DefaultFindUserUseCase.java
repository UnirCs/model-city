package com.modelcity.core.users.usecase;

import com.modelcity.core.users.repository.UserRepository;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link FindUserUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultFindUserUseCase implements FindUserUseCase {

    private final UserRepository userRepository;

    @Override
    @Cacheable(cacheNames = CacheNames.CITIZEN_EXISTS, key = "#sub")
    public boolean execute(String sub) {
        return userRepository.existsById(sub);
    }
}
