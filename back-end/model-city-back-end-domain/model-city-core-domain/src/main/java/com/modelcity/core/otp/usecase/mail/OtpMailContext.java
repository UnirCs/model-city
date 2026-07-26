package com.modelcity.core.otp.usecase.mail;

/** Holds the variable data needed to render any OTP email template. */
public record OtpMailContext(
        String cityName,
        String otp,
        String address
) {}

