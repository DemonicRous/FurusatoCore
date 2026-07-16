package com.demonicrous.furusato.core.config;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public final class FurusatoCoreConfig {
    private static final String GENERAL = Configuration.CATEGORY_GENERAL;
    private static Configuration configuration;

    private static boolean debugLogging;

    private FurusatoCoreConfig() {
    }

    public static void load(File file) {
        configuration = new Configuration(file);
        configuration.load();
        debugLogging = configuration.getBoolean(
                "debugLogging",
                GENERAL,
                false,
                "Enable additional diagnostic logging for Furusato modules."
        );

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    public static boolean isDebugLoggingEnabled() {
        return debugLogging;
    }
}
