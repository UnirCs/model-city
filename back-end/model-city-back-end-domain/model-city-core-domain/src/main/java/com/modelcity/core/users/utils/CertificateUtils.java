package com.modelcity.core.users.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriUtils;

import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class CertificateUtils {

    private CertificateUtils() {}

    public static X509Certificate parseUrlEncodedPemCertificate(String escapedPem) {
        try {
            String pem = UriUtils.decode(escapedPem.trim(), StandardCharsets.UTF_8)
                    .replace("\r\n", "\n")
                    .replace('\r', '\n');
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.error("Failed to parse certificate. escapedPem value: {}", escapedPem, e);
            throw new IllegalArgumentException("No se pudo parsear el certificado cliente", e);
        }
    }

    public static Map<String, String> parseDistinguishedName(String subject) {
        Map<String, String> result = new LinkedHashMap<>();
        if (subject == null || subject.isBlank()) {
            return result;
        }
        String[] parts = subject.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String part : parts) {
            String[] kv = part.trim().split("=", 2);
            if (kv.length == 2) {
                result.put(
                        kv[0].trim().toUpperCase(),
                        kv[1].trim().replace("\"", "")
                );
            }
        }
        return result;
    }

    public static String extractSpanishDocumentNumber(String subject, Map<String, String> dn) {
        String serialNumberAttribute = dn.get("SERIALNUMBER");
        String fromSerialNumber = findDniLikeValue(serialNumberAttribute);
        if (fromSerialNumber != null) {
            return fromSerialNumber;
        }
        return findDniLikeValue(subject);
    }

    public static String findDniLikeValue(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        Pattern pattern = Pattern.compile("([0-9]{8}[A-Z])", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
        }
        return null;
    }

}
