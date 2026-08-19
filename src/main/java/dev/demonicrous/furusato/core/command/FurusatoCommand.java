package dev.demonicrous.furusato.core.command;

import dev.demonicrous.furusato.api.FurusatoAPI;
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
        return "/furusato info";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        sender.sendMessage(new TextComponentTranslation(
                "furusatocore.command.info",
                FurusatoAPI.get().version(),
                FurusatoAPI.get().apiVersion(),
                FurusatoAPI.get().state().name()));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender,
            String[] args, BlockPos targetPos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, "info")
                : Collections.<String>emptyList();
    }
}
