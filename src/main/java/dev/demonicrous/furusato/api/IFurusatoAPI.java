package dev.demonicrous.furusato.api;

import dev.demonicrous.furusato.api.bootstrap.BootstrapReport;
import dev.demonicrous.furusato.api.module.IModuleManager;
import dev.demonicrous.furusato.api.service.IServiceRegistry;

public interface IFurusatoAPI {
    String version();

    int apiVersion();

    CoreState state();

    BootstrapReport bootstrapReport();

    IModuleManager modules();

    IServiceRegistry services();
}
