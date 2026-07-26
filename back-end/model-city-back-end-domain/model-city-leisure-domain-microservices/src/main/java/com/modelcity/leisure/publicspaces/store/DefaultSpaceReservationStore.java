package com.modelcity.leisure.publicspaces.store;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Component;
import com.modelcity.leisure.publicspaces.model.SpaceReservationView;
import com.modelcity.leisure.publicspaces.repository.SpaceReservationRepository;
import com.modelcity.leisure.publicspaces.repository.model.SpaceReservation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/** JPA adapter for the space reservation persistence port. */
@RequiredArgsConstructor
@Component
@ModelCityDisabledIfInherited
public class DefaultSpaceReservationStore implements SpaceReservationStore<SpaceReservation> {

    private final SpaceReservationRepository<SpaceReservation> spaceReservationRepository;

    @Override
    public List<SpaceReservation> findByResourceAndDate(Long resourceId, LocalDate date) {
        return spaceReservationRepository.findByResourceIdAndReservationDateOrderByStartTimeAsc(resourceId, date);
    }

    @Override
    public Page<SpaceReservation> findByResourceAndDate(Long resourceId, LocalDate date, Pageable pageable) {
        return spaceReservationRepository.findByResourceIdAndReservationDateOrderByStartTimeAsc(resourceId, date, pageable);
    }

    @Override
    public Optional<SpaceReservation> findById(Long id) {
        return spaceReservationRepository.findById(id);
    }

    @Override
    public SpaceReservation create(Long resourceId, String citizenSub, String citizenName,
                                       LocalDate reservationDate, LocalTime startTime, LocalTime endTime) {
        SpaceReservation reservation = SpaceReservation.builder()
                .resourceId(resourceId)
                .citizenSub(citizenSub)
                .citizenName(citizenName)
                .reservationDate(reservationDate)
                .startTime(startTime)
                .endTime(endTime)
                .build();
        return spaceReservationRepository.save(reservation);
    }

    @Override
    public void delete(Long id) {
        spaceReservationRepository.deleteById(id);
    }
}
