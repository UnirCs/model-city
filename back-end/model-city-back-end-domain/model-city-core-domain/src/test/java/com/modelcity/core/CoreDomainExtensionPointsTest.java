package com.modelcity.core;

import com.modelcity.common.dto.OperationAuthorizationResponseDto;
import com.modelcity.common.config.ModelCityExtensibilityAutoConfiguration;
import com.modelcity.core.trails.SystemTrailGenerator;
import com.modelcity.core.trails.controller.DefaultSystemTrailController;
import com.modelcity.core.trails.controller.SystemTrailController;
import com.modelcity.core.trails.store.CoreSystemTrailStore;
import com.modelcity.core.trails.usecase.DefaultGetSystemTrailsUseCase;
import com.modelcity.core.trails.usecase.GetSystemTrailsUseCase;
import com.modelcity.core.otp.controller.DefaultOperationAuthorizationController;
import com.modelcity.core.otp.controller.OperationAuthorizationController;
import com.modelcity.core.otp.controller.model.CreateChallengeRequestDto;
import com.modelcity.core.otp.controller.model.ValidateChallengeRequestDto;
import com.modelcity.core.otp.repository.OperationAuthorizationRepository;
import com.modelcity.core.otp.usecase.BurnOperationAuthorizationUseCase;
import com.modelcity.core.otp.usecase.CreateChallengeUseCase;
import com.modelcity.core.otp.usecase.DefaultBurnOperationAuthorizationUseCase;
import com.modelcity.core.otp.usecase.DefaultCreateChallengeUseCase;
import com.modelcity.core.otp.usecase.DefaultGetOperationAuthorizationUseCase;
import com.modelcity.core.otp.usecase.DefaultRegenerateOtpUseCase;
import com.modelcity.core.otp.usecase.DefaultValidateChallengeUseCase;
import com.modelcity.core.otp.usecase.GetOperationAuthorizationUseCase;
import com.modelcity.core.otp.usecase.RegenerateOtpUseCase;
import com.modelcity.core.otp.usecase.ValidateChallengeUseCase;
import com.modelcity.core.otp.usecase.mail.OtpService;
import com.modelcity.core.security.CertificateVerificationTokenService;
import com.modelcity.core.security.DniHasher;
import com.modelcity.core.users.controller.AgentController;
import com.modelcity.core.users.controller.CertificateVerificationsController;
import com.modelcity.core.users.controller.DefaultAgentController;
import com.modelcity.core.users.controller.DefaultCertificateVerificationsController;
import com.modelcity.core.users.controller.DefaultUsersController;
import com.modelcity.core.users.controller.UsersController;
import com.modelcity.core.users.controller.model.InviteAgentRequestDto;
import com.modelcity.core.users.controller.model.SignInRequestDto;
import com.modelcity.core.users.controller.model.UserProfileDto;
import com.modelcity.core.users.controller.model.UserSummaryDto;
import com.modelcity.core.users.facade.Auth0ManagementFacade;
import com.modelcity.core.users.repository.NeighbourhoodRepository;
import com.modelcity.core.users.repository.UserRepository;
import com.modelcity.core.users.usecase.DecodeCertificateUseCase;
import com.modelcity.core.users.usecase.DefaultDecodeCertificateUseCase;
import com.modelcity.core.users.usecase.DefaultDeleteUserUseCase;
import com.modelcity.core.users.usecase.DefaultFindUserUseCase;
import com.modelcity.core.users.usecase.DefaultGetUserUseCase;
import com.modelcity.core.users.usecase.DefaultHandleCertificateVerificationUseCase;
import com.modelcity.core.users.usecase.DefaultInviteAgentUseCase;
import com.modelcity.core.users.usecase.DefaultListUsersUseCase;
import com.modelcity.core.users.usecase.DefaultRegisterUserUseCase;
import com.modelcity.core.users.usecase.DefaultSetUserStatusUseCase;
import com.modelcity.core.users.usecase.DefaultVerifyCertificateUseCase;
import com.modelcity.core.users.usecase.DeleteUserUseCase;
import com.modelcity.core.users.usecase.FindUserUseCase;
import com.modelcity.core.users.usecase.GetUserUseCase;
import com.modelcity.core.users.usecase.HandleCertificateVerificationUseCase;
import com.modelcity.core.users.usecase.InviteAgentUseCase;
import com.modelcity.core.users.usecase.ListUsersUseCase;
import com.modelcity.core.users.usecase.RegisterUserUseCase;
import com.modelcity.core.users.usecase.SetUserStatusUseCase;
import com.modelcity.core.users.usecase.VerifyCertificateUseCase;
import com.modelcity.core.users.usecase.mail.InvitationMailService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Verifies the core extension points under the scanned-defaults model: the {@code Default*} beans carry a
 * regular stereotype plus {@code @ModelCityDisabledIfInherited} and are registered by component scanning (here
 * simulated with {@code withBean}). By default the platform's {@code Default*} beans are wired, and as soon as a
 * local deployment declares its own use case or controller bean for the same seam, the matching default is
 * removed at startup by
 * {@link com.modelcity.common.extensibility.ModelCityDisabledIfInheritedProcessor} (registered by
 * {@link ModelCityExtensibilityAutoConfiguration}).
 */
class CoreDomainExtensionPointsTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ModelCityExtensibilityAutoConfiguration.class))
            .withBean(OperationAuthorizationRepository.class, () -> mock(OperationAuthorizationRepository.class))
            .withBean(UserRepository.class, () -> mock(UserRepository.class))
            .withBean(OtpService.class, () -> mock(OtpService.class))
            .withBean(CertificateVerificationTokenService.class, () -> mock(CertificateVerificationTokenService.class))
            .withBean(SystemTrailGenerator.class, () -> mock(SystemTrailGenerator.class))
            .withBean(NeighbourhoodRepository.class, () -> mock(NeighbourhoodRepository.class))
            .withBean(Auth0ManagementFacade.class, () -> mock(Auth0ManagementFacade.class))
            .withBean(InvitationMailService.class, () -> mock(InvitationMailService.class))
            .withBean(DniHasher.class, () -> mock(DniHasher.class))
            .withBean(CoreSystemTrailStore.class, () -> mock(CoreSystemTrailStore.class))
            // otp
            .withBean(DefaultCreateChallengeUseCase.class)
            .withBean(DefaultRegenerateOtpUseCase.class)
            .withBean(DefaultValidateChallengeUseCase.class)
            .withBean(DefaultBurnOperationAuthorizationUseCase.class)
            .withBean(DefaultGetOperationAuthorizationUseCase.class)
            .withBean(DefaultOperationAuthorizationController.class)
            // system trails
            .withBean(DefaultGetSystemTrailsUseCase.class)
            .withBean(DefaultSystemTrailController.class)
            // users
            .withBean(DefaultGetUserUseCase.class)
            .withBean(DefaultRegisterUserUseCase.class)
            .withBean(DefaultListUsersUseCase.class)
            .withBean(DefaultDeleteUserUseCase.class)
            .withBean(DefaultSetUserStatusUseCase.class)
            .withBean(DefaultFindUserUseCase.class)
            .withBean(DefaultInviteAgentUseCase.class)
            .withBean(DefaultVerifyCertificateUseCase.class)
            .withBean(DefaultDecodeCertificateUseCase.class)
            .withBean(DefaultHandleCertificateVerificationUseCase.class)
            .withBean(DefaultAgentController.class)
            .withBean(DefaultCertificateVerificationsController.class)
            .withBean(DefaultUsersController.class);

    // --- otp ---

    @Test
    void otpPlatformDefaultsAreWired() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(CreateChallengeUseCase.class);
            assertThat(context).hasSingleBean(ValidateChallengeUseCase.class);
            assertThat(context).hasSingleBean(GetOperationAuthorizationUseCase.class);
            assertThat(context).hasSingleBean(BurnOperationAuthorizationUseCase.class);
            assertThat(context).hasSingleBean(RegenerateOtpUseCase.class);
            assertThat(context).hasSingleBean(OperationAuthorizationController.class);
            assertThat(context.getBean(CreateChallengeUseCase.class)).isInstanceOf(DefaultCreateChallengeUseCase.class);
            assertThat(context.getBean(OperationAuthorizationController.class)).isInstanceOf(DefaultOperationAuthorizationController.class);
        });
    }

    @Test
    void otpUseCaseOverrideWins() {
        runner.withUserConfiguration(OtpUseCaseOverride.class).run(context -> {
            assertThat(context).hasSingleBean(CreateChallengeUseCase.class);
            assertThat(context).doesNotHaveBean(DefaultCreateChallengeUseCase.class);
            assertThat(context.getBean(CreateChallengeUseCase.class)).isInstanceOf(OtpUseCaseOverride.CustomCreateChallenge.class);
        });
    }

    @Test
    void otpControllerOverrideWins() {
        runner.withUserConfiguration(OtpControllerOverride.class).run(context -> {
            assertThat(context).hasSingleBean(OperationAuthorizationController.class);
            assertThat(context).doesNotHaveBean(DefaultOperationAuthorizationController.class);
            assertThat(context.getBean(OperationAuthorizationController.class)).isInstanceOf(OtpControllerOverride.CustomOperationAuthorizationController.class);
        });
    }

    // --- system trails ---

    @Test
    void systemEventsPlatformDefaultsAreWired() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(GetSystemTrailsUseCase.class);
            assertThat(context).hasSingleBean(SystemTrailController.class);
            assertThat(context.getBean(GetSystemTrailsUseCase.class)).isInstanceOf(DefaultGetSystemTrailsUseCase.class);
            assertThat(context.getBean(SystemTrailController.class)).isInstanceOf(DefaultSystemTrailController.class);
        });
    }

    // --- users ---

    @Test
    void usersPlatformDefaultsAreWired() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(GetUserUseCase.class);
            assertThat(context).hasSingleBean(RegisterUserUseCase.class);
            assertThat(context).hasSingleBean(ListUsersUseCase.class);
            assertThat(context).hasSingleBean(FindUserUseCase.class);
            assertThat(context).hasSingleBean(InviteAgentUseCase.class);
            assertThat(context).hasSingleBean(DeleteUserUseCase.class);
            assertThat(context).hasSingleBean(SetUserStatusUseCase.class);
            assertThat(context).hasSingleBean(VerifyCertificateUseCase.class);
            assertThat(context).hasSingleBean(DecodeCertificateUseCase.class);
            assertThat(context).hasSingleBean(HandleCertificateVerificationUseCase.class);
            assertThat(context).hasSingleBean(UsersController.class);
            assertThat(context).hasSingleBean(AgentController.class);
            assertThat(context).hasSingleBean(CertificateVerificationsController.class);
            assertThat(context.getBean(GetUserUseCase.class)).isInstanceOf(DefaultGetUserUseCase.class);
            assertThat(context.getBean(UsersController.class)).isInstanceOf(DefaultUsersController.class);
            assertThat(context.getBean(AgentController.class)).isInstanceOf(DefaultAgentController.class);
            assertThat(context.getBean(CertificateVerificationsController.class)).isInstanceOf(DefaultCertificateVerificationsController.class);
        });
    }

    @Test
    void usersUseCaseOverrideWins() {
        runner.withUserConfiguration(UsersUseCaseOverride.class).run(context -> {
            assertThat(context).hasSingleBean(GetUserUseCase.class);
            assertThat(context).doesNotHaveBean(DefaultGetUserUseCase.class);
            assertThat(context.getBean(GetUserUseCase.class)).isInstanceOf(UsersUseCaseOverride.CustomGetUser.class);
        });
    }

    @Test
    void usersControllerOverrideWins() {
        runner.withUserConfiguration(UsersControllerOverride.class).run(context -> {
            assertThat(context).hasSingleBean(UsersController.class);
            assertThat(context).doesNotHaveBean(DefaultUsersController.class);
            assertThat(context.getBean(UsersController.class)).isInstanceOf(UsersControllerOverride.CustomUsersController.class);
        });
    }

    @Test
    void certificateVerificationsControllerOverrideWins() {
        runner.withUserConfiguration(CertificateVerificationsControllerOverride.class).run(context -> {
            assertThat(context).hasSingleBean(CertificateVerificationsController.class);
            assertThat(context).doesNotHaveBean(DefaultCertificateVerificationsController.class);
            assertThat(context.getBean(CertificateVerificationsController.class))
                    .isInstanceOf(CertificateVerificationsControllerOverride.CustomCertificateVerificationsController.class);
        });
    }

    // --- override configurations ---

    @Configuration
    static class OtpUseCaseOverride {
        @Bean
        CreateChallengeUseCase<CreateChallengeRequestDto> customCreateChallenge() {
            return new CustomCreateChallenge();
        }

        static class CustomCreateChallenge implements CreateChallengeUseCase<CreateChallengeRequestDto> {
            @Override
            public OperationAuthorizationResponseDto execute(String sub, CreateChallengeRequestDto request) {
                return null;
            }
        }
    }

    @Configuration
    static class OtpControllerOverride {
        @Bean
        OperationAuthorizationController<CreateChallengeRequestDto, ValidateChallengeRequestDto> customOperationAuthorizationController(
                CreateChallengeUseCase<CreateChallengeRequestDto> createChallengeUseCase,
                ValidateChallengeUseCase<ValidateChallengeRequestDto> validateChallengeUseCase,
                GetOperationAuthorizationUseCase getOperationAuthorizationUseCase,
                BurnOperationAuthorizationUseCase burnOperationAuthorizationUseCase) {
            return new CustomOperationAuthorizationController(createChallengeUseCase, validateChallengeUseCase,
                    getOperationAuthorizationUseCase, burnOperationAuthorizationUseCase);
        }

        static class CustomOperationAuthorizationController extends OperationAuthorizationController<CreateChallengeRequestDto, ValidateChallengeRequestDto> {
            CustomOperationAuthorizationController(
                    CreateChallengeUseCase<CreateChallengeRequestDto> createChallengeUseCase,
                    ValidateChallengeUseCase<ValidateChallengeRequestDto> validateChallengeUseCase,
                    GetOperationAuthorizationUseCase getOperationAuthorizationUseCase,
                    BurnOperationAuthorizationUseCase burnOperationAuthorizationUseCase) {
                super(createChallengeUseCase, validateChallengeUseCase,
                        getOperationAuthorizationUseCase, burnOperationAuthorizationUseCase);
            }
        }
    }

    @Configuration
    static class UsersUseCaseOverride {
        @Bean
        GetUserUseCase<UserProfileDto> customGetUser() {
            return new CustomGetUser();
        }

        static class CustomGetUser implements GetUserUseCase<UserProfileDto> {
            @Override
            public UserProfileDto execute(String requestingSub, String targetUserId, String locale) {
                return new UserProfileDto();
            }
        }
    }

    @Configuration
    static class UsersControllerOverride {
        @Bean
        UsersController<UserProfileDto, UserSummaryDto, SignInRequestDto> customUsersController(
                GetUserUseCase<UserProfileDto> getUserUseCase,
                RegisterUserUseCase<SignInRequestDto> registerUserUseCase,
                FindUserUseCase findUserUseCase,
                ListUsersUseCase<UserSummaryDto> listUsersUseCase,
                DeleteUserUseCase deleteUserUseCase,
                SetUserStatusUseCase setUserStatusUseCase) {
            return new CustomUsersController(getUserUseCase, registerUserUseCase, findUserUseCase, listUsersUseCase,
                    deleteUserUseCase, setUserStatusUseCase);
        }

        static class CustomUsersController extends UsersController<UserProfileDto, UserSummaryDto, SignInRequestDto> {
            CustomUsersController(
                    GetUserUseCase<UserProfileDto> getUserUseCase,
                    RegisterUserUseCase<SignInRequestDto> registerUserUseCase,
                    FindUserUseCase findUserUseCase,
                    ListUsersUseCase<UserSummaryDto> listUsersUseCase,
                    DeleteUserUseCase deleteUserUseCase,
                    SetUserStatusUseCase setUserStatusUseCase) {
                super(getUserUseCase, registerUserUseCase, findUserUseCase, listUsersUseCase, deleteUserUseCase,
                        setUserStatusUseCase);
            }
        }
    }

    @Configuration
    static class CertificateVerificationsControllerOverride {
        @Bean
        CertificateVerificationsController customCertificateVerificationsController(
                HandleCertificateVerificationUseCase certificateVerificationUseCase) {
            return new CustomCertificateVerificationsController(certificateVerificationUseCase);
        }

        static class CustomCertificateVerificationsController extends CertificateVerificationsController {
            CustomCertificateVerificationsController(HandleCertificateVerificationUseCase certificateVerificationUseCase) {
                super(certificateVerificationUseCase);
            }
        }
    }
}
