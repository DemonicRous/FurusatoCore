package dev.demonicrous.furusato.api.service;

import java.util.Optional;

public interface IServiceRegistry {
    <T> void register(Class<T> service, T implementation);

    <T> T get(Class<T> service);

    <T> Optional<T> find(Class<T> service);

    boolean isFrozen();
}
