package com.modelcity.common.extensibility;

import java.util.List;

/**
 * City-name tokens that must never appear in platform code (class or package names) of the {@code *-domain}
 * libraries. The defaults and seams are city-neutral; a deployment's customizations live in its own
 * deployable, not here.
 *
 * <p>All tokens are lower-case and matched case-insensitively as substrings of the fully qualified type
 * name. Keep them specific enough not to collide with legitimate domain identifiers; extend the list as new
 * pilot cities appear.
 */
final class CityNames {

    static final List<String> TOKENS = List.of(
            "aranjuez",
            "madrid",
            "barcelona",
            "sevilla",
            "valencia",
            "zaragoza",
            "malaga",
            "bilbao",
            "granada",
            "cordoba",
            "valladolid",
            "alicante",
            "gijon"
    );

    private CityNames() {
    }
}
