package com.modelcity.leisure.publicspaces.store;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Component;
import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.common.i18n.SupportedLocale;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceRequestDto;
import com.modelcity.leisure.publicspaces.model.PublicSpaceView;
import com.modelcity.leisure.publicspaces.repository.PublicSpaceRepository;
import com.modelcity.leisure.publicspaces.repository.model.PublicSpace;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** JPA adapter for the public space persistence port. */
@RequiredArgsConstructor
@Component
@ModelCityDisabledIfInherited
public class DefaultPublicSpaceStore implements PublicSpaceStore<PublicSpace, PublicSpaceRequestDto> {

    private final PublicSpaceRepository<PublicSpace> publicSpaceRepository;

    @Override
    public Page<PublicSpace> findActive(Pageable pageable) {
        return publicSpaceRepository.findByActiveTrue(pageable);
    }

    @Override
    public Optional<PublicSpace> findActiveById(Long id) {
        return publicSpaceRepository.findByIdAndActiveTrue(id);
    }

    @Override
    public PublicSpace create(PublicSpaceRequestDto request) {
        PublicSpace space = new PublicSpace();
        space.setActive(true);
        applyFields(space, request);
        return publicSpaceRepository.save(space);
    }

    @Override
    public PublicSpace update(Long id, PublicSpaceRequestDto request) {
        PublicSpace space = publicSpaceRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("PublicSpace", id));
        applyFields(space, request);
        return publicSpaceRepository.save(space);
    }

    @Override
    public void softDelete(Long id) {
        PublicSpace space = publicSpaceRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("PublicSpace", id));
        space.setActive(false);
        publicSpaceRepository.save(space);
    }

    private void applyFields(PublicSpace space, PublicSpaceRequestDto request) {
        space.setName(LocalizedText.requireDefault("name", request.getName()));
        space.setDescription(LocalizedText.requireDefault("description", request.getDescription()));
        space.setAddress(defaultValue(request.getAddress()));
        space.setLatitude(request.getLatitude());
        space.setLongitude(request.getLongitude());
        applyPhotos(space, request.getPhotoUrls());
        applyTranslations(space, request);
    }

    private void applyTranslations(PublicSpace space, PublicSpaceRequestDto request) {
        Map<String, String> name = LocalizedText.nonDefault(request.getName());
        Map<String, String> description = LocalizedText.nonDefault(request.getDescription());
        Map<String, String> address = LocalizedText.nonDefault(request.getAddress());

        Set<String> locales = new HashSet<>();
        locales.addAll(name.keySet());
        locales.addAll(description.keySet());
        locales.addAll(address.keySet());

        Map<String, PublicSpace.PublicSpaceI18n> translations = new HashMap<>();
        for (String locale : locales) {
            translations.put(locale, new PublicSpace.PublicSpaceI18n(
                    name.get(locale), description.get(locale), address.get(locale)));
        }
        space.getTranslations().clear();
        space.getTranslations().putAll(translations);
    }

    private static String defaultValue(Map<String, String> values) {
        return values == null ? null : values.get(SupportedLocale.DEFAULT.code());
    }

    private void applyPhotos(PublicSpace space, List<String> photos) {
        space.setPhotoUrl1(null);
        space.setPhotoUrl2(null);
        space.setPhotoUrl3(null);
        if (photos == null) return;
        if (photos.size() > 0) space.setPhotoUrl1(photos.get(0));
        if (photos.size() > 1) space.setPhotoUrl2(photos.get(1));
        if (photos.size() > 2) space.setPhotoUrl3(photos.get(2));
    }
}
