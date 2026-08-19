package dev.demonicrous.furusato.api.module;

public interface IFurusatoModule {
    ModuleMetadata metadata();

    void onLoad(ModuleContext context) throws Exception;

    void onEnable() throws Exception;

    void onDisable() throws Exception;
}
