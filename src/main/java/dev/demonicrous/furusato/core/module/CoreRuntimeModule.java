package dev.demonicrous.furusato.core.module;

import dev.demonicrous.furusato.api.module.IFurusatoModule;
import dev.demonicrous.furusato.api.module.ModuleContext;
import dev.demonicrous.furusato.api.module.ModuleMetadata;
import dev.demonicrous.furusato.core.FurusatoCore;

public final class CoreRuntimeModule implements IFurusatoModule {
    private final ModuleMetadata metadata = ModuleMetadata
            .builder("core", "Core Runtime", FurusatoCore.VERSION)
            .requiredForCore()
            .build();

    @Override
    public ModuleMetadata metadata() {
        return metadata;
    }

    @Override
    public void onLoad(ModuleContext context) {
        // The foundational module owns no optional service yet.
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }
}
