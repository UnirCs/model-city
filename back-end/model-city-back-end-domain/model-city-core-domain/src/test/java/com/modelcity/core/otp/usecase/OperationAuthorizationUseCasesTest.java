package com.modelcity.core.otp.usecase;

import com.modelcity.common.dto.OperationAuthorizationResponseDto;
import com.modelcity.core.otp.controller.model.CreateChallengeRequestDto;
import com.modelcity.core.otp.controller.model.ValidateChallengeRequestDto;
import com.modelcity.core.otp.repository.OperationAuthorizationRepository;
import com.modelcity.core.otp.repository.model.OperationAuthorization;
import com.modelcity.core.otp.repository.model.OperationStatus;
import com.modelcity.core.otp.usecase.mail.OtpService;
import com.modelcity.core.security.CertificateVerificationTokenService;
import com.modelcity.core.trails.SystemTrailGenerator;
import com.modelcity.core.users.repository.UserRepository;
import com.modelcity.core.users.repository.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OperationAuthorizationUseCasesTest {

    @Mock
    OperationAuthorizationRepository repository;

    @Mock
    UserRepository userRepository;

    @Mock
    OtpService otpService;

    @Mock
    CertificateVerificationTokenService tokenService;

    @Mock
    SystemTrailGenerator systemTrailGenerator;

    private OperationAuthorization buildAuth(UUID id, OperationStatus status, String userId) {
        return OperationAuthorization.builder()
                .operationAuthorizationId(id)
                .operationType("CONFIRM_ANSWER")
                .resourceType("public-question")
                .resourceId("1")
                .userId(userId)
                .expiresAt(Instant.now().plusSeconds(120))
                .status(status)
                .otpHash("otphash")
                .attemptsRemaining(3)
                .createdAt(Instant.now())
                .build();
    }

    private User buildUser(String id, String email) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        return u;
    }

    @Nested
    class CreateChallengeTests {

        DefaultCreateChallengeUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultCreateChallengeUseCase(repository, userRepository, otpService, tokenService, systemTrailGenerator);
        }

        @Test
        void execute_createsChallengeWithoutVerificationToken() {
            User user = buildUser("user-sub", "citizen@example.com");
            CreateChallengeRequestDto request = new CreateChallengeRequestDto(
                    "CONFIRM_ANSWER", "public-question", "1", null);

            when(userRepository.findById("user-sub")).thenReturn(Optional.of(user));
            when(otpService.generateOtpHash("citizen@example.com")).thenReturn("otphash");

            OperationAuthorizationResponseDto result = useCase.execute("user-sub", request);

            assertThat(result.status()).isEqualTo("PENDING");
            assertThat(result.dniHash()).isNull();
            verify(repository).save(any(OperationAuthorization.class));
            verify(systemTrailGenerator).operationAuthorizationCreated(any());
        }

        @Test
        void execute_createsChallengeWithVerificationToken_bindsAndDniHash() {
            User user = buildUser("user-sub", "citizen@example.com");
            CreateChallengeRequestDto request = new CreateChallengeRequestDto(
                    "CONFIRM_ANSWER", "public-question", "1", "token-abc");

            when(userRepository.findById("user-sub")).thenReturn(Optional.of(user));
            when(otpService.generateOtpHash("citizen@example.com")).thenReturn("otphash");
            when(tokenService.verifyAndExtractDniHash("token-abc")).thenReturn("dni-hash-xyz");

            OperationAuthorizationResponseDto result = useCase.execute("user-sub", request);

            assertThat(result.dniHash()).isEqualTo("dni-hash-xyz");
        }

        @Test
        void execute_throwsWhenUserNotFound() {
            when(userRepository.findById("missing")).thenReturn(Optional.empty());

            CreateChallengeRequestDto request = new CreateChallengeRequestDto(
                    "CONFIRM_ANSWER", "public-question", "1", null);

            assertThatThrownBy(() -> useCase.execute("missing", request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    class ValidateChallengeTests {

        DefaultValidateChallengeUseCase useCase;

        @Mock
        RegenerateOtpUseCase regenerateOtpUseCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultValidateChallengeUseCase(repository, otpService, regenerateOtpUseCase, systemTrailGenerator);
        }

        private ValidateChallengeRequestDto matchingRequest(String otp) {
            return new ValidateChallengeRequestDto(otp, "CONFIRM_ANSWER", "public-question", "1");
        }

        @Test
        void execute_validatesCorrectOtp() {
            UUID id = UUID.randomUUID();
            OperationAuthorization auth = buildAuth(id, OperationStatus.PENDING, "user-sub");
            when(repository.findById(id)).thenReturn(Optional.of(auth));
            when(otpService.validateOtpHash("123456", "otphash")).thenReturn(true);

            OperationAuthorizationResponseDto result = useCase.execute(id, "user-sub", matchingRequest("123456"));

            assertThat(result.status()).isEqualTo("VERIFIED");
            verify(systemTrailGenerator).operationAuthorizationVerified(auth);
        }

        @Test
        void execute_throwsWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(id, "user-sub", matchingRequest("123456")))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Challenge not found");
        }

        @Test
        void execute_throwsWhenUserMismatch() {
            UUID id = UUID.randomUUID();
            OperationAuthorization auth = buildAuth(id, OperationStatus.PENDING, "owner-sub");
            when(repository.findById(id)).thenReturn(Optional.of(auth));

            assertThatThrownBy(() -> useCase.execute(id, "other-sub", matchingRequest("123456")))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("does not belong");
        }

        @Test
        void execute_throwsWhenAlreadyVerified() {
            UUID id = UUID.randomUUID();
            OperationAuthorization auth = buildAuth(id, OperationStatus.VERIFIED, "user-sub");
            when(repository.findById(id)).thenReturn(Optional.of(auth));

            assertThatThrownBy(() -> useCase.execute(id, "user-sub", matchingRequest("123456")))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("already been verified");
        }

        @Test
        void execute_throwsWhenExpired() {
            UUID id = UUID.randomUUID();
            OperationAuthorization auth = buildAuth(id, OperationStatus.PENDING, "user-sub");
            auth.setExpiresAt(Instant.now().minusSeconds(60));
            when(repository.findById(id)).thenReturn(Optional.of(auth));

            assertThatThrownBy(() -> useCase.execute(id, "user-sub", matchingRequest("123456")))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("expired");
        }

        @Test
        void execute_throwsWhenOperationMismatch() {
            UUID id = UUID.randomUUID();
            OperationAuthorization auth = buildAuth(id, OperationStatus.PENDING, "user-sub");
            when(repository.findById(id)).thenReturn(Optional.of(auth));

            ValidateChallengeRequestDto mismatched =
                    new ValidateChallengeRequestDto("123456", "OTHER_TYPE", "public-question", "1");

            assertThatThrownBy(() -> useCase.execute(id, "user-sub", mismatched))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("mismatch");
        }

        @Test
        void execute_wrongOtp_decrementsAttemptsAndRegenerates() {
            UUID id = UUID.randomUUID();
            OperationAuthorization auth = buildAuth(id, OperationStatus.PENDING, "user-sub");
            when(repository.findById(id)).thenReturn(Optional.of(auth));
            when(otpService.validateOtpHash("000000", "otphash")).thenReturn(false);

            assertThatThrownBy(() -> useCase.execute(id, "user-sub", matchingRequest("000000")))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("attempt(s) remaining");

            verify(regenerateOtpUseCase).execute(auth);
        }

        @Test
        void execute_wrongOtp_exhaustsAttempts_marksExpired() {
            UUID id = UUID.randomUUID();
            OperationAuthorization auth = buildAuth(id, OperationStatus.PENDING, "user-sub");
            auth.setAttemptsRemaining(1);
            when(repository.findById(id)).thenReturn(Optional.of(auth));
            when(otpService.validateOtpHash("000000", "otphash")).thenReturn(false);

            assertThatThrownBy(() -> useCase.execute(id, "user-sub", matchingRequest("000000")))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("No attempts remaining");

            assertThat(auth.getStatus()).isEqualTo(OperationStatus.EXPIRED);
            verify(regenerateOtpUseCase, never()).execute(any());
        }
    }

    @Nested
    class RegenerateOtpTests {

        DefaultRegenerateOtpUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultRegenerateOtpUseCase(repository, userRepository, otpService);
        }

        @Test
        void execute_regeneratesOtpForExistingUser() {
            OperationAuthorization auth = buildAuth(UUID.randomUUID(), OperationStatus.PENDING, "user-sub");
            User user = buildUser("user-sub", "citizen@example.com");

            when(userRepository.findById("user-sub")).thenReturn(Optional.of(user));
            when(otpService.generateOtpHash("citizen@example.com")).thenReturn("new-hash");

            useCase.execute(auth);

            assertThat(auth.getOtpHash()).isEqualTo("new-hash");
            verify(repository).save(auth);
        }

        @Test
        void execute_throwsWhenUserNotFound() {
            OperationAuthorization auth = buildAuth(UUID.randomUUID(), OperationStatus.PENDING, "missing");
            when(userRepository.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(auth))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    class BurnOperationAuthorizationTests {

        DefaultBurnOperationAuthorizationUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultBurnOperationAuthorizationUseCase(repository, systemTrailGenerator);
        }

        @Test
        void execute_burnsVerifiedAuthorization() {
            UUID id = UUID.randomUUID();
            OperationAuthorization auth = buildAuth(id, OperationStatus.VERIFIED, "user-sub");
            when(repository.findById(id)).thenReturn(Optional.of(auth));

            OperationAuthorizationResponseDto result = useCase.execute(id);

            assertThat(result.status()).isEqualTo("BURNT");
            verify(systemTrailGenerator).operationAuthorizationBurnt(auth);
        }

        @Test
        void execute_throwsWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(id))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        void execute_throwsWhenNotVerified() {
            UUID id = UUID.randomUUID();
            OperationAuthorization auth = buildAuth(id, OperationStatus.PENDING, "user-sub");
            when(repository.findById(id)).thenReturn(Optional.of(auth));

            assertThatThrownBy(() -> useCase.execute(id))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Only VERIFIED");
        }
    }

    @Nested
    class GetOperationAuthorizationTests {

        DefaultGetOperationAuthorizationUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetOperationAuthorizationUseCase(repository);
        }

        @Test
        void execute_returnsAuthorizationById() {
            UUID id = UUID.randomUUID();
            OperationAuthorization auth = buildAuth(id, OperationStatus.PENDING, "user-sub");
            when(repository.findById(id)).thenReturn(Optional.of(auth));

            OperationAuthorizationResponseDto result = useCase.execute(id);

            assertThat(result.operationAuthorizationId()).isEqualTo(id);
        }

        @Test
        void execute_throwsWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(id))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("not found");
        }
    }
}
