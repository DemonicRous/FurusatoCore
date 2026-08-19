package dev.demonicrous.furusato.core.internal;

import dev.demonicrous.furusato.api.CoreState;
import dev.demonicrous.furusato.api.FurusatoAPI;
import dev.demonicrous.furusato.api.IFurusatoAPI;
import dev.demonicrous.furusato.api.bootstrap.BootstrapReport;
import dev.demonicrous.furusato.api.service.IServiceRegistry;
import dev.demonicrous.furusato.core.bootstrap.BootstrapTracker;
import dev.demonicrous.furusato.core.internal.service.DefaultServiceRegistry;

public final class FurusatoApiImpl implements IFurusatoAPI {
    private final String version;
    private final BootstrapTracker bootstrapTracker;
    private final DefaultServiceRegistry services = new DefaultServiceRegistry();
    private volatile CoreState state = CoreState.NEW;

    public FurusatoApiImpl(String version, BootstrapTracker bootstrapTracker) {
        this.version = version;
        this.bootstrapTracker = bootstrapTracker;
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public int apiVersion() {
        return FurusatoAPI.API_VERSION;
    }

    @Override
    public CoreState state() {
        return state;
    }

    @Override
    public BootstrapReport bootstrapReport() {
        return bootstrapTracker.snapshot();
    }

    @Override
    public IServiceRegistry services() {
        return services;
    }

    public void starting() {
        transition(CoreState.NEW, CoreState.STARTING);
    }

    public void available() {
        services.freeze();
        transition(CoreState.STARTING, CoreState.AVAILABLE);
    }

    private synchronized void transition(CoreState expected, CoreState next) {
        if (state != expected) {
            throw new IllegalStateException(
                    "Invalid Core state transition: " + state + " -> " + next);
        }
        state = next;
    }
}
