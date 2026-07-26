package com.modelcity.core.utils.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.Map;

/** Loads an HTML file from the classpath and replaces {{PLACEHOLDER}} tokens. */
@Slf4j
public class ClasspathHtmlMailTemplate<T> implements MailTemplate<T> {

    private final String html;
    private final PlaceholderResolver<T> placeholderResolver;

    public ClasspathHtmlMailTemplate(Resource resource, PlaceholderResolver<T> placeholderResolver) {
        try {
            this.html = resource.getContentAsString(StandardCharsets.UTF_8);
            this.placeholderResolver = placeholderResolver;
            log.info("Mail template loaded from {}", resource.getDescription());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load mail template: " + resource.getDescription(), e);
        }
    }

    @Override
    public String render(T context) {
        String rendered = html;
        Map<String, String> placeholders = placeholderResolver.resolve(context);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            rendered = rendered.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return rendered.replace("{{YEAR}}", String.valueOf(Year.now().getValue()));
    }

    /** Resolves placeholder names to their values from the context. */
    @FunctionalInterface
    public interface PlaceholderResolver<T> {
        Map<String, String> resolve(T context);
    }
}
