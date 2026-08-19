package dev.demonicrous.furusato.core.platform;

import dev.demonicrous.furusato.core.client.CoreScreenController;

public final class ClientProxy extends CommonProxy {
    @Override
    public void initializeClientFeatures() {
        CoreScreenController.initialize();
    }
}
