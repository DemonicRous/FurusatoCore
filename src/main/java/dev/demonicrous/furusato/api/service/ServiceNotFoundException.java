package dev.demonicrous.furusato.api.service;

public final class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException(Class<?> service) {
        super("Service is not registered: " + service.getName());
    }
}
