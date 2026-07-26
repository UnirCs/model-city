package com.modelcity.core.otp.usecase;

import com.modelcity.core.users.repository.UserRepository;
import com.modelcity.core.otp.repository.model.OperationAuthorization;
import com.modelcity.core.otp.repository.OperationAuthorizationRepository;
import com.modelcity.core.otp.usecase.mail.OtpService;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Default {@link RegenerateOtpUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@Slf4j
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultRegenerateOtpUseCase implements RegenerateOtpUseCase {

    private final OperationAuthorizationRepository repository;
    private final UserRepository userRepository;
    private final OtpService otpService;

    @Override
    @Transactional
    public void execute(OperationAuthorization auth) {
        String email = userRepository.findById(auth.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .getEmail();
        String newHash = otpService.generateOtpHash(email);
        auth.setOtpHash(newHash);
        repository.save(auth);
        log.info("New OTP generated and sent for challenge id={}", auth.getOperationAuthorizationId());
    }
}
