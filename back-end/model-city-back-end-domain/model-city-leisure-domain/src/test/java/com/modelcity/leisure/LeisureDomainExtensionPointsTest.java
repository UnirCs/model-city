package com.modelcity.leisure;

import com.modelcity.common.client.CoreClient;
import com.modelcity.common.config.ModelCityExtensibilityAutoConfiguration;
import com.modelcity.leisure.cityplaces.controller.CityPlaceController;
import com.modelcity.leisure.cityplaces.controller.DefaultCityPlaceController;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceDto;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceRequestDto;
import com.modelcity.leisure.cityplaces.controller.model.CityPlaceSummaryDto;
import com.modelcity.leisure.cityplaces.store.CityPlaceStore;
import com.modelcity.leisure.cityplaces.usecase.CreateCityPlaceUseCase;
import com.modelcity.leisure.cityplaces.usecase.DefaultCreateCityPlaceUseCase;
import com.modelcity.leisure.cityplaces.usecase.DefaultDeleteCityPlaceUseCase;
import com.modelcity.leisure.cityplaces.usecase.DefaultGetCityPlaceForEditUseCase;
import com.modelcity.leisure.cityplaces.usecase.DefaultGetCityPlaceUseCase;
import com.modelcity.leisure.cityplaces.usecase.DefaultGetCityPlacesUseCase;
import com.modelcity.leisure.cityplaces.usecase.DefaultUpdateCityPlaceUseCase;
import com.modelcity.leisure.cityplaces.usecase.GetCityPlaceForEditUseCase;
import com.modelcity.leisure.cityplaces.usecase.UpdateCityPlaceUseCase;
import com.modelcity.leisure.cityplaces.usecase.DeleteCityPlaceUseCase;
import com.modelcity.leisure.cityplaces.usecase.GetCityPlaceUseCase;
import com.modelcity.leisure.cityplaces.usecase.GetCityPlacesUseCase;
import com.modelcity.leisure.cityroutes.controller.CityRouteController;
import com.modelcity.leisure.cityroutes.controller.DefaultCityRouteController;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteDto;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteRequestDto;
import com.modelcity.leisure.cityroutes.controller.model.CityRouteSummaryDto;
import com.modelcity.leisure.cityroutes.store.CityRouteStore;
import com.modelcity.leisure.cityroutes.usecase.CreateCityRouteUseCase;
import com.modelcity.leisure.cityroutes.usecase.DefaultCreateCityRouteUseCase;
import com.modelcity.leisure.cityroutes.usecase.DefaultDeleteCityRouteUseCase;
import com.modelcity.leisure.cityroutes.usecase.DefaultGetCityRouteForEditUseCase;
import com.modelcity.leisure.cityroutes.usecase.DefaultGetCityRoutePlaceUseCase;
import com.modelcity.leisure.cityroutes.usecase.DefaultGetCityRoutePlacesUseCase;
import com.modelcity.leisure.cityroutes.usecase.DefaultGetCityRouteUseCase;
import com.modelcity.leisure.cityroutes.usecase.DefaultGetCityRoutesUseCase;
import com.modelcity.leisure.cityroutes.usecase.DefaultUpdateCityRouteUseCase;
import com.modelcity.leisure.cityroutes.usecase.GetCityRouteForEditUseCase;
import com.modelcity.leisure.cityroutes.usecase.GetCityRoutePlaceUseCase;
import com.modelcity.leisure.cityroutes.usecase.GetCityRoutePlacesUseCase;
import com.modelcity.leisure.cityroutes.usecase.DeleteCityRouteUseCase;
import com.modelcity.leisure.cityroutes.usecase.GetCityRouteUseCase;
import com.modelcity.leisure.cityroutes.usecase.GetCityRoutesUseCase;
import com.modelcity.leisure.cityroutes.usecase.UpdateCityRouteUseCase;
import com.modelcity.leisure.publicspaces.controller.DefaultPublicSpaceController;
import com.modelcity.leisure.publicspaces.controller.DefaultReservableResourceController;
import com.modelcity.leisure.publicspaces.controller.DefaultReservationController;
import com.modelcity.leisure.publicspaces.controller.PublicSpaceController;
import com.modelcity.leisure.publicspaces.controller.ReservableResourceController;
import com.modelcity.leisure.publicspaces.controller.ReservationController;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceDto;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceRequestDto;
import com.modelcity.leisure.publicspaces.controller.model.PublicSpaceSummaryDto;
import com.modelcity.leisure.publicspaces.store.PublicSpaceStore;
import com.modelcity.leisure.publicspaces.store.ReservableResourceStore;
import com.modelcity.leisure.publicspaces.store.SpaceReservationStore;
import com.modelcity.leisure.publicspaces.usecase.CreatePublicSpaceUseCase;
import com.modelcity.leisure.publicspaces.usecase.DefaultCreatePublicSpaceUseCase;
import com.modelcity.leisure.publicspaces.usecase.DefaultCreateReservableResourceUseCase;
import com.modelcity.leisure.publicspaces.usecase.DefaultCreateReservationUseCase;
import com.modelcity.leisure.publicspaces.usecase.DefaultDeletePublicSpaceUseCase;
import com.modelcity.leisure.publicspaces.usecase.DefaultDeleteReservableResourceUseCase;
import com.modelcity.leisure.publicspaces.usecase.DefaultDeleteReservationUseCase;
import com.modelcity.leisure.publicspaces.usecase.DefaultGetPublicSpaceForEditUseCase;
import com.modelcity.leisure.publicspaces.usecase.DefaultGetPublicSpaceUseCase;
import com.modelcity.leisure.publicspaces.usecase.DefaultGetPublicSpacesUseCase;
import com.modelcity.leisure.publicspaces.usecase.DefaultGetReservableResourcesForEditUseCase;
import com.modelcity.leisure.publicspaces.usecase.DefaultGetReservableResourcesUseCase;
import com.modelcity.leisure.publicspaces.usecase.DefaultGetReservationsUseCase;
import com.modelcity.leisure.publicspaces.usecase.DefaultUpdatePublicSpaceUseCase;
import com.modelcity.leisure.publicspaces.usecase.DefaultUpdateReservableResourceUseCase;
import com.modelcity.leisure.publicspaces.usecase.DeletePublicSpaceUseCase;
import com.modelcity.leisure.publicspaces.usecase.UpdatePublicSpaceUseCase;
import com.modelcity.leisure.publicspaces.usecase.GetPublicSpaceForEditUseCase;
import com.modelcity.leisure.publicspaces.usecase.GetPublicSpaceUseCase;
import com.modelcity.leisure.publicspaces.usecase.GetPublicSpacesUseCase;
import com.modelcity.leisure.trails.SystemTrailGenerator;
import com.modelcity.leisure.trails.controller.DefaultSystemTrailController;
import com.modelcity.leisure.trails.controller.SystemTrailController;
import com.modelcity.leisure.trails.store.LeisureSystemTrailStore;
import com.modelcity.leisure.trails.usecase.DefaultGetSystemTrailsUseCase;
import com.modelcity.leisure.trails.usecase.GetSystemTrailsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Verifies the leisure extension points under the scanned-defaults model: the {@code Default*} beans carry a
 * regular stereotype plus {@code @ModelCityDisabledIfInherited} and are registered by component scanning (here
 * simulated with {@code withBean}). By default the platform's {@code Default*} beans are wired, and as soon as a
 * local deployment declares its own use case or controller bean for the same seam, the matching default is
 * removed at startup by
 * {@link com.modelcity.common.extensibility.ModelCityDisabledIfInheritedProcessor} (registered by
 * {@link ModelCityExtensibilityAutoConfiguration}). The events vertical is covered separately by
 * {@code EventExtensionPointsTest}.
 */
class LeisureDomainExtensionPointsTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ModelCityExtensibilityAutoConfiguration.class))
            .withBean(CityPlaceStore.class, () -> mock(CityPlaceStore.class))
            .withBean(CityRouteStore.class, () -> mock(CityRouteStore.class))
            .withBean(PublicSpaceStore.class, () -> mock(PublicSpaceStore.class))
            .withBean(ReservableResourceStore.class, () -> mock(ReservableResourceStore.class))
            .withBean(SpaceReservationStore.class, () -> mock(SpaceReservationStore.class))
            .withBean(LeisureSystemTrailStore.class, () -> mock(LeisureSystemTrailStore.class))
            .withBean(CoreClient.class, () -> mock(CoreClient.class))
            .withBean(SystemTrailGenerator.class, () -> mock(SystemTrailGenerator.class))
            // city places
            .withBean(DefaultGetCityPlacesUseCase.class)
            .withBean(DefaultGetCityPlaceUseCase.class)
            .withBean(DefaultGetCityPlaceForEditUseCase.class)
            .withBean(DefaultCreateCityPlaceUseCase.class)
            .withBean(DefaultUpdateCityPlaceUseCase.class)
            .withBean(DefaultDeleteCityPlaceUseCase.class)
            .withBean(DefaultCityPlaceController.class)
            // city routes
            .withBean(DefaultGetCityRoutesUseCase.class)
            .withBean(DefaultGetCityRouteUseCase.class)
            .withBean(DefaultGetCityRouteForEditUseCase.class)
            .withBean(DefaultGetCityRoutePlacesUseCase.class)
            .withBean(DefaultGetCityRoutePlaceUseCase.class)
            .withBean(DefaultCreateCityRouteUseCase.class)
            .withBean(DefaultUpdateCityRouteUseCase.class)
            .withBean(DefaultDeleteCityRouteUseCase.class)
            .withBean(DefaultCityRouteController.class)
            // public spaces
            .withBean(DefaultGetPublicSpacesUseCase.class)
            .withBean(DefaultGetPublicSpaceUseCase.class)
            .withBean(DefaultGetPublicSpaceForEditUseCase.class)
            .withBean(DefaultCreatePublicSpaceUseCase.class)
            .withBean(DefaultUpdatePublicSpaceUseCase.class)
            .withBean(DefaultDeletePublicSpaceUseCase.class)
            .withBean(DefaultGetReservableResourcesUseCase.class)
            .withBean(DefaultGetReservableResourcesForEditUseCase.class)
            .withBean(DefaultCreateReservableResourceUseCase.class)
            .withBean(DefaultUpdateReservableResourceUseCase.class)
            .withBean(DefaultDeleteReservableResourceUseCase.class)
            .withBean(DefaultGetReservationsUseCase.class)
            .withBean(DefaultCreateReservationUseCase.class)
            .withBean(DefaultDeleteReservationUseCase.class)
            .withBean(DefaultPublicSpaceController.class)
            .withBean(DefaultReservableResourceController.class)
            .withBean(DefaultReservationController.class)
            // system trails
            .withBean(DefaultGetSystemTrailsUseCase.class)
            .withBean(DefaultSystemTrailController.class);

    // --- city places ---

    @Test
    void cityPlacesPlatformDefaultsAreWired() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(GetCityPlacesUseCase.class);
            assertThat(context).hasSingleBean(GetCityPlaceUseCase.class);
            assertThat(context).hasSingleBean(GetCityPlaceForEditUseCase.class);
            assertThat(context).hasSingleBean(CityPlaceController.class);
            assertThat(context.getBean(GetCityPlacesUseCase.class)).isInstanceOf(DefaultGetCityPlacesUseCase.class);
            assertThat(context.getBean(CityPlaceController.class)).isInstanceOf(DefaultCityPlaceController.class);
        });
    }

    @Test
    void cityPlacesUseCaseOverrideWins() {
        runner.withUserConfiguration(CityPlaceUseCaseOverride.class).run(context -> {
            assertThat(context).hasSingleBean(GetCityPlacesUseCase.class);
            assertThat(context).doesNotHaveBean(DefaultGetCityPlacesUseCase.class);
            assertThat(context.getBean(GetCityPlacesUseCase.class)).isInstanceOf(CityPlaceUseCaseOverride.CustomGetCityPlaces.class);
        });
    }

    @Test
    void cityPlacesControllerOverrideWins() {
        runner.withUserConfiguration(CityPlaceControllerOverride.class).run(context -> {
            assertThat(context).hasSingleBean(CityPlaceController.class);
            assertThat(context).doesNotHaveBean(DefaultCityPlaceController.class);
            assertThat(context.getBean(CityPlaceController.class)).isInstanceOf(CityPlaceControllerOverride.CustomCityPlaceController.class);
        });
    }

    // --- city routes ---

    @Test
    void cityRoutesPlatformDefaultsAreWired() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(GetCityRoutesUseCase.class);
            assertThat(context).hasSingleBean(GetCityRouteUseCase.class);
            assertThat(context).hasSingleBean(GetCityRouteForEditUseCase.class);
            assertThat(context).hasSingleBean(CityRouteController.class);
            assertThat(context.getBean(GetCityRoutesUseCase.class)).isInstanceOf(DefaultGetCityRoutesUseCase.class);
            assertThat(context.getBean(CityRouteController.class)).isInstanceOf(DefaultCityRouteController.class);
        });
    }

    @Test
    void cityRoutesUseCaseOverrideWins() {
        runner.withUserConfiguration(CityRouteUseCaseOverride.class).run(context -> {
            assertThat(context).hasSingleBean(GetCityRoutesUseCase.class);
            assertThat(context).doesNotHaveBean(DefaultGetCityRoutesUseCase.class);
            assertThat(context.getBean(GetCityRoutesUseCase.class)).isInstanceOf(CityRouteUseCaseOverride.CustomGetCityRoutes.class);
        });
    }

    @Test
    void cityRoutesControllerOverrideWins() {
        runner.withUserConfiguration(CityRouteControllerOverride.class).run(context -> {
            assertThat(context).hasSingleBean(CityRouteController.class);
            assertThat(context).doesNotHaveBean(DefaultCityRouteController.class);
            assertThat(context.getBean(CityRouteController.class)).isInstanceOf(CityRouteControllerOverride.CustomCityRouteController.class);
        });
    }

    // --- public spaces ---

    @Test
    void publicSpacesPlatformDefaultsAreWired() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(GetPublicSpacesUseCase.class);
            assertThat(context).hasSingleBean(GetPublicSpaceUseCase.class);
            assertThat(context).hasSingleBean(PublicSpaceController.class);
            assertThat(context).hasSingleBean(ReservableResourceController.class);
            assertThat(context).hasSingleBean(ReservationController.class);
            assertThat(context.getBean(GetPublicSpacesUseCase.class)).isInstanceOf(DefaultGetPublicSpacesUseCase.class);
            assertThat(context.getBean(PublicSpaceController.class)).isInstanceOf(DefaultPublicSpaceController.class);
            assertThat(context.getBean(ReservableResourceController.class)).isInstanceOf(DefaultReservableResourceController.class);
            assertThat(context.getBean(ReservationController.class)).isInstanceOf(DefaultReservationController.class);
        });
    }

    @Test
    void publicSpacesUseCaseOverrideWins() {
        runner.withUserConfiguration(PublicSpaceUseCaseOverride.class).run(context -> {
            assertThat(context).hasSingleBean(GetPublicSpacesUseCase.class);
            assertThat(context).doesNotHaveBean(DefaultGetPublicSpacesUseCase.class);
            assertThat(context.getBean(GetPublicSpacesUseCase.class)).isInstanceOf(PublicSpaceUseCaseOverride.CustomGetPublicSpaces.class);
        });
    }

    @Test
    void publicSpacesControllerOverrideWins() {
        runner.withUserConfiguration(PublicSpaceControllerOverride.class).run(context -> {
            assertThat(context).hasSingleBean(PublicSpaceController.class);
            assertThat(context).doesNotHaveBean(DefaultPublicSpaceController.class);
            assertThat(context.getBean(PublicSpaceController.class)).isInstanceOf(PublicSpaceControllerOverride.CustomPublicSpaceController.class);
        });
    }

    // --- system trails ---

    @Test
    void systemTrailsPlatformDefaultsAreWired() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(GetSystemTrailsUseCase.class);
            assertThat(context).hasSingleBean(SystemTrailController.class);
            assertThat(context.getBean(GetSystemTrailsUseCase.class)).isInstanceOf(DefaultGetSystemTrailsUseCase.class);
            assertThat(context.getBean(SystemTrailController.class)).isInstanceOf(DefaultSystemTrailController.class);
        });
    }

    // --- override configurations ---

    @Configuration
    static class CityPlaceUseCaseOverride {
        @Bean
        GetCityPlacesUseCase<CityPlaceDto> customGetCityPlaces() {
            return new CustomGetCityPlaces();
        }

        static class CustomGetCityPlaces implements GetCityPlacesUseCase<CityPlaceDto> {
            @Override
            public Page<CityPlaceDto> execute(String category, int page, String locale) {
                return Page.empty();
            }
        }
    }

    @Configuration
    static class CityPlaceControllerOverride {
        @Bean
        CityPlaceController<CityPlaceDto, CityPlaceRequestDto> customCityPlaceController(
                GetCityPlacesUseCase<CityPlaceDto> getCityPlacesUseCase,
                GetCityPlaceUseCase<CityPlaceDto> getCityPlaceUseCase,
                GetCityPlaceForEditUseCase<CityPlaceDto> getCityPlaceForEditUseCase,
                CreateCityPlaceUseCase<CityPlaceDto, CityPlaceRequestDto> createCityPlaceUseCase,
                UpdateCityPlaceUseCase<CityPlaceDto, CityPlaceRequestDto> updateCityPlaceUseCase,
                DeleteCityPlaceUseCase deleteCityPlaceUseCase) {
            return new CustomCityPlaceController(getCityPlacesUseCase, getCityPlaceUseCase, getCityPlaceForEditUseCase,
                    createCityPlaceUseCase, updateCityPlaceUseCase, deleteCityPlaceUseCase);
        }

        static class CustomCityPlaceController extends CityPlaceController<CityPlaceDto, CityPlaceRequestDto> {
            CustomCityPlaceController(
                    GetCityPlacesUseCase<CityPlaceDto> getCityPlacesUseCase,
                    GetCityPlaceUseCase<CityPlaceDto> getCityPlaceUseCase,
                    GetCityPlaceForEditUseCase<CityPlaceDto> getCityPlaceForEditUseCase,
                    CreateCityPlaceUseCase<CityPlaceDto, CityPlaceRequestDto> createCityPlaceUseCase,
                    UpdateCityPlaceUseCase<CityPlaceDto, CityPlaceRequestDto> updateCityPlaceUseCase,
                    DeleteCityPlaceUseCase deleteCityPlaceUseCase) {
                super(getCityPlacesUseCase, getCityPlaceUseCase, getCityPlaceForEditUseCase,
                        createCityPlaceUseCase, updateCityPlaceUseCase, deleteCityPlaceUseCase);
            }
        }
    }

    @Configuration
    static class CityRouteUseCaseOverride {
        @Bean
        GetCityRoutesUseCase<CityRouteSummaryDto> customGetCityRoutes() {
            return new CustomGetCityRoutes();
        }

        static class CustomGetCityRoutes implements GetCityRoutesUseCase<CityRouteSummaryDto> {
            @Override
            public Page<CityRouteSummaryDto> execute(int page, String locale) {
                return Page.empty();
            }
        }
    }

    @Configuration
    static class CityRouteControllerOverride {
        @Bean
        CityRouteController<CityRouteDto, CityRouteSummaryDto, CityRouteRequestDto,
                CityPlaceDto, CityPlaceSummaryDto> customCityRouteController(
                GetCityRoutesUseCase<CityRouteSummaryDto> getCityRoutesUseCase,
                GetCityRouteUseCase<CityRouteDto> getCityRouteUseCase,
                GetCityRouteForEditUseCase<CityRouteDto> getCityRouteForEditUseCase,
                GetCityRoutePlacesUseCase<CityPlaceSummaryDto> getCityRoutePlacesUseCase,
                GetCityRoutePlaceUseCase<CityPlaceDto> getCityRoutePlaceUseCase,
                CreateCityRouteUseCase<CityRouteDto, CityRouteRequestDto> createCityRouteUseCase,
                UpdateCityRouteUseCase<CityRouteDto, CityRouteRequestDto> updateCityRouteUseCase,
                DeleteCityRouteUseCase deleteCityRouteUseCase) {
            return new CustomCityRouteController(getCityRoutesUseCase, getCityRouteUseCase, getCityRouteForEditUseCase,
                    getCityRoutePlacesUseCase, getCityRoutePlaceUseCase, createCityRouteUseCase,
                    updateCityRouteUseCase, deleteCityRouteUseCase);
        }

        static class CustomCityRouteController extends CityRouteController<
                CityRouteDto, CityRouteSummaryDto, CityRouteRequestDto, CityPlaceDto, CityPlaceSummaryDto> {
            CustomCityRouteController(
                    GetCityRoutesUseCase<CityRouteSummaryDto> getCityRoutesUseCase,
                    GetCityRouteUseCase<CityRouteDto> getCityRouteUseCase,
                    GetCityRouteForEditUseCase<CityRouteDto> getCityRouteForEditUseCase,
                    GetCityRoutePlacesUseCase<CityPlaceSummaryDto> getCityRoutePlacesUseCase,
                    GetCityRoutePlaceUseCase<CityPlaceDto> getCityRoutePlaceUseCase,
                    CreateCityRouteUseCase<CityRouteDto, CityRouteRequestDto> createCityRouteUseCase,
                    UpdateCityRouteUseCase<CityRouteDto, CityRouteRequestDto> updateCityRouteUseCase,
                    DeleteCityRouteUseCase deleteCityRouteUseCase) {
                super(getCityRoutesUseCase, getCityRouteUseCase, getCityRouteForEditUseCase,
                        getCityRoutePlacesUseCase, getCityRoutePlaceUseCase,
                        createCityRouteUseCase, updateCityRouteUseCase, deleteCityRouteUseCase);
            }
        }
    }

    @Configuration
    static class PublicSpaceUseCaseOverride {
        @Bean
        GetPublicSpacesUseCase<PublicSpaceSummaryDto> customGetPublicSpaces() {
            return new CustomGetPublicSpaces();
        }

        static class CustomGetPublicSpaces implements GetPublicSpacesUseCase<PublicSpaceSummaryDto> {
            @Override
            public Page<PublicSpaceSummaryDto> execute(int page, String locale) {
                return Page.empty();
            }
        }
    }

    @Configuration
    static class PublicSpaceControllerOverride {
        @Bean
        PublicSpaceController<PublicSpaceDto, PublicSpaceSummaryDto, PublicSpaceRequestDto> customPublicSpaceController(
                GetPublicSpacesUseCase<PublicSpaceSummaryDto> getPublicSpacesUseCase,
                GetPublicSpaceUseCase<PublicSpaceDto> getPublicSpaceUseCase,
                GetPublicSpaceForEditUseCase<PublicSpaceDto> getPublicSpaceForEditUseCase,
                CreatePublicSpaceUseCase<PublicSpaceDto, PublicSpaceRequestDto> createPublicSpaceUseCase,
                UpdatePublicSpaceUseCase<PublicSpaceDto, PublicSpaceRequestDto> updatePublicSpaceUseCase,
                DeletePublicSpaceUseCase deletePublicSpaceUseCase) {
            return new CustomPublicSpaceController(getPublicSpacesUseCase, getPublicSpaceUseCase,
                    getPublicSpaceForEditUseCase, createPublicSpaceUseCase, updatePublicSpaceUseCase,
                    deletePublicSpaceUseCase);
        }

        static class CustomPublicSpaceController extends PublicSpaceController<PublicSpaceDto, PublicSpaceSummaryDto, PublicSpaceRequestDto> {
            CustomPublicSpaceController(
                    GetPublicSpacesUseCase<PublicSpaceSummaryDto> getPublicSpacesUseCase,
                    GetPublicSpaceUseCase<PublicSpaceDto> getPublicSpaceUseCase,
                    GetPublicSpaceForEditUseCase<PublicSpaceDto> getPublicSpaceForEditUseCase,
                    CreatePublicSpaceUseCase<PublicSpaceDto, PublicSpaceRequestDto> createPublicSpaceUseCase,
                    UpdatePublicSpaceUseCase<PublicSpaceDto, PublicSpaceRequestDto> updatePublicSpaceUseCase,
                    DeletePublicSpaceUseCase deletePublicSpaceUseCase) {
                super(getPublicSpacesUseCase, getPublicSpaceUseCase, getPublicSpaceForEditUseCase,
                        createPublicSpaceUseCase, updatePublicSpaceUseCase, deletePublicSpaceUseCase);
            }
        }
    }
}
