package com.modelcity.leisure.publicspaces.store;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;

import com.modelcity.leisure.publicspaces.model.SpaceReservationView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/** Persistence port for space reservations. */
@ModelCityExtensionPoint
public interface SpaceReservationStore<T extends SpaceReservationView> {

    /** All reservations for the resource on the date, ordered by start time (used for overlap checks). */
    List<T> findByResourceAndDate(Long resourceId, LocalDate date);

    Page<T> findByResourceAndDate(Long resourceId, LocalDate date, Pageable pageable);

    Optional<T> findById(Long id);

    T create(Long resourceId, String citizenSub, String citizenName,
                                LocalDate reservationDate, LocalTime startTime, LocalTime endTime);

    void delete(Long id);
}
