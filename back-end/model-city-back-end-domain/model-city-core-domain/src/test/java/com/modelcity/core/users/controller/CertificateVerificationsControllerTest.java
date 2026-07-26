package com.modelcity.core.users.controller;

import com.modelcity.core.users.controller.model.CertificateIdentityDto;
import com.modelcity.core.users.usecase.HandleCertificateVerificationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificateVerificationsControllerTest {

    @Mock HandleCertificateVerificationUseCase certificateVerificationUseCase;

    DefaultCertificateVerificationsController controller;

    @BeforeEach
    void setUp() {
        controller = new DefaultCertificateVerificationsController(certificateVerificationUseCase);
    }

    @Test
    void verify_delegatesToUseCaseWithAllHeaders() {
        CertificateIdentityDto identity = new CertificateIdentityDto("dni-hash", "token");
        when(certificateVerificationUseCase.execute("user-sub", "subject", "issuer", "validity", "leaf"))
                .thenReturn(identity);

        ResponseEntity<CertificateIdentityDto> result =
                controller.verify("user-sub", "subject", "issuer", "validity", "leaf");

        assertThat(result.getBody()).isEqualTo(identity);
    }
}
