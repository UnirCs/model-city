package com.modelcity.leisure;

import com.modelcity.common.extensibility.ExtensionPointRules;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.Test;

/**
 * Applies the shared extensibility contract ({@link ExtensionPointRules}) to the leisure vertical. The
 * rules live in {@code model-city-commons}; this test only scopes them to {@code com.modelcity.leisure}, so
 * a future vertical is covered by copying this ~6-line class.
 */
class LeisureExtensibilityArchTest {

    private static final JavaClasses CLASSES = ExtensionPointRules.importVertical("com.modelcity.leisure");

    @Test
    void defaultsAndSeamsAreCityNeutral() {
        ExtensionPointRules.NO_CITY_NAMES.check(CLASSES);
    }

    @Test
    void markerOnlyMarksSeams() {
        ExtensionPointRules.MARKER_ONLY_ON_SEAMS.check(CLASSES);
    }

    @Test
    void defaultsAreWiredThroughASeam() {
        ExtensionPointRules.DEFAULTS_WIRED_THROUGH_SEAM.check(CLASSES);
    }

    @Test
    void defaultsAreScannedAndDisableable() {
        ExtensionPointRules.DEFAULTS_ARE_SCANNED_AND_DISABLEABLE.check(CLASSES);
    }

    @Test
    void disabledMarkerOnlyMarksConcreteDefaults() {
        ExtensionPointRules.DISABLED_MARKER_ONLY_ON_CONCRETE_DEFAULTS.check(CLASSES);
    }

    @Test
    void useCaseSeamsExposeOneMethod() {
        ExtensionPointRules.USE_CASE_SEAMS_ARE_SINGLE_METHOD.check(CLASSES);
    }

    @Test
    void storePortsAreSeams() {
        ExtensionPointRules.STORE_PORTS_ARE_MARKED.check(CLASSES);
    }
}
