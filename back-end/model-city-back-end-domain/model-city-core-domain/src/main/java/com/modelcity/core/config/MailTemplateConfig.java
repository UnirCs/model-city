package com.modelcity.core.config;

import com.modelcity.core.otp.usecase.mail.ClasspathHtmlOtpMailTemplate;
import com.modelcity.core.otp.usecase.mail.OtpMailContext;
import com.modelcity.core.users.usecase.mail.ClasspathHtmlInvitationMailTemplate;
import com.modelcity.core.users.usecase.mail.InvitationMailContext;
import com.modelcity.core.utils.mail.MailTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/** Wires the OTP and invitation mail templates from their configured classpath resources. */
@Configuration
public class MailTemplateConfig {

    @Value("${mail.otp.template.resource}")
    private String otpTemplateResource;

    @Value("${mail.invitation.template.resource}")
    private String invitationTemplateResource;

    @Bean
    public MailTemplate<OtpMailContext> otpMailTemplate() {
        return new ClasspathHtmlOtpMailTemplate(new ClassPathResource(otpTemplateResource));
    }

    @Bean
    public MailTemplate<InvitationMailContext> invitationMailTemplate() {
        return new ClasspathHtmlInvitationMailTemplate(new ClassPathResource(invitationTemplateResource));
    }
}
