package dev.demonicrous.furusato.api.module;

import java.util.Collection;
import java.util.Optional;

public interface IModuleManager {
    void register(IFurusatoModule module);

    Optional<ModuleContainer> find(String moduleId);

    Collection<ModuleContainer> containers();
}
