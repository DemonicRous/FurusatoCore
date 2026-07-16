package com.demonicrous.furusato.core.asm;

import java.io.File;
import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

/**
 * Loads Furusato's bytecode transformers before Minecraft classes are defined.
 */
@IFMLLoadingPlugin.Name("Furusato Core")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions("com.demonicrous.furusato.core.asm")
public final class FurusatoCorePlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
                ForgeSplashTransformer.class.getName(),
                UnicodeGuiScaleTransformer.class.getName()
        };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        Object gameDirectory = data.get("mcLocation");
        SplashThemeInstaller.install(gameDirectory instanceof File ? (File) gameDirectory : null);
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
