package com.demonicrous.furusato.core;

import com.demonicrous.furusato.core.config.FurusatoCoreConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = FurusatoCore.MOD_ID,
        name = FurusatoCore.NAME,
        version = FurusatoCore.VERSION,
        acceptedMinecraftVersions = "[1.12.2]",
        dependencies = "required-after:forge@[14.23.5.2847,)"
)
public final class FurusatoCore {
    public static final String MOD_ID = "furusatocore";
    public static final String NAME = "Furusato Core";
    public static final String VERSION = "@VERSION@";

    private static Logger logger;

    public static Logger getLogger() {
        if (logger == null) {
            throw new IllegalStateException("Furusato Core is not initialized yet");
        }
        return logger;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        FurusatoCoreConfig.load(event.getSuggestedConfigurationFile());
        logger.info("Furusato Core {} pre-initialized", VERSION);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("Furusato Core initialized");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        logger.info("Furusato Core is ready");
    }
}
