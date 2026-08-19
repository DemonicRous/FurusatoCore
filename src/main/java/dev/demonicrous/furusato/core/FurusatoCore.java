package dev.demonicrous.furusato.core;

import dev.demonicrous.furusato.api.FurusatoAPI;
import dev.demonicrous.furusato.core.command.FurusatoCommand;
import dev.demonicrous.furusato.core.internal.FurusatoApiImpl;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = FurusatoCore.MOD_ID,
        name = FurusatoCore.NAME,
        version = FurusatoCore.VERSION,
        acceptedMinecraftVersions = "[1.12.2]"
)
public final class FurusatoCore {
    public static final String MOD_ID = "furusatocore";
    public static final String NAME = "FurusatoCore";
    public static final String VERSION = "@VERSION@";

    private FurusatoApiImpl api;
    private Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        api = new FurusatoApiImpl(VERSION);
        api.starting();
        FurusatoAPI.install(api);
        logger.info("FurusatoCore {} starting (API {})", VERSION, FurusatoAPI.API_VERSION);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("FurusatoCore initialization complete");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        api.available();
        logger.info("FurusatoCore {} ready", VERSION);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new FurusatoCommand());
    }
}
