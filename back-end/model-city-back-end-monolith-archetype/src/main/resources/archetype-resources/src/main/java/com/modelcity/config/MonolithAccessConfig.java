package com.modelcity.config;

import com.modelcity.common.client.CoreClient;
import com.modelcity.common.dto.OperationAuthorizationResponseDto;
import com.modelcity.common.security.ModelCityAccessAspect;
import com.modelcity.core.otp.usecase.BurnOperationAuthorizationUseCase;
import com.modelcity.core.otp.usecase.GetOperationAuthorizationUseCase;
import com.modelcity.core.users.repository.UserRepository;
import com.modelcity.core.users.repository.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

/**
 * In-process replacement for {@link CoreClient}. The microservices deployment uses a
 * WebClient call to {@code http://core/...}; in the monolith we resolve everything in-process:
 * users from the JPA repository and operation authorizations from the transaction-authorization use cases.
 *
 * Also registers the {@link ModelCityAccessAspect} that backs the {@code @ModelCityAccess}
 * annotations on controller methods.
 */
@Configuration
public class MonolithAccessConfig {

    /** Builds a {@link CoreClient} subclass that talks to local components instead of HTTP. */
    @Bean
    @Primary
    public CoreClient coreClient(UserRepository userRepository,
                                 WebClient.Builder webClientBuilder,
                                 GetOperationAuthorizationUseCase getOperationAuthorizationUseCase,
                                 BurnOperationAuthorizationUseCase burnOperationAuthorizationUseCase) {
        return new InProcessCoreClient(webClientBuilder, userRepository,
                getOperationAuthorizationUseCase, burnOperationAuthorizationUseCase);
    }

    @Bean
    public ModelCityAccessAspect modelCityAccessAspect(CoreClient coreClient) {
        return new ModelCityAccessAspect(coreClient);
    }

    /** CoreClient implementation backed by local repositories and use cases. */
    private static final class InProcessCoreClient extends CoreClient {

        private final UserRepository userRepository;
        private final GetOperationAuthorizationUseCase getOperationAuthorizationUseCase;
        private final BurnOperationAuthorizationUseCase burnOperationAuthorizationUseCase;

        InProcessCoreClient(WebClient.Builder webClientBuilder,
                            UserRepository userRepository,
                            GetOperationAuthorizationUseCase getOperationAuthorizationUseCase,
                            BurnOperationAuthorizationUseCase burnOperationAuthorizationUseCase) {
            super(webClientBuilder);
            this.userRepository = userRepository;
            this.getOperationAuthorizationUseCase = getOperationAuthorizationUseCase;
            this.burnOperationAuthorizationUseCase = burnOperationAuthorizationUseCase;
        }

        @Override
        public String getUserRole(String sub) {
            return userRepository.findById(sub)
                    .map(User::getRole)
                    .map(r -> r.getValue())
                    .orElse(null);
        }

        @Override
        public String getUserName(String sub) {
            return userRepository.findById(sub).map(User::getName).orElse(null);
        }

        @Override
        public OperationAuthorizationResponseDto getOperationAuthorization(UUID id) {
            return getOperationAuthorizationUseCase.execute(id);
        }

        @Override
        public void burnOperationAuthorization(UUID id) {
            burnOperationAuthorizationUseCase.execute(id);
        }
    }
}
