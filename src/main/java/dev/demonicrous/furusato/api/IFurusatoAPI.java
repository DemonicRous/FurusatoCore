package dev.demonicrous.furusato.api;

import dev.demonicrous.furusato.api.service.IServiceRegistry;

public interface IFurusatoAPI {
    String version();

    int apiVersion();

    CoreState state();

    IServiceRegistry services();
}
