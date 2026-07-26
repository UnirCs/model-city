package com.modelcity.common.security;

/**
 * Shared security constants used by the gateway, the monolith and the domain libraries.
 * Centralising them avoids drift between the OAuth2 Resource Server and the consumers of the
 * propagated identity header.
 */
public final class AuthConstants {

    private AuthConstants() {}

    /** Header that carries the Auth0 {@code sub} claim downstream after the JWT has been validated. */
    public static final String HEADER_AUTH_SUB = "X-Auth-Sub";
}
