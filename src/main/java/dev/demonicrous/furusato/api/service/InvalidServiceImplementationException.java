package dev.demonicrous.furusato.api.service;

public final class InvalidServiceImplementationException extends IllegalArgumentException {
    public InvalidServiceImplementationException(Class<?> contract, Class<?> implementation) {
        super("Implementation " + implementation.getName()
                + " does not implement " + contract.getName());
    }
}
