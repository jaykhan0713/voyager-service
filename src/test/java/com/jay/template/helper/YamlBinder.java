package com.jay.template.helper;

import java.io.IOException;
import java.util.List;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

public final class YamlBinder {

    private static final String APP_YAML = "unittest.yml";

    private final Binder binder;

    public YamlBinder() throws IOException {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        ClassPathResource resource = new ClassPathResource(APP_YAML);

        List<PropertySource<?>> sources = loader.load("testAppYaml", resource);

        ConfigurableEnvironment env = new StandardEnvironment();
        for (PropertySource<?> source : sources) {
            env.getPropertySources().addLast(source);
        }

        this.binder = Binder.get(env);
    }

    public <T> T bind(String name, Class<T> type) throws Exception {
        return binder.bind(name, Bindable.of(type))
                .orElseThrow(() -> new Exception("Failed to bind " + name));
    }
}
