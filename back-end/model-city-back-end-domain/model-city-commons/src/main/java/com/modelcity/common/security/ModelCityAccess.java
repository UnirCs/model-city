package com.modelcity.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container of access annotations to restrict controller methods to specific Model City roles.
 * Combine annotations on the same method to allow any of them (logical OR).
 *
 * Validation is enforced by {@code ModelCityAccessAspect} which fetches the caller role
 * from the core microservice using the {@code X-Auth-Sub} request header.
 */
public final class ModelCityAccess {

    private ModelCityAccess() {}

    /** Only users with role MODEL-CITY-PLATFORM-ADMIN can invoke the method. */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PlatformAdmin {}

    /** Only users with role MODEL-CITY-BACKOFFICE can invoke the method. */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface BackOffice {}

    /** Only users with role MODEL-CITY-OPERATOR can invoke the method. */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Operator {}

    /** Only users with role MODEL-CITY-MOBILITY-AGENT can invoke the method. */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MobilityAgent {}
}

