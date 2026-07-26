package com.modelcity.core.otp.usecase.mail;

import com.modelcity.core.utils.mail.ClasspathHtmlMailTemplate;
import org.springframework.core.io.Resource;

import java.util.Map;

/** Loads an HTML file from the classpath and replaces {{PLACEHOLDER}} tokens. */
public class ClasspathHtmlOtpMailTemplate extends ClasspathHtmlMailTemplate<OtpMailContext> {

    public ClasspathHtmlOtpMailTemplate(Resource resource) {
        super(resource, ctx -> Map.of(
                "CITY_NAME", ctx.cityName(),
                "OTP_CODE", formatOtp(ctx.otp()),
                "ADDRESS", ctx.address()
        ));
    }

    /** Splits a 6-digit OTP into "XXX XXX" for readability. */
    private static String formatOtp(String otp) {
        if (otp != null && otp.length() == 6) {
            return otp.substring(0, 3) + " " + otp.substring(3);
        }
        return otp;
    }
}

