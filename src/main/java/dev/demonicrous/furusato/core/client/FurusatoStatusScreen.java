package dev.demonicrous.furusato.core.client;

import dev.demonicrous.furusato.api.FurusatoAPI;
import dev.demonicrous.furusato.api.IFurusatoAPI;
import dev.demonicrous.furusato.api.bootstrap.BootstrapReport;
import dev.demonicrous.furusato.api.module.ModuleContainer;
import dev.demonicrous.furusato.api.module.ModuleState;
import dev.demonicrous.furusato.api.service.ServiceSnapshot;
import dev.demonicrous.furusato.api.service.ServiceStatus;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class FurusatoStatusScreen extends GuiScreen {
    private static final int TAB_OVERVIEW = 0;
    private static final int TAB_MODULES = 1;
    private static final int TAB_SERVICES = 2;
    private static final int BUTTON_CLOSE = 10;
    private static final int COLOR_TEXT = 0xFFE6E6E6;
    private static final int COLOR_MUTED = 0xFFAAAAAA;
    private static final int COLOR_GREEN = 0xFF55FF55;
    private static final int COLOR_RED = 0xFFFF5555;
    private static final int COLOR_YELLOW = 0xFFFFFF55;
    private static final int COLOR_BLUE = 0xFF55FFFF;

    private int selectedTab = TAB_OVERVIEW;
    private int scrollOffset;

    @Override
    public void initGui() {
        buttonList.clear();
        int tabWidth = Math.min(110, (width - 40) / 3);
        int tabsLeft = (width - tabWidth * 3) / 2;
        buttonList.add(new GuiButton(TAB_OVERVIEW, tabsLeft, 34, tabWidth, 20,
                I18n.format("furusatocore.screen.tab.overview")));
        buttonList.add(new GuiButton(TAB_MODULES, tabsLeft + tabWidth, 34, tabWidth, 20,
                I18n.format("furusatocore.screen.tab.modules")));
        buttonList.add(new GuiButton(TAB_SERVICES, tabsLeft + tabWidth * 2, 34, tabWidth, 20,
                I18n.format("furusatocore.screen.tab.services")));
        buttonList.add(new GuiButton(BUTTON_CLOSE, width / 2 - 50, height - 28, 100, 20,
                I18n.format("furusatocore.screen.close")));
        updateTabButtons();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id >= TAB_OVERVIEW && button.id <= TAB_SERVICES) {
            selectedTab = button.id;
            scrollOffset = 0;
            updateTabButtons();
        } else if (button.id == BUTTON_CLOSE) {
            mc.displayGuiScreen(null);
        }
    }

    private void updateTabButtons() {
        for (GuiButton button : buttonList) {
            if (button.id >= TAB_OVERVIEW && button.id <= TAB_SERVICES) {
                button.enabled = button.id != selectedTab;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, I18n.format("furusatocore.screen.title"),
                width / 2, 14, 0xFFFFFFFF);

        int left = Math.max(20, width / 2 - 230);
        int right = Math.min(width - 20, width / 2 + 230);
        int top = 60;
        int bottom = height - 34;
        drawRect(left, top, right, bottom, 0xB0101010);

        List<Line> lines = buildLines();
        int visibleLines = Math.max(1, (bottom - top - 16) / 12);
        int maxScroll = Math.max(0, lines.size() - visibleLines);
        scrollOffset = Math.min(scrollOffset, maxScroll);
        for (int i = 0; i < visibleLines && i + scrollOffset < lines.size(); i++) {
            Line line = lines.get(i + scrollOffset);
            fontRenderer.drawString(line.text, left + 10, top + 9 + i * 12, line.color);
        }
        if (maxScroll > 0) {
            drawString(fontRenderer, I18n.format("furusatocore.screen.scroll"),
                    right - 94, bottom - 12, COLOR_MUTED);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollOffset = Math.max(0, scrollOffset + (wheel < 0 ? 1 : -1));
        }
    }

    private List<Line> buildLines() {
        if (selectedTab == TAB_MODULES) {
            return moduleLines();
        }
        if (selectedTab == TAB_SERVICES) {
            return serviceLines();
        }
        return overviewLines();
    }

    private List<Line> overviewLines() {
        IFurusatoAPI api = FurusatoAPI.get();
        BootstrapReport report = api.bootstrapReport();
        int enabled = 0;
        for (ModuleContainer module : api.modules().containers()) {
            if (module.state() == ModuleState.ENABLED) {
                enabled++;
            }
        }
        List<Line> lines = new ArrayList<Line>();
        lines.add(new Line(I18n.format("furusatocore.screen.version", api.version()), COLOR_TEXT));
        lines.add(new Line(I18n.format("furusatocore.screen.api", api.apiVersion()), COLOR_TEXT));
        lines.add(new Line(I18n.format("furusatocore.screen.state", api.state()), COLOR_GREEN));
        lines.add(new Line(I18n.format("furusatocore.screen.bootstrap",
                formatMillis(report.totalMillis())), COLOR_TEXT));
        lines.add(new Line(I18n.format("furusatocore.screen.elapsed",
                formatMillis(report.elapsedMillis())), COLOR_MUTED));
        lines.add(new Line(I18n.format("furusatocore.screen.modules", enabled,
                api.modules().containers().size()), COLOR_TEXT));
        lines.add(new Line(I18n.format("furusatocore.screen.services", api.services().size()),
                COLOR_TEXT));
        return lines;
    }

    private List<Line> moduleLines() {
        List<Line> lines = new ArrayList<Line>();
        for (ModuleContainer module : FurusatoAPI.get().modules().containers()) {
            lines.add(new Line(I18n.format("furusatocore.screen.module.entry",
                    module.metadata().name(), module.metadata().id(), module.metadata().version(),
                    module.state()), moduleColor(module.state())));
            if (!module.statusDetail().isEmpty()) {
                lines.add(new Line("  " + module.statusDetail(), COLOR_MUTED));
            }
        }
        if (lines.isEmpty()) {
            lines.add(new Line(I18n.format("furusatocore.screen.empty"), COLOR_MUTED));
        }
        return lines;
    }

    private List<Line> serviceLines() {
        List<Line> lines = new ArrayList<Line>();
        for (ServiceSnapshot service : FurusatoAPI.get().services().snapshots()) {
            lines.add(new Line(I18n.format("furusatocore.screen.service.entry", service.id(),
                    service.status()), service.status() == ServiceStatus.AVAILABLE
                    ? COLOR_GREEN : COLOR_YELLOW));
            lines.add(new Line(I18n.format("furusatocore.screen.service.detail",
                    service.ownerModuleId(), service.threadPolicy(), service.consumers().size()),
                    COLOR_MUTED));
        }
        if (lines.isEmpty()) {
            lines.add(new Line(I18n.format("furusatocore.screen.empty"), COLOR_MUTED));
        }
        return lines;
    }

    private int moduleColor(ModuleState state) {
        switch (state) {
            case ENABLED:
                return COLOR_GREEN;
            case FAILED:
                return COLOR_RED;
            case LOADED:
                return COLOR_YELLOW;
            case DISCOVERED:
                return COLOR_BLUE;
            default:
                return COLOR_MUTED;
        }
    }

    private String formatMillis(double millis) {
        return String.format(Locale.ROOT, "%.3f", millis);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static final class Line {
        private final String text;
        private final int color;

        private Line(String text, int color) {
            this.text = text;
            this.color = color;
        }
    }
}
