package dev.demonicrous.furusato.core.command;

import dev.demonicrous.furusato.api.FurusatoAPI;
import dev.demonicrous.furusato.api.module.ModuleContainer;
import dev.demonicrous.furusato.api.service.ServiceSnapshot;
import dev.demonicrous.furusato.api.service.ServiceStatus;
import java.util.Collections;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public final class FurusatoCommand extends CommandBase {
    @Override
    public String getName() {
        return "furusato";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/furusato <info|modules|services>";
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
        if (args.length > 0 && "services".equalsIgnoreCase(args[0])) {
            showServices(sender);
            return;
        }
        sender.sendMessage(colored(new TextComponentTranslation(
                "furusatocore.command.info",
                FurusatoAPI.get().version(),
                FurusatoAPI.get().apiVersion(),
                FurusatoAPI.get().state().name(),
                String.format(java.util.Locale.ROOT, "%.3f",
                        FurusatoAPI.get().bootstrapReport().totalMillis()),
                FurusatoAPI.get().services().size()),
                TextFormatting.AQUA));
    }

    private void showServices(ICommandSender sender) {
        sender.sendMessage(colored(new TextComponentTranslation(
                "furusatocore.command.services.header",
                FurusatoAPI.get().services().size()), TextFormatting.GOLD));
        for (ServiceSnapshot service : FurusatoAPI.get().services().snapshots()) {
            sender.sendMessage(colored(new TextComponentTranslation(
                    "furusatocore.command.services.entry",
                    service.id(),
                    service.ownerModuleId(),
                    service.threadPolicy().name(),
                    service.status().name(),
                    service.consumers().size()),
                    service.status() == ServiceStatus.AVAILABLE
                            ? TextFormatting.GREEN : TextFormatting.YELLOW));
        }
    }

    private void showModules(ICommandSender sender) {
        int enabled = 0;
        for (ModuleContainer module : FurusatoAPI.get().modules().containers()) {
            if (module.state() == dev.demonicrous.furusato.api.module.ModuleState.ENABLED) {
                enabled++;
            }
        }
        sender.sendMessage(colored(new TextComponentTranslation(
                "furusatocore.command.modules.header",
                enabled, FurusatoAPI.get().modules().containers().size()),
                TextFormatting.GOLD));
        for (ModuleContainer module : FurusatoAPI.get().modules().containers()) {
            sender.sendMessage(colored(new TextComponentTranslation(
                    "furusatocore.command.modules.entry",
                    module.metadata().name(),
                    module.metadata().id(),
                    module.metadata().version(),
                    module.state().name(),
                    module.statusDetail()), colorFor(module)));
        }
    }

    private ITextComponent colored(ITextComponent component, TextFormatting color) {
        component.getStyle().setColor(color);
        return component;
    }

    private TextFormatting colorFor(ModuleContainer module) {
        switch (module.state()) {
            case ENABLED:
                return TextFormatting.GREEN;
            case FAILED:
                return TextFormatting.RED;
            case DISABLED:
                return TextFormatting.GRAY;
            case LOADED:
                return TextFormatting.YELLOW;
            case DISCOVERED:
            default:
                return TextFormatting.BLUE;
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender,
            String[] args, BlockPos targetPos) {
        return args.length == 1
                ? getListOfStringsMatchingLastWord(args, "info", "modules", "services")
                : Collections.<String>emptyList();
    }
}
