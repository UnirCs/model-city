package com.modelcity.core.users.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.core.users.controller.model.UserSummaryDto;
import com.modelcity.core.users.repository.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Returns a filtered, paginated list of users. Authorization is enforced by {@code @ModelCityAccess.PlatformAdmin}.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultListUsersUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface ListUsersUseCase<T extends UserSummaryDto> {

    Page<T> execute(String requestingSub, Boolean citizen, String name,
                    Long neighbourhoodId, UserRole role, Pageable pageable);
}
