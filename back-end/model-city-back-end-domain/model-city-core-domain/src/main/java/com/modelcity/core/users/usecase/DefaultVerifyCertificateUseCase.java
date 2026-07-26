package com.modelcity.core.users.usecase;

import com.modelcity.core.security.CertificateVerificationTokenService;
import com.modelcity.core.security.DniHasher;
import com.modelcity.core.users.repository.UserRepository;
import com.modelcity.core.users.repository.model.User;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Default {@link VerifyCertificateUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@Slf4j
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultVerifyCertificateUseCase implements VerifyCertificateUseCase {

    private final UserRepository userRepository;
    private final DniHasher dniHasher;
    private final CertificateVerificationTokenService tokenService;

    @Override
    @Transactional
    public String bindAndIssueToken(String sub, String dni) {
        if (dni == null || dni.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Certificate does not carry a DNI");
        }
        String dniHash = dniHasher.hash(dni);

        User user = userRepository.findById(sub)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getDniHash() == null) {
            user.setDniHash(dniHash);
            userRepository.save(user);
            log.info("Bound DNI hash to user sub={} on first certificate verification", sub);
        } else if (!user.getDniHash().equals(dniHash)) {
            log.warn("Certificate DNI mismatch for sub={}: account is bound to a different certificate", sub);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This account is already bound to a different certificate");
        }

        return tokenService.issue(dniHash);
    }
}
