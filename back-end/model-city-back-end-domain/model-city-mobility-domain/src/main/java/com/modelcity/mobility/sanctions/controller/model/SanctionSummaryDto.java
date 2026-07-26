package com.modelcity.mobility.sanctions.controller.model;

import com.modelcity.mobility.sanctions.store.model.SanctionView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Sanction representation without the evidence image.
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a local deployment may subclass it to add
 * city-specific fields and have its overridden use cases / controllers work with the subtype through the
 * generic seams (e.g. {@code GetSanctionsUseCase<T extends SanctionSummaryDto>}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanctionSummaryDto {

    private Long id;
    private String licensePlate;
    private Double latitude;
    private Double longitude;
    private String agentSub;
    private OffsetDateTime createdAt;

    public static SanctionSummaryDto from(SanctionView s) {
        return new SanctionSummaryDto(s.getId(), s.getLicensePlate(), s.getLatitude(),
                s.getLongitude(), s.getAgentSub(), s.getCreatedAt());
    }
}
