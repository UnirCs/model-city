package com.modelcity.core.users.usecase.mail;

import com.modelcity.core.utils.mail.MailFacade;
import com.modelcity.core.utils.mail.MailTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Sends invitation emails to newly created staff users. */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationMailService {

    private final MailFacade mailFacade;
    private final MailTemplate<InvitationMailContext> invitationMailTemplate;

    @Value("${mail.commons.city}")
    private String cityName;

    @Value("${mail.commons.address}")
    private String address;

    public void sendInvitation(String email, String roleLabel, String ticketUrl) {
        InvitationMailContext ctx = new InvitationMailContext(cityName, roleLabel, ticketUrl, address);
        String html = invitationMailTemplate.render(ctx);
        mailFacade.sendHtml(email, cityName + " — Invitación para unirte al equipo", html);
        log.debug("Invitation email sent to {}", email);
    }
}

