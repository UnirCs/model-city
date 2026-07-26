package com.modelcity.common.config.cache;

/**
 * Canonical cache names shared between the microservices and the monolith. Using these constants in
 * {@code @Cacheable}/{@code @CacheEvict} avoids typos and keeps the names aligned with the per-cache TTLs
 * declared in {@link ModelCityCacheAutoConfiguration}.
 */
public final class CacheNames {

    private CacheNames() {}

    // Leisure — master data, long TTL
    public static final String CITY_PLACE = "cityPlace";
    public static final String CITY_PLACES = "cityPlaces";
    public static final String CITY_ROUTE = "cityRoute";
    public static final String CITY_ROUTES = "cityRoutes";
    public static final String CITY_ROUTE_PLACES = "cityRoutePlaces";
    public static final String PUBLIC_SPACE = "publicSpace";
    public static final String PUBLIC_SPACES = "publicSpaces";
    public static final String RESERVABLE_RESOURCES = "reservableResources";

    // Leisure — events, short TTL
    public static final String EVENT = "event";
    public static final String EVENTS = "events";

    // Mobility — long TTL
    public static final String USER_CARS = "userCars";
    public static final String SANCTION = "sanction";
    public static final String USER_SANCTIONS = "userSanctions";

    // Citizen engagement
    public static final String PUBLIC_QUESTION = "publicQuestion";
    public static final String PUBLIC_QUESTIONS = "publicQuestions";
    public static final String SECURITY_ALERTS = "securityAlerts";

    // Core — users, medium TTL
    public static final String USER_PROFILE = "userProfile";
    public static final String USER_LIST = "userList";
    public static final String CITIZEN_EXISTS = "citizenExists";
}
