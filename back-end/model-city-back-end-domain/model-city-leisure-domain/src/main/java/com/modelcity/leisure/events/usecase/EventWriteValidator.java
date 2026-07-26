package com.modelcity.leisure.events.usecase;
import com.modelcity.leisure.cityplaces.store.model.CityPlaceView;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceRequestDto;

import com.modelcity.leisure.events.controller.model.EventRequestDto;
import com.modelcity.leisure.cityplaces.store.CityPlaceStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

/** Shared validation for event write operations (consistency + referenced city place existence). */
@Component
@RequiredArgsConstructor
public class EventWriteValidator {

    private final CityPlaceStore<? extends CityPlaceView, CityPlaceRequestDto> cityPlaceStore;

    void validate(EventRequestDto r) {
        if (!r.getEndsAt().isAfter(r.getStartsAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endsAt must be after startsAt");
        }
        if (r.isPaid() && !r.isRequiresTicket()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paid events must require a ticket");
        }
        boolean priceIsZero = r.getPrice() == null || r.getPrice().compareTo(BigDecimal.ZERO) == 0;
        if (r.isPaid() && priceIsZero) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paid events must have a price greater than zero");
        }
        if (!r.isPaid() && !priceIsZero) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Free events must have a zero price");
        }
        if (!cityPlaceStore.existsById(r.getPlaceId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Referenced city place does not exist: " + r.getPlaceId());
        }
    }
}
