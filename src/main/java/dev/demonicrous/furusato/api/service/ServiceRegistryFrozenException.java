package dev.demonicrous.furusato.api.service;

public final class ServiceRegistryFrozenException extends IllegalStateException {
    public ServiceRegistryFrozenException() {
        super("Service registry is frozen");
    }
}
