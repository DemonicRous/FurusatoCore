package dev.demonicrous.furusato.core.command;

import dev.demonicrous.furusato.api.FurusatoAPI;
import dev.demonicrous.furusato.api.module.ModuleContainer;
import java.util.Collections;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

public final class FurusatoCommand extends CommandBase {
    @Override
    public String getName() {
        return "furusato";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/furusato <info|modules>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length > 0 && "modules".equalsIgnoreCase(args[0])) {
            showModules(sender);
            return;
        }
        sender.sendMessage(new TextComponentTranslation(
                "furusatocore.command.info",
                FurusatoAPI.get().version(),
                FurusatoAPI.get().apiVersion(),
                FurusatoAPI.get().state().name(),
                String.format(java.util.Locale.ROOT, "%.3f",
                        FurusatoAPI.get().bootstrapReport().totalMillis())));
    }

    private void showModules(ICommandSender sender) {
        int enabled = 0;
        for (ModuleContainer module : FurusatoAPI.get().modules().containers()) {
            if (module.state() == dev.demonicrous.furusato.api.module.ModuleState.ENABLED) {
                enabled++;
            }
        }
        sender.sendMessage(new TextComponentTranslation(
                "furusatocore.command.modules.header",
                enabled, FurusatoAPI.get().modules().containers().size()));
        for (ModuleContainer module : FurusatoAPI.get().modules().containers()) {
            sender.sendMessage(new TextComponentTranslation(
                    "furusatocore.command.modules.entry",
                    module.metadata().name(),
                    module.metadata().id(),
                    module.metadata().version(),
                    module.state().name(),
                    module.statusDetail()));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender,
            String[] args, BlockPos targetPos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, "info", "modules")
                : Collections.<String>emptyList();
    }
}
