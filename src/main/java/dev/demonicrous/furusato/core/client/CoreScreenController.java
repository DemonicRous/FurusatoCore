package dev.demonicrous.furusato.core.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public final class CoreScreenController {
    private static final KeyBinding OPEN_STATUS = new KeyBinding(
            "key.furusatocore.open_status", Keyboard.KEY_F8, "key.categories.furusatocore");
    private static boolean initialized;

    private CoreScreenController() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        ClientRegistry.registerKeyBinding(OPEN_STATUS);
        MinecraftForge.EVENT_BUS.register(new CoreScreenController());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !OPEN_STATUS.isPressed()) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.currentScreen instanceof FurusatoStatusScreen) {
            minecraft.displayGuiScreen(null);
        } else {
            minecraft.displayGuiScreen(new FurusatoStatusScreen());
        }
    }
}
