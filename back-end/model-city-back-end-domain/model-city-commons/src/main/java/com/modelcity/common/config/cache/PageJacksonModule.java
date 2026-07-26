package com.modelcity.common.config.cache;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.type.WritableTypeId;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * Jackson 3 module that gives {@link PageImpl} a stable, round-trippable JSON shape so Spring Data
 * {@code Page<T>} results can be cached in Valkey. {@code PageImpl} has no Jackson-friendly constructor,
 * so a custom serializer/deserializer pair is used. Element types survive thanks to the default typing
 * configured on the serializer's mapper.
 */
public class PageJacksonModule extends SimpleModule {

    public PageJacksonModule() {
        addSerializer(PageImpl.class, new PageImplSerializer());
        addDeserializer(PageImpl.class, new PageImplDeserializer());
    }

    private static final class PageImplSerializer extends ValueSerializer<PageImpl> {

        @Override
        public void serialize(PageImpl page, JsonGenerator gen, SerializationContext ctxt) {
            gen.writeStartObject();
            writeFields(page, gen);
            gen.writeEndObject();
        }

        @Override
        public void serializeWithType(PageImpl page, JsonGenerator gen, SerializationContext ctxt,
                TypeSerializer typeSer) {
            WritableTypeId typeId = typeSer.typeId(page, JsonToken.START_OBJECT);
            typeSer.writeTypePrefix(gen, ctxt, typeId);
            writeFields(page, gen);
            typeSer.writeTypeSuffix(gen, ctxt, typeId);
        }

        private void writeFields(PageImpl page, JsonGenerator gen) {
            gen.writePOJOProperty("content", page.getContent());
            gen.writeNumberProperty("number", page.getNumber());
            gen.writeNumberProperty("size", page.getSize());
            gen.writeNumberProperty("totalElements", page.getTotalElements());
        }
    }

    private static final class PageImplDeserializer extends ValueDeserializer<PageImpl> {

        @Override
        public PageImpl deserialize(JsonParser p, DeserializationContext ctxt) {
            JsonNode node = ctxt.readTree(p);

            // The content list is written with the serializer's default typing, so let Jackson reconstruct
            // it: each element regains its concrete type from its embedded type id.
            List<Object> content = new ArrayList<>();
            JsonNode contentNode = node.get("content");
            if (contentNode != null && !contentNode.isNull()) {
                Object decoded = ctxt.readTreeAsValue(contentNode, Object.class);
                if (decoded instanceof List<?> list) {
                    content.addAll(list);
                }
            }

            int number = node.path("number").asInt(0);
            int size = node.path("size").asInt(0);
            long totalElements = node.path("totalElements").asLong(content.size());

            Pageable pageable = size > 0 ? PageRequest.of(number, size) : Pageable.unpaged();
            return new PageImpl<>(content, pageable, totalElements);
        }
    }
}
