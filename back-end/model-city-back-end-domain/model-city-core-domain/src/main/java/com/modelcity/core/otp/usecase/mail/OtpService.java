package com.modelcity.core.otp.usecase.mail;

import com.modelcity.core.utils.mail.MailFacade;
import com.modelcity.core.utils.mail.MailTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/** Handles OTP generation, hashing and email delivery. */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final MailFacade mailFacade;
    private final MailTemplate<OtpMailContext> otpMailTemplate;

    @Value("${mail.commons.city}")
    private String cityName;

    @Value("${mail.commons.address}")
    private String address;

    /** Generates an OTP, sends it to the given email and returns its SHA-256 hash. */
    public String generateOtpHash(String email) {
        String otp = generate();
        send(email, otp);
        return hash(otp);
    }

    /** Returns true if the given OTP matches the stored SHA-256 hash. */
    public boolean validateOtpHash(String inputOtp, String storedHash) {
        return hash(inputOtp).equals(storedHash);
    }

    private String hash(String otp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(otp.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String generate() {
        int code = new SecureRandom().nextInt(900_000) + 100_000;
        return String.valueOf(code);
    }

    private void send(String email, String otp) {
        OtpMailContext ctx = new OtpMailContext(cityName, otp, address);
        String html = otpMailTemplate.render(ctx);
        mailFacade.sendHtml(email, cityName + " — Tu código de verificación", html);
    }
}

