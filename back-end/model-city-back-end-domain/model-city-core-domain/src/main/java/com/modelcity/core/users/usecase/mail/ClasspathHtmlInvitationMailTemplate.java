package com.modelcity.core.users.usecase.mail;

import com.modelcity.core.utils.mail.ClasspathHtmlMailTemplate;
import org.springframework.core.io.Resource;

import java.util.Map;

/** Loads an HTML file from the classpath and replaces {{PLACEHOLDER}} tokens for invitations. */
public class ClasspathHtmlInvitationMailTemplate extends ClasspathHtmlMailTemplate<InvitationMailContext> {

    public ClasspathHtmlInvitationMailTemplate(Resource resource) {
        super(resource, ctx -> Map.of(
                "CITY_NAME", ctx.cityName(),
                "ROLE_LABEL", ctx.roleLabel(),
                "TICKET_URL", ctx.ticketUrl(),
                "ADDRESS", ctx.address()
        ));
    }
}
