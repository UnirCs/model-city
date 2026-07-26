package com.modelcity.leisure.events.controller.model;

import com.modelcity.leisure.events.repository.model.EventType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Request body for creating or replacing an event (backoffice / admin). The localizable {@code name}
 * and {@code description} are multi-locale maps ({@code locale -> text}) with a mandatory {@code es} entry.
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a city may subclass it to accept extra input fields
 * and have its overridden write use cases / controllers bind the subtype through the generic seams
 * (e.g. {@code CreateEventUseCase<T extends EventDto, R extends EventRequestDto>}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestDto {

    @NotNull
    private Long placeId;
    @NotEmpty
    private Map<String, String> name;
    @NotEmpty
    private Map<String, String> description;
    @NotNull
    private EventType eventType;
    private boolean requiresTicket;
    private boolean paid;
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal price;
    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;
    @Positive
    private Integer capacity;
    @NotNull
    private LocalDateTime startsAt;
    @NotNull
    private LocalDateTime endsAt;
    @Size(max = 3)
    private List<String> photoUrls;
}
