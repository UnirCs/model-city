package com.modelcity.leisure.events.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;
import com.modelcity.leisure.events.controller.model.EventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link GetEventForEditUseCase} implementation. The component-scanned platform default; disabled at startup when a local deployment provides its own bean for the seam. */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultGetEventForEditUseCase implements GetEventForEditUseCase<EventDto> {

    private final CachedEventReader cachedEventReader;

    @Override
    @Transactional(readOnly = true)
    public EventDto execute(Long id, String locale) {
        return cachedEventReader.getForEdit(id, locale);
    }
}
