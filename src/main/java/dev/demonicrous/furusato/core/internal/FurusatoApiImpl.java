package dev.demonicrous.furusato.core.internal;

import dev.demonicrous.furusato.api.CoreState;
import dev.demonicrous.furusato.api.FurusatoAPI;
import dev.demonicrous.furusato.api.IFurusatoAPI;
import dev.demonicrous.furusato.api.service.IServiceRegistry;
import dev.demonicrous.furusato.core.internal.service.DefaultServiceRegistry;

public final class FurusatoApiImpl implements IFurusatoAPI {
    private final String version;
    private final DefaultServiceRegistry services = new DefaultServiceRegistry();
    private volatile CoreState state = CoreState.NEW;

    public FurusatoApiImpl(String version) {
        this.version = version;
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
    public IServiceRegistry services() {
        return services;
    }

    public void starting() {
        state = CoreState.STARTING;
    }

    public void available() {
        services.freeze();
        state = CoreState.AVAILABLE;
    }
}
