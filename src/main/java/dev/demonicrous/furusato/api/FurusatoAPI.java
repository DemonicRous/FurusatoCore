package dev.demonicrous.furusato.api;

public final class FurusatoAPI {
    public static final int API_VERSION = 1;
    private static volatile IFurusatoAPI instance;

    private FurusatoAPI() {
    }

    public static IFurusatoAPI get() {
        IFurusatoAPI current = instance;
        if (current == null) {
            throw new IllegalStateException("FurusatoCore API is not available yet");
        }
        return current;
    }

    public static boolean isAvailable() {
        return instance != null && instance.state() == CoreState.AVAILABLE;
    }

    /** Internal bootstrap entry point. Extensions must never call this method. */
    public static synchronized void install(IFurusatoAPI implementation) {
        if (implementation == null) {
            throw new NullPointerException("implementation");
        }
        if (instance != null) {
            throw new IllegalStateException("FurusatoCore API is already installed");
        }
        instance = implementation;
    }
}
