package com.modelcity.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhotoUrlsTest {

    @Test
    void collect_allPresent_preservesOrder() {
        assertThat(PhotoUrls.collect("a.jpg", "b.jpg", "c.jpg"))
                .containsExactly("a.jpg", "b.jpg", "c.jpg");
    }

    @Test
    void collect_allNull_returnsEmptyList() {
        assertThat(PhotoUrls.collect(null, null, null)).isEmpty();
    }

    @Test
    void collect_middleNull_skipsNullEntries() {
        assertThat(PhotoUrls.collect("a.jpg", null, "c.jpg")).containsExactly("a.jpg", "c.jpg");
    }

    @Test
    void collect_onlyFirst_returnsSingleEntry() {
        assertThat(PhotoUrls.collect("a.jpg", null, null)).containsExactly("a.jpg");
    }
}
