package com.modelcity.leisure.cityplaces.store;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Component;
import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.common.i18n.SupportedLocale;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceRequestDto;
import com.modelcity.leisure.cityplaces.store.CityPlaceStore;
import com.modelcity.leisure.cityplaces.store.model.CityPlaceView;
import com.modelcity.leisure.cityplaces.repository.CityPlaceRepository;
import com.modelcity.leisure.cityplaces.repository.model.CityPlace;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** JPA adapter for the city place persistence port. */
@RequiredArgsConstructor
@Component
@ModelCityDisabledIfInherited
public class DefaultCityPlaceStore implements CityPlaceStore<CityPlace, CityPlaceRequestDto> {

    private final CityPlaceRepository<CityPlace> cityPlaceRepository;

    @Override
    public Page<CityPlace> findAll(Pageable pageable) {
        return cityPlaceRepository.findAll(pageable);
    }

    @Override
    public Page<CityPlace> findByCategory(String category, Pageable pageable) {
        return cityPlaceRepository.findByCategoryIgnoreCase(category, pageable);
    }

    @Override
    public Optional<CityPlace> findById(Long id) {
        return cityPlaceRepository.findById(id);
    }

    @Override
    public CityPlace create(CityPlaceRequestDto request) {
        CityPlace place = new CityPlace();
        applyFields(place, request);
        return cityPlaceRepository.save(place);
    }

    @Override
    public CityPlace update(Long id, CityPlaceRequestDto request) {
        CityPlace place = cityPlaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CityPlace", id));
        applyFields(place, request);
        return cityPlaceRepository.save(place);
    }

    @Override
    public boolean existsById(Long id) {
        return cityPlaceRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        cityPlaceRepository.deleteById(id);
    }

    private void applyFields(CityPlace place, CityPlaceRequestDto request) {
        place.setName(LocalizedText.requireDefault("name", request.getName()));
        place.setLatitude(request.getLatitude());
        place.setLongitude(request.getLongitude());
        place.setDescription(LocalizedText.requireDefault("description", request.getDescription()));
        place.setAddress(defaultValue(request.getAddress()));
        place.setAccessInfo(defaultValue(request.getAccessInfo()));
        place.setAccessibilityInfo(defaultValue(request.getAccessibilityInfo()));
        place.setCategory(request.getCategory());
        place.setVisitDurationMinutes(request.getVisitDurationMinutes());
        applyPhotos(place, request.getPhotoUrls());
        applyTranslations(place, request);
    }

    private void applyTranslations(CityPlace place, CityPlaceRequestDto request) {
        Map<String, String> name = LocalizedText.nonDefault(request.getName());
        Map<String, String> description = LocalizedText.nonDefault(request.getDescription());
        Map<String, String> address = LocalizedText.nonDefault(request.getAddress());
        Map<String, String> accessInfo = LocalizedText.nonDefault(request.getAccessInfo());
        Map<String, String> accessibilityInfo = LocalizedText.nonDefault(request.getAccessibilityInfo());

        Set<String> locales = new HashSet<>();
        locales.addAll(name.keySet());
        locales.addAll(description.keySet());
        locales.addAll(address.keySet());
        locales.addAll(accessInfo.keySet());
        locales.addAll(accessibilityInfo.keySet());

        Map<String, CityPlace.CityPlaceI18n> translations = new HashMap<>();
        for (String locale : locales) {
            translations.put(locale, new CityPlace.CityPlaceI18n(
                    name.get(locale), description.get(locale), address.get(locale),
                    accessInfo.get(locale), accessibilityInfo.get(locale)));
        }
        place.getTranslations().clear();
        place.getTranslations().putAll(translations);
    }

    private static String defaultValue(Map<String, String> values) {
        return values == null ? null : values.get(SupportedLocale.DEFAULT.code());
    }

    private void applyPhotos(CityPlace place, List<String> photos) {
        place.setPhotoUrl1(null);
        place.setPhotoUrl2(null);
        place.setPhotoUrl3(null);
        if (photos == null) return;
        if (photos.size() > 0) place.setPhotoUrl1(photos.get(0));
        if (photos.size() > 1) place.setPhotoUrl2(photos.get(1));
        if (photos.size() > 2) place.setPhotoUrl3(photos.get(2));
    }
}
