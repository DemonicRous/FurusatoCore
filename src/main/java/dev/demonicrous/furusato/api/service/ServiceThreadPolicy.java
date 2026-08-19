package dev.demonicrous.furusato.api.service;

public enum ServiceThreadPolicy {
    THREAD_SAFE,
    CLIENT_THREAD_ONLY,
    SERVER_THREAD_ONLY,
    IMMUTABLE
}
