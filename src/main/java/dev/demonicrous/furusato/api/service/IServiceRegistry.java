package dev.demonicrous.furusato.api.service;

import java.util.Collection;
import java.util.Optional;

public interface IServiceRegistry {
    <T> void register(Class<T> service, T implementation);

    <T> void register(ServiceMetadata<T> metadata, T implementation);

    <T> T get(Class<T> service);

    <T> Optional<T> find(Class<T> service);

    <T> T require(Class<T> service, String consumerModuleId);

    Collection<ServiceSnapshot> snapshots();

    int size();

    boolean isFrozen();
}
