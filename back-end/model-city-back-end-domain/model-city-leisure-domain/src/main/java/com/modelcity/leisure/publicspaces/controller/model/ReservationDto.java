package com.modelcity.leisure.publicspaces.controller.model;

import com.modelcity.leisure.publicspaces.model.SpaceReservationView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Public-facing representation of a reservation.
 * Citizen identification fields are only populated for admin/operator callers.
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a local deployment may subclass it and bind the
 * subtype through the generic seams (e.g. {@code GetReservationsUseCase<T extends ReservationDto>}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDto {

    private Long id;
    private Long resourceId;
    private LocalDate reservationDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String citizenSub;
    private String citizenName;

    /** Public view (no citizen info). */
    public static ReservationDto publicView(SpaceReservationView r) {
        return new ReservationDto(r.getId(), r.getResourceId(), r.getReservationDate(),
                r.getStartTime(), r.getEndTime(), null, null);
    }

    /** Privileged view including the citizen that booked. */
    public static ReservationDto privilegedView(SpaceReservationView r) {
        return new ReservationDto(r.getId(), r.getResourceId(), r.getReservationDate(),
                r.getStartTime(), r.getEndTime(), r.getCitizenSub(), r.getCitizenName());
    }
}
