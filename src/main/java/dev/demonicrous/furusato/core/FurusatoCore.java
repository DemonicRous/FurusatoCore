package dev.demonicrous.furusato.core;

import dev.demonicrous.furusato.api.FurusatoAPI;
import dev.demonicrous.furusato.api.bootstrap.BootstrapReport;
import dev.demonicrous.furusato.api.bootstrap.BootstrapStage;
import dev.demonicrous.furusato.core.bootstrap.BootstrapTracker;
import dev.demonicrous.furusato.core.internal.FurusatoApiImpl;
import dev.demonicrous.furusato.core.module.CoreRuntimeModule;
import dev.demonicrous.furusato.core.module.ModuleBootstrapException;
import dev.demonicrous.furusato.core.platform.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
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

    @SidedProxy(
            clientSide = "dev.demonicrous.furusato.core.platform.ClientProxy",
            serverSide = "dev.demonicrous.furusato.core.platform.CommonProxy"
    )
    public static CommonProxy proxy;

    private final BootstrapTracker bootstrapTracker;
    private final FurusatoApiImpl api;
    private Logger logger;

    public FurusatoCore() {
        bootstrapTracker = new BootstrapTracker();
        bootstrapTracker.begin(BootstrapStage.CONSTRUCT);
        api = new FurusatoApiImpl(VERSION, bootstrapTracker);
        api.starting();
        api.internalModules().register(new CoreRuntimeModule());
        bootstrapTracker.end(BootstrapStage.CONSTRUCT);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        bootstrapTracker.begin(BootstrapStage.PRE_INIT);
        try {
            logger = event.getModLog();
            FurusatoAPI.install(api);
            logger.info("FurusatoCore {} starting (API {})", VERSION, FurusatoAPI.API_VERSION);
        } finally {
            bootstrapTracker.end(BootstrapStage.PRE_INIT);
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        bootstrapTracker.begin(BootstrapStage.INIT);
        try {
            api.internalModules().loadAll();
            proxy.initializeClientFeatures();
            logger.info("FurusatoCore initialization complete");
        } catch (ModuleBootstrapException failure) {
            api.failed();
            logger.error("Required FurusatoCore module failed during bootstrap", failure);
            throw failure;
        } finally {
            bootstrapTracker.end(BootstrapStage.INIT);
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        bootstrapTracker.begin(BootstrapStage.POST_INIT);
        try {
            logger.info("FurusatoCore post-initialization complete");
        } finally {
            bootstrapTracker.end(BootstrapStage.POST_INIT);
        }
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        bootstrapTracker.begin(BootstrapStage.LOAD_COMPLETE);
        bootstrapTracker.end(BootstrapStage.LOAD_COMPLETE);
        api.available();

        BootstrapReport report = api.bootstrapReport();
        logger.info("FurusatoCore {} ready; Core bootstrap {} ms, elapsed {} ms", VERSION,
                String.format(java.util.Locale.ROOT, "%.3f", report.totalMillis()),
                String.format(java.util.Locale.ROOT, "%.3f", report.elapsedMillis()));
        for (BootstrapStage stage : BootstrapStage.values()) {
            if (report.stageNanos(stage).isPresent()) {
                logger.info("Bootstrap {}: {} ms", stage,
                        String.format(java.util.Locale.ROOT, "%.3f",
                                report.stageNanos(stage).getAsLong() / 1_000_000.0D));
            }
        }
        api.modules().containers().forEach(module -> logger.info(
                "Module {} {}: {} ({})",
                module.metadata().id(), module.metadata().version(),
                module.state(), module.statusDetail()));
        api.services().snapshots().forEach(service -> logger.info(
                "Service {}: {} owner={} policy={} consumers={}",
                service.id(), service.status(), service.ownerModuleId(),
                service.threadPolicy(), service.consumers().size()));
    }

}
