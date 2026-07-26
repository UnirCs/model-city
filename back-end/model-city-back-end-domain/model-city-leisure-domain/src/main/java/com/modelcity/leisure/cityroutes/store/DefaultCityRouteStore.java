package com.modelcity.leisure.cityroutes.store;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Component;
import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteRequestDto;
import com.modelcity.leisure.cityroutes.store.CityRouteStore;
import com.modelcity.leisure.cityroutes.store.model.CityRouteView;
import com.modelcity.leisure.cityplaces.repository.CityPlaceRepository;
import com.modelcity.leisure.cityroutes.repository.CityRouteRepository;
import com.modelcity.leisure.cityplaces.repository.model.CityPlace;
import com.modelcity.leisure.cityroutes.repository.model.CityRoute;
import com.modelcity.leisure.cityroutes.repository.model.CityRoutePlace;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** JPA adapter for the city route persistence port. */
@RequiredArgsConstructor
@Component
@ModelCityDisabledIfInherited
public class DefaultCityRouteStore implements CityRouteStore<CityRoute, CityRouteRequestDto> {

    private final CityRouteRepository<CityRoute> cityRouteRepository;
    private final CityPlaceRepository<CityPlace> cityPlaceRepository;

    @Override
    public Page<CityRoute> findAll(Pageable pageable) {
        return cityRouteRepository.findAll(pageable);
    }

    @Override
    public Optional<CityRoute> findById(Long id) {
        return cityRouteRepository.findById(id);
    }

    @Override
    public CityRoute create(CityRouteRequestDto request) {
        CityRoute route = CityRoute.builder()
                .routePlaces(new ArrayList<>())
                .build();
        applyScalarFields(route, request);
        applyPlaces(route, request);
        return cityRouteRepository.save(route);
    }

    @Override
    public CityRoute update(Long id, CityRouteRequestDto request) {
        CityRoute route = cityRouteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CityRoute", id));
        applyScalarFields(route, request);
        // Flush the clear so Postgres processes DELETEs before the upcoming INSERTs,
        // avoiding the unique constraint violation on (route_id, place_id).
        route.getRoutePlaces().clear();
        cityRouteRepository.saveAndFlush(route);
        applyPlaces(route, request);
        return cityRouteRepository.save(route);
    }

    @Override
    public boolean existsById(Long id) {
        return cityRouteRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        cityRouteRepository.deleteById(id);
    }

    private void applyScalarFields(CityRoute route, CityRouteRequestDto request) {
        route.setName(LocalizedText.requireDefault("name", request.getName()));
        route.setDescription(LocalizedText.requireDefault("description", request.getDescription()));
        route.setTargetAudience(request.getTargetAudience());
        route.setImageUrl(request.getImageUrl());
        route.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        applyTranslations(route, request);
    }

    private void applyTranslations(CityRoute route, CityRouteRequestDto request) {
        Map<String, String> name = LocalizedText.nonDefault(request.getName());
        Map<String, String> description = LocalizedText.nonDefault(request.getDescription());

        Set<String> locales = new HashSet<>();
        locales.addAll(name.keySet());
        locales.addAll(description.keySet());

        Map<String, CityRoute.CityRouteI18n> translations = new HashMap<>();
        for (String locale : locales) {
            translations.put(locale, new CityRoute.CityRouteI18n(name.get(locale), description.get(locale)));
        }
        route.getTranslations().clear();
        route.getTranslations().putAll(translations);
    }

    private void applyPlaces(CityRoute route, CityRouteRequestDto request) {
        route.getRoutePlaces().clear();
        List<Long> ids = request.getCityPlaceIds();
        if (ids == null || ids.isEmpty()) return;

        List<CityPlace> found = cityPlaceRepository.findAllById(ids);
        if (found.size() != ids.stream().distinct().count()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more cityPlaceIds do not exist");
        }
        var byId = new HashMap<Long, CityPlace>();
        for (CityPlace p : found) byId.put(p.getId(), p);

        List<CityRoutePlace> ordered = new ArrayList<>();
        int order = 0;
        for (Long id : ids) {
            CityPlace place = byId.get(id);
            if (place == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "City place not found id=" + id);
            }
            ordered.add(CityRoutePlace.builder()
                    .route(route)
                    .place(place)
                    .sortOrder(order++)
                    .build());
        }
        route.getRoutePlaces().addAll(ordered);
    }
}
