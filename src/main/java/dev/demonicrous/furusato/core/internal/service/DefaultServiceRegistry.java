package dev.demonicrous.furusato.core.internal.service;

import dev.demonicrous.furusato.api.service.IServiceRegistry;
import dev.demonicrous.furusato.api.service.ServiceNotFoundException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class DefaultServiceRegistry implements IServiceRegistry {
    private Map<Class<?>, Object> services = new LinkedHashMap<Class<?>, Object>();
    private boolean frozen;

    @Override
    public synchronized <T> void register(Class<T> service, T implementation) {
        if (frozen) {
            throw new IllegalStateException("Service registry is frozen");
        }
        if (service == null || implementation == null) {
            throw new NullPointerException("service and implementation are required");
        }
        if (!service.isInstance(implementation)) {
            throw new IllegalArgumentException("Implementation does not implement " + service.getName());
        }
        if (services.containsKey(service)) {
            throw new IllegalStateException("Service is already registered: " + service.getName());
        }
        services.put(service, implementation);
    }

    @Override
    public <T> T get(Class<T> service) {
        return find(service).orElseThrow(() -> new ServiceNotFoundException(service));
    }

    @Override
    public <T> Optional<T> find(Class<T> service) {
        return Optional.ofNullable(service.cast(services.get(service)));
    }

    @Override
    public boolean isFrozen() {
        return frozen;
    }

    public synchronized void freeze() {
        if (!frozen) {
            services = Collections.unmodifiableMap(new LinkedHashMap<Class<?>, Object>(services));
            frozen = true;
        }
    }
}
