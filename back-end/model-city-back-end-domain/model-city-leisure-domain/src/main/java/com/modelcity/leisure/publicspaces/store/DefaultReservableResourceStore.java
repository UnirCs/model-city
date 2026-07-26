package com.modelcity.leisure.publicspaces.store;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Component;
import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.common.i18n.SupportedLocale;
import com.modelcity.leisure.publicspaces.controller.model.ReservableResourceRequestDto;
import com.modelcity.leisure.publicspaces.model.ReservableResourceView;
import com.modelcity.leisure.publicspaces.repository.ReservableResourceRepository;
import com.modelcity.leisure.publicspaces.repository.model.ReservableResource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** JPA adapter for the reservable resource persistence port. */
@RequiredArgsConstructor
@Component
@ModelCityDisabledIfInherited
public class DefaultReservableResourceStore implements ReservableResourceStore<ReservableResource, ReservableResourceRequestDto> {

    private final ReservableResourceRepository<ReservableResource> reservableResourceRepository;

    @Override
    public Optional<ReservableResource> findActiveByIdAndPublicSpace(Long id, Long publicSpaceId) {
        return reservableResourceRepository.findByIdAndPublicSpaceIdAndActiveTrue(id, publicSpaceId);
    }

    @Override
    public Page<ReservableResource> findActiveByPublicSpace(Long publicSpaceId, Pageable pageable) {
        return reservableResourceRepository.findByPublicSpaceIdAndActiveTrueOrderByIdAsc(publicSpaceId, pageable);
    }

    @Override
    public ReservableResource create(Long publicSpaceId, ReservableResourceRequestDto request) {
        ReservableResource resource = ReservableResource.builder()
                .publicSpaceId(publicSpaceId)
                .name(LocalizedText.requireDefault("name", request.getName()))
                .description(defaultValue(request.getDescription()))
                .resourceType(request.getResourceType())
                .active(true)
                .build();
        applyTranslations(resource, request);
        return reservableResourceRepository.save(resource);
    }

    @Override
    public ReservableResource update(Long id, Long publicSpaceId, ReservableResourceRequestDto request) {
        ReservableResource resource = reservableResourceRepository
                .findByIdAndPublicSpaceIdAndActiveTrue(id, publicSpaceId)
                .orElseThrow(() -> new ResourceNotFoundException("ReservableResource", id));
        resource.setName(LocalizedText.requireDefault("name", request.getName()));
        resource.setDescription(defaultValue(request.getDescription()));
        resource.setResourceType(request.getResourceType());
        applyTranslations(resource, request);
        return reservableResourceRepository.save(resource);
    }

    private void applyTranslations(ReservableResource resource, ReservableResourceRequestDto request) {
        Map<String, String> name = LocalizedText.nonDefault(request.getName());
        Map<String, String> description = LocalizedText.nonDefault(request.getDescription());

        Set<String> locales = new HashSet<>();
        locales.addAll(name.keySet());
        locales.addAll(description.keySet());

        Map<String, ReservableResource.ReservableResourceI18n> translations = new HashMap<>();
        for (String locale : locales) {
            translations.put(locale, new ReservableResource.ReservableResourceI18n(
                    name.get(locale), description.get(locale)));
        }
        resource.getTranslations().clear();
        resource.getTranslations().putAll(translations);
    }

    private static String defaultValue(Map<String, String> values) {
        return values == null ? null : values.get(SupportedLocale.DEFAULT.code());
    }

    @Override
    public void softDelete(Long id, Long publicSpaceId) {
        ReservableResource resource = reservableResourceRepository
                .findByIdAndPublicSpaceIdAndActiveTrue(id, publicSpaceId)
                .orElseThrow(() -> new ResourceNotFoundException("ReservableResource", id));
        resource.setActive(false);
        reservableResourceRepository.save(resource);
    }

    @Override
    public void softDeleteByPublicSpace(Long publicSpaceId) {
        for (ReservableResource r : reservableResourceRepository.findByPublicSpaceIdAndActiveTrueOrderByIdAsc(publicSpaceId)) {
            r.setActive(false);
            reservableResourceRepository.save(r);
        }
    }
}
