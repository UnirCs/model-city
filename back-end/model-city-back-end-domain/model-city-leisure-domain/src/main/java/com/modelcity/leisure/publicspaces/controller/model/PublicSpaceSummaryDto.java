package com.modelcity.leisure.publicspaces.controller.model;

import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.leisure.publicspaces.model.PublicSpaceView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lightweight projection for paginated listings, resolved to the requested locale.
 *
 * <p>Extensible DTO (plain class, not a {@code record}) so a local deployment may subclass it and bind the
 * subtype through the generic seams (e.g. {@code GetPublicSpacesUseCase<S extends PublicSpaceSummaryDto>}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublicSpaceSummaryDto {

    private Long id;
    private String name;
    private String address;
    private String photoUrl;

    public static PublicSpaceSummaryDto from(PublicSpaceView s, String locale) {
        PublicSpaceView.Translation t = s.getTranslations().get(locale);
        return new PublicSpaceSummaryDto(
                s.getId(),
                LocalizedText.resolve(s.getName(), t == null ? null : t.getName()),
                LocalizedText.resolve(s.getAddress(), t == null ? null : t.getAddress()),
                s.getPhotoUrl1());
    }
}
