package com.modelcity.core.users.usecase;

import com.modelcity.core.users.controller.model.UserSummaryDto;
import com.modelcity.core.users.repository.UserRepository;
import com.modelcity.core.users.repository.UserSpecs;
import com.modelcity.core.users.repository.model.Neighbourhood;
import com.modelcity.core.users.repository.model.User;
import com.modelcity.core.users.repository.model.UserRole;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.modelcity.common.config.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link ListUsersUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultListUsersUseCase implements ListUsersUseCase<UserSummaryDto> {

    private final UserRepository userRepository;

    @Override
    @Cacheable(cacheNames = CacheNames.USER_LIST,
            key = "(#citizen == null ? 'all' : #citizen.toString()) + ':' + (#name == null ? '' : #name) + ':' "
                    + "+ (#neighbourhoodId == null ? '' : #neighbourhoodId) + ':' + (#role == null ? '' : #role.name()) + ':' "
                    + "+ #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<UserSummaryDto> execute(String requestingSub, Boolean citizen, String name,
                                        Long neighbourhoodId, UserRole role, Pageable pageable) {
        Specification<User> spec = UserSpecs.and(
                UserSpecs.and(UserSpecs.citizen(citizen), UserSpecs.roleEquals(role)),
                UserSpecs.and(UserSpecs.nameContains(name), UserSpecs.neighbourhoodIdEquals(neighbourhoodId)));

        Page<User> users = (spec == null) ? userRepository.findAll(pageable) : userRepository.findAll(spec, pageable);
        return users.map(this::toSummary);
    }

    private UserSummaryDto toSummary(User u) {
        Neighbourhood n = u.getNeighbourhood();
        return new UserSummaryDto(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getStatus(),
                n == null ? null : n.getId(), n == null ? null : n.getName(), u.getCreatedAt());
    }
}
