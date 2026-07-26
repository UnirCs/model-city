package com.modelcity.core.users.usecase;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.core.security.CertificateVerificationTokenService;
import com.modelcity.core.security.DniHasher;
import com.modelcity.core.trails.SystemTrailGenerator;
import com.modelcity.core.users.controller.model.CertificateIdentityDto;
import com.modelcity.core.users.controller.model.InviteAgentRequestDto;
import com.modelcity.core.users.controller.model.SignInRequestDto;
import com.modelcity.core.users.controller.model.UserProfileDto;
import com.modelcity.core.users.controller.model.UserSummaryDto;
import com.modelcity.core.users.facade.Auth0ManagementFacade;
import com.modelcity.core.users.repository.NeighbourhoodRepository;
import com.modelcity.core.users.repository.UserRepository;
import com.modelcity.core.users.repository.model.Neighbourhood;
import com.modelcity.core.users.repository.model.User;
import com.modelcity.core.users.repository.model.UserRole;
import com.modelcity.core.users.repository.model.UserStatus;
import com.modelcity.core.users.usecase.mail.InvitationMailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserUseCasesTest {

    @Mock
    UserRepository userRepository;

    @Mock
    NeighbourhoodRepository neighbourhoodRepository;

    @Mock
    Auth0ManagementFacade auth0ManagementFacade;

    @Mock
    InvitationMailService invitationMailService;

    @Mock
    SystemTrailGenerator systemTrailGenerator;

    @Mock
    DniHasher dniHasher;

    @Mock
    CertificateVerificationTokenService tokenService;

    @Mock
    DecodeCertificateUseCase decodeCertificateUseCase;

    @Mock
    VerifyCertificateUseCase verifyCertificateUseCase;

    private User buildUser(String id, String email, UserRole role) {
        User u = new User();
        u.setId(id);
        u.setName("Test User");
        u.setEmail(email);
        u.setRole(role);
        u.setStatus(UserStatus.ACTIVE);
        return u;
    }

    private Neighbourhood buildNeighbourhood(Long id, String name) {
        Neighbourhood n = new Neighbourhood();
        n.setId(id);
        n.setName(name);
        n.setDisplayName(name);
        return n;
    }

    @Nested
    class DeleteUserTests {

        DefaultDeleteUserUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultDeleteUserUseCase(userRepository, systemTrailGenerator);
        }

        @Test
        void execute_deletesNonAdminUser() {
            User target = buildUser("target-sub", "target@example.com", UserRole.MODEL_CITY_CITIZEN);
            when(userRepository.findById("target-sub")).thenReturn(Optional.of(target));

            useCase.execute("admin-sub", "target-sub");

            verify(userRepository).delete(target);
            verify(systemTrailGenerator).userDeleted("admin-sub", target);
        }

        @Test
        void execute_throwsWhenNotFound() {
            when(userRepository.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute("admin-sub", "missing"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void execute_throwsWhenTargetIsAdmin() {
            User admin = buildUser("target-sub", "admin@example.com", UserRole.MODEL_CITY_PLATFORM_ADMIN);
            when(userRepository.findById("target-sub")).thenReturn(Optional.of(admin));

            assertThatThrownBy(() -> useCase.execute("admin-sub", "target-sub"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("cannot be deleted");
        }
    }

    @Nested
    class FindUserTests {

        DefaultFindUserUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultFindUserUseCase(userRepository);
        }

        @Test
        void execute_returnsTrueWhenUserExists() {
            when(userRepository.existsById("user-sub")).thenReturn(true);

            assertThat(useCase.execute("user-sub")).isTrue();
        }

        @Test
        void execute_returnsFalseWhenUserMissing() {
            when(userRepository.existsById("missing")).thenReturn(false);

            assertThat(useCase.execute("missing")).isFalse();
        }
    }

    @Nested
    class GetUserTests {

        DefaultGetUserUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetUserUseCase(userRepository);
        }

        @Test
        void execute_selfAccess_returnsProfile() {
            User user = buildUser("user-sub", "user@example.com", UserRole.MODEL_CITY_CITIZEN);
            when(userRepository.findById("user-sub")).thenReturn(Optional.of(user));

            UserProfileDto result = useCase.execute("user-sub", "user-sub", "es");

            assertThat(result.getId()).isEqualTo("user-sub");
            assertThat(result.getEmail()).isEqualTo("user@example.com");
        }

        @Test
        void execute_adminAccessingOtherUser_returnsProfile() {
            User admin = buildUser("admin-sub", "admin@example.com", UserRole.MODEL_CITY_PLATFORM_ADMIN);
            User target = buildUser("target-sub", "target@example.com", UserRole.MODEL_CITY_CITIZEN);
            when(userRepository.findById("admin-sub")).thenReturn(Optional.of(admin));
            when(userRepository.findById("target-sub")).thenReturn(Optional.of(target));

            UserProfileDto result = useCase.execute("admin-sub", "target-sub", "es");

            assertThat(result.getId()).isEqualTo("target-sub");
        }

        @Test
        void execute_nonAdminAccessingOtherUser_throwsForbidden() {
            User requester = buildUser("citizen-sub", "citizen@example.com", UserRole.MODEL_CITY_CITIZEN);
            when(userRepository.findById("citizen-sub")).thenReturn(Optional.of(requester));

            assertThatThrownBy(() -> useCase.execute("citizen-sub", "other-sub", "es"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("requires admin role");
        }

        @Test
        void execute_throwsWhenTargetNotFound() {
            when(userRepository.findById("user-sub")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute("user-sub", "user-sub", "es"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class ListUsersTests {

        DefaultListUsersUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultListUsersUseCase(userRepository);
        }

        @Test
        void execute_withoutFilters_returnsAllUsers() {
            User user = buildUser("user-sub", "user@example.com", UserRole.MODEL_CITY_CITIZEN);
            Page<User> page = new PageImpl<>(List.of(user));
            when(userRepository.findAll(any(PageRequest.class))).thenReturn(page);

            Page<UserSummaryDto> result = useCase.execute("admin-sub", null, null, null, null, PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo("user-sub");
        }

        @Test
        @SuppressWarnings("unchecked")
        void execute_withFilters_usesSpecification() {
            User user = buildUser("user-sub", "user@example.com", UserRole.MODEL_CITY_CITIZEN);
            Page<User> page = new PageImpl<>(List.of(user));
            doReturn(page).when(userRepository).findAll(any(Specification.class), any(PageRequest.class));

            Page<UserSummaryDto> result = useCase.execute("admin-sub", true, "Test", 5L, UserRole.MODEL_CITY_CITIZEN, PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(userRepository).findAll(any(Specification.class), any(PageRequest.class));
        }
    }

    @Nested
    class RegisterUserTests {

        DefaultRegisterUserUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultRegisterUserUseCase(userRepository, neighbourhoodRepository, auth0ManagementFacade, systemTrailGenerator);
        }

        @Test
        void execute_newUser_registersWithResolvedRole() {
            Neighbourhood neighbourhood = buildNeighbourhood(1L, "el-recreo");
            SignInRequestDto request = new SignInRequestDto("Nuevo Usuario", "nuevo@example.com", "el-recreo");

            when(neighbourhoodRepository.findByName("el-recreo")).thenReturn(Optional.of(neighbourhood));
            when(userRepository.findById("new-sub")).thenReturn(Optional.empty());
            when(auth0ManagementFacade.getUserRoles("new-sub")).thenReturn(List.of());

            useCase.execute("new-sub", request);

            verify(userRepository).save(argThat(u -> u.getId().equals("new-sub")
                    && u.getRole() == UserRole.MODEL_CITY_CITIZEN
                    && u.getName().equals("Nuevo Usuario")));
            verify(systemTrailGenerator).userRegistered(any());
        }

        @Test
        void execute_existingUser_updatesRecord() {
            User existing = buildUser("existing-sub", "old@example.com", UserRole.MODEL_CITY_CITIZEN);
            Neighbourhood neighbourhood = buildNeighbourhood(1L, "el-recreo");
            SignInRequestDto request = new SignInRequestDto("Nombre Actualizado", "updated@example.com", "el-recreo");

            when(neighbourhoodRepository.findByName("el-recreo")).thenReturn(Optional.of(neighbourhood));
            when(userRepository.findById("existing-sub")).thenReturn(Optional.of(existing));

            useCase.execute("existing-sub", request);

            verify(userRepository).save(existing);
            verify(systemTrailGenerator).userUpdated(existing);
            verify(systemTrailGenerator, never()).userRegistered(any());
        }

        @Test
        void execute_throwsWhenNeighbourhoodNotFound() {
            SignInRequestDto request = new SignInRequestDto("Nuevo Usuario", "nuevo@example.com", "unknown");
            when(neighbourhoodRepository.findByName("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute("new-sub", request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class SetUserStatusTests {

        DefaultSetUserStatusUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultSetUserStatusUseCase(userRepository, systemTrailGenerator);
        }

        @Test
        void execute_updatesStatusOfNonAdminUser() {
            User target = buildUser("target-sub", "target@example.com", UserRole.MODEL_CITY_CITIZEN);
            when(userRepository.findById("target-sub")).thenReturn(Optional.of(target));

            useCase.execute("admin-sub", "target-sub", UserStatus.DISABLED);

            assertThat(target.getStatus()).isEqualTo(UserStatus.DISABLED);
            verify(userRepository).save(target);
            verify(systemTrailGenerator).userStatusChanged("admin-sub", target, "ACTIVE", "DISABLED");
        }

        @Test
        void execute_throwsWhenNotFound() {
            when(userRepository.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute("admin-sub", "missing", UserStatus.DISABLED))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void execute_throwsWhenTargetIsAdmin() {
            User admin = buildUser("target-sub", "admin@example.com", UserRole.MODEL_CITY_PLATFORM_ADMIN);
            when(userRepository.findById("target-sub")).thenReturn(Optional.of(admin));

            assertThatThrownBy(() -> useCase.execute("admin-sub", "target-sub", UserStatus.DISABLED))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("cannot be disabled");
        }
    }

    @Nested
    class InviteAgentTests {

        DefaultInviteAgentUseCase useCase;

        @BeforeEach
        void setUp() throws Exception {
            useCase = new DefaultInviteAgentUseCase(auth0ManagementFacade, invitationMailService, systemTrailGenerator);
            setPrivateField(useCase, "backofficeRoleId", "role-backoffice-id");
            setPrivateField(useCase, "operatorRoleId", "role-operator-id");
            setPrivateField(useCase, "mobilityAgentRoleId", "role-mobility-id");
        }

        private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        }

        @Test
        void execute_invitesBackofficeAgentSuccessfully() {
            InviteAgentRequestDto request = new InviteAgentRequestDto(
                    "Nuevo Agente", "agente@example.com", "MODEL-CITY-BACKOFFICE");

            when(auth0ManagementFacade.createUser("agente@example.com", "Nuevo Agente")).thenReturn("auth0|new-agent");
            when(auth0ManagementFacade.createPasswordChangeTicket("auth0|new-agent")).thenReturn("https://ticket-url");

            useCase.execute("admin-sub", request);

            verify(auth0ManagementFacade).assignRoles("auth0|new-agent", List.of("role-backoffice-id"));
            verify(invitationMailService).sendInvitation("agente@example.com", "Agente Administrativo", "https://ticket-url");
            verify(systemTrailGenerator).agentInvited("admin-sub", "agente@example.com", "Nuevo Agente", "MODEL-CITY-BACKOFFICE");
        }

        @Test
        void execute_throwsForInvalidRole() {
            InviteAgentRequestDto request = new InviteAgentRequestDto(
                    "Nuevo Agente", "agente@example.com", "MODEL-CITY-CITIZEN");

            assertThatThrownBy(() -> useCase.execute("admin-sub", request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Invalid role for invitation");
        }
    }

    @Nested
    class DecodeCertificateTests {

        DefaultDecodeCertificateUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultDecodeCertificateUseCase();
        }

        @Test
        void parse_throwsWhenSubjectAndIssuerMissing() {
            assertThatThrownBy(() -> useCase.parse("N/A", "N/A", "N/A", null))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    class VerifyCertificateTests {

        DefaultVerifyCertificateUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultVerifyCertificateUseCase(userRepository, dniHasher, tokenService);
        }

        @Test
        void bindAndIssueToken_firstVerification_bindsHash() {
            User user = buildUser("user-sub", "user@example.com", UserRole.MODEL_CITY_CITIZEN);
            when(dniHasher.hash("12345678A")).thenReturn("dni-hash-value");
            when(userRepository.findById("user-sub")).thenReturn(Optional.of(user));
            when(tokenService.issue("dni-hash-value")).thenReturn("signed-token");

            String result = useCase.bindAndIssueToken("user-sub", "12345678A");

            assertThat(result).isEqualTo("signed-token");
            assertThat(user.getDniHash()).isEqualTo("dni-hash-value");
            verify(userRepository).save(user);
        }

        @Test
        void bindAndIssueToken_matchingExistingHash_doesNotRebind() {
            User user = buildUser("user-sub", "user@example.com", UserRole.MODEL_CITY_CITIZEN);
            user.setDniHash("dni-hash-value");
            when(dniHasher.hash("12345678A")).thenReturn("dni-hash-value");
            when(userRepository.findById("user-sub")).thenReturn(Optional.of(user));
            when(tokenService.issue("dni-hash-value")).thenReturn("signed-token");

            useCase.bindAndIssueToken("user-sub", "12345678A");

            verify(userRepository, never()).save(any());
        }

        @Test
        void bindAndIssueToken_mismatchedHash_throwsConflict() {
            User user = buildUser("user-sub", "user@example.com", UserRole.MODEL_CITY_CITIZEN);
            user.setDniHash("other-hash");
            when(dniHasher.hash("12345678A")).thenReturn("dni-hash-value");
            when(userRepository.findById("user-sub")).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> useCase.bindAndIssueToken("user-sub", "12345678A"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("already bound");
        }

        @Test
        void bindAndIssueToken_throwsWhenDniBlank() {
            assertThatThrownBy(() -> useCase.bindAndIssueToken("user-sub", ""))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("does not carry a DNI");
        }
    }

    @Nested
    class HandleCertificateVerificationTests {

        DefaultHandleCertificateVerificationUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultHandleCertificateVerificationUseCase(decodeCertificateUseCase, verifyCertificateUseCase, dniHasher);
        }

        @Test
        void execute_delegatesToDecodeAndVerify() {
            when(decodeCertificateUseCase.parse("subject", "issuer", "validity", "leaf")).thenReturn("12345678A");
            when(verifyCertificateUseCase.bindAndIssueToken("user-sub", "12345678A")).thenReturn("signed-token");
            when(dniHasher.hash("12345678A")).thenReturn("dni-hash-value");

            CertificateIdentityDto result = useCase.execute("user-sub", "subject", "issuer", "validity", "leaf");

            assertThat(result.getVerificationToken()).isEqualTo("signed-token");
            assertThat(result.getDni()).isEqualTo("dni-hash-value");
        }

        @Test
        void execute_throwsWhenSubMissing() {
            assertThatThrownBy(() -> useCase.execute(null, "subject", "issuer", "validity", "leaf"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Authentication is required");
        }
    }
}
