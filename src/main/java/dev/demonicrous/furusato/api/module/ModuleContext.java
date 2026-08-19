package dev.demonicrous.furusato.api.module;

import dev.demonicrous.furusato.api.service.IServiceRegistry;

public final class ModuleContext {
    private final IServiceRegistry services;

    public ModuleContext(IServiceRegistry services) {
        if (services == null) {
            throw new NullPointerException("services");
        }
        this.services = services;
    }

    public IServiceRegistry services() {
        return services;
    }
}
