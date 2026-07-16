package com.demonicrous.furusato.core.asm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Installs Furusato's early Forge splash resources before SplashProgress starts. */
final class SplashThemeInstaller {
    private static final Logger LOGGER = LogManager.getLogger("Furusato Core/Splash");
    private static final String THEME_VERSION = "1";
    private static final String RESOURCE_PATH =
            "/assets/furusatocore/textures/gui/loading_animation.png";

    private SplashThemeInstaller() {
    }

    static void install(File gameDirectory) {
        if (gameDirectory == null) {
            LOGGER.warn("Game directory was not supplied; the Furusato splash theme is disabled");
            return;
        }

        try {
            File texture = new File(gameDirectory,
                    "resources/assets/furusatocore/textures/gui/loading_animation.png");
            copyTexture(texture);
            configureSplash(new File(gameDirectory, "config/splash.properties"));
        } catch (IOException error) {
            LOGGER.error("Could not install the Furusato splash theme", error);
        }
    }

    private static void copyTexture(File target) throws IOException {
        File parent = target.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Could not create splash resource directory: " + parent);
        }

        try (InputStream input = SplashThemeInstaller.class.getResourceAsStream(RESOURCE_PATH)) {
            if (input == null) {
                throw new IOException("Missing bundled splash texture: " + RESOURCE_PATH);
            }
            Files.copy(input, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void configureSplash(File file) throws IOException {
        File parent = file.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Could not create config directory: " + parent);
        }

        Properties properties = new Properties();
        if (file.isFile()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        }

        if (!THEME_VERSION.equals(properties.getProperty("furusatoThemeVersion"))) {
            putIfMissing(properties, "enabled", "true");
            properties.setProperty("rotate", "false");
            properties.setProperty("showMemory", "false");
            properties.setProperty("logoOffset", "16");
            properties.setProperty("background", "0x050816");
            properties.setProperty("font", "0xE7E5E4");
            properties.setProperty("barBorder", "0x78716C");
            properties.setProperty("bar", "0xD9913D");
            properties.setProperty("barBackground", "0x111827");
            properties.setProperty("resourcePackPath", "resources");
            properties.setProperty("forgeTexture",
                    "furusatocore:textures/gui/loading_animation.png");
            properties.setProperty("furusatoThemeVersion", THEME_VERSION);
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            properties.store(writer, "Forge splash screen properties - themed by Furusato Core");
        }
    }

    private static void putIfMissing(Properties properties, String key, String value) {
        if (!properties.containsKey(key)) {
            properties.setProperty(key, value);
        }
    }
}
