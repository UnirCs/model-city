package com.modelcity.core.users.usecase.mail;

/** Holds the variable data needed to render an invitation email template. */
public record InvitationMailContext(
        String cityName,
        String roleLabel,
        String ticketUrl,
        String address
) {}
