package dev.demonicrous.furusato.api.service;

public final class DuplicateServiceException extends IllegalStateException {
    public DuplicateServiceException(String message) {
        super(message);
    }
}
