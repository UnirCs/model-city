package com.modelcity.mobility.sanctions.controller.model;

import com.modelcity.mobility.sanctions.store.model.SanctionView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Full sanction representation including the evidence image.
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a local deployment may subclass it to add
 * city-specific fields and have its overridden use cases / controllers work with the subtype through the
 * generic seams (e.g. {@code GetSanctionUseCase<T extends SanctionDto>}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanctionDto {

    private Long id;
    private String licensePlate;
    private Double latitude;
    private Double longitude;
    private String agentSub;
    private OffsetDateTime createdAt;
    private String imageBase64;

    public static SanctionDto from(SanctionView s) {
        return new SanctionDto(s.getId(), s.getLicensePlate(), s.getLatitude(), s.getLongitude(),
                s.getAgentSub(), s.getCreatedAt(), s.getImageBase64());
    }
}
