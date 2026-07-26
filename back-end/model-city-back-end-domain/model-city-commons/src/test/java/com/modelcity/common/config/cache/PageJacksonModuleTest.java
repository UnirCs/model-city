package com.modelcity.common.config.cache;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PageJacksonModuleTest {

    private record Item(Long id, String name) {
    }

    private final ObjectMapper mapper = JsonMapper.builder()
            .addModule(new PageJacksonModule())
            .build();

    @Test
    void serialize_writesContentNumberSizeAndTotalElements() {
        Page<Item> page = new PageImpl<>(
                List.of(new Item(1L, "Plaza Mayor"), new Item(2L, "Museo")),
                PageRequest.of(1, 2), 10);

        JsonNode node = mapper.readTree(mapper.writeValueAsString(page));

        assertThat(node.get("number").asInt()).isEqualTo(1);
        assertThat(node.get("size").asInt()).isEqualTo(2);
        assertThat(node.get("totalElements").asLong()).isEqualTo(10);
        assertThat(node.get("content").isArray()).isTrue();
        assertThat(node.get("content")).hasSize(2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void roundTrip_preservesNumberSizeAndTotalElements() {
        Page<Item> original = new PageImpl<>(
                List.of(new Item(1L, "Plaza Mayor"), new Item(2L, "Museo")),
                PageRequest.of(1, 2), 10);

        String json = mapper.writeValueAsString(original);
        PageImpl<Object> restored = mapper.readValue(json, PageImpl.class);

        assertThat(restored.getNumber()).isEqualTo(1);
        assertThat(restored.getSize()).isEqualTo(2);
        assertThat(restored.getTotalElements()).isEqualTo(10);
        assertThat(restored.getContent()).hasSize(2);
        assertThat(((Map<String, Object>) restored.getContent().get(0))).containsEntry("name", "Plaza Mayor");
    }

    @Test
    @SuppressWarnings("unchecked")
    void roundTrip_emptyPage_restoresZeroTotalElements() {
        Page<Item> original = new PageImpl<>(List.of(), PageRequest.of(0, 5), 0);

        String json = mapper.writeValueAsString(original);
        PageImpl<Object> restored = mapper.readValue(json, PageImpl.class);

        assertThat(restored.getContent()).isEmpty();
        assertThat(restored.getTotalElements()).isZero();
    }

    @Test
    @SuppressWarnings("unchecked")
    void deserialize_missingSize_usesUnpagedPageable() {
        String json = "{\"content\":[],\"number\":0,\"totalElements\":0}";

        PageImpl<Object> restored = mapper.readValue(json, PageImpl.class);

        assertThat(restored.getPageable().isUnpaged()).isTrue();
    }
}
