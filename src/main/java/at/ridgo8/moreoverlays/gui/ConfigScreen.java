package at.ridgo8.moreoverlays.gui;

import at.ridgo8.moreoverlays.MoreOverlays;
import at.ridgo8.moreoverlays.gui.config.ConfigOptionList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;


public class ConfigScreen extends Screen {

    private final String modId;
    private final ModConfigSpec configSpec;
    private final List<String> pathCache = new ArrayList<>();
    private final Screen modListScreen;
    private ConfigOptionList optionList;
    private String categoryTitle = null;
    private Button btnReset;
    private Button btnUndo;
    private Button btnBack;
    private String txtUndo = "";
    private String txtReset = "";
    private String txtDone = "";

    public ConfigScreen(Screen modListScreen, ModConfigSpec spec, String modId) {
        super(Component.translatable("gui.config." + modId + ".title"));
        this.modListScreen = modListScreen;
        this.configSpec = spec;
        this.modId = modId;

        this.txtReset = I18n.get("gui.config." + MoreOverlays.MOD_ID + ".reset_config");
        this.txtUndo = I18n.get("gui.config." + MoreOverlays.MOD_ID + ".undo");
        this.txtDone = I18n.get("gui.done");
    }

    private void clearBottomButtonFocus() {
        if (this.btnBack != null) this.btnBack.setFocused(false);
        if (this.btnUndo != null) this.btnUndo.setFocused(false);
        if (this.btnReset != null) this.btnReset.setFocused(false);
        this.setFocused(null);
    }

    @Override
    protected void init() {

        if (this.optionList == null) {
            this.optionList = new ConfigOptionList(this.minecraft, this.modId, this);

            if (pathCache.isEmpty()) {
                this.optionList.setConfiguration(configSpec);
            } else {
                this.optionList.setConfiguration(configSpec, this.pathCache);
            }
        }

        Font font = Minecraft.getInstance().font;

        int undoGlyphWidth = font.width(ConfigOptionList.UNDO_CHAR);
        int resetGlyphWidth = font.width(ConfigOptionList.RESET_CHAR);

        int undoWidth = Math.max(font.width(" " + this.txtUndo) + undoGlyphWidth + 20, 80);
        int resetWidth = Math.max(font.width(" " + this.txtReset) + resetGlyphWidth + 20, 80);
        int doneWidth = Math.max(font.width(this.txtDone) + 20, 100);

        final int buttonY = this.height - 32 + (32 - 20) / 2;
        final int buttonHeight = 20;

        int pad = 10;
        int gap = 8; // spacing between Undo and Reset buttons
        final int xBack = pad;
        final int xDefaultAll = this.width - resetWidth - pad;
        final int xUndoAll = xDefaultAll - gap - undoWidth;

        this.btnReset = new Button.Builder(
                Component.nullToEmpty(ConfigOptionList.RESET_CHAR + " " + this.txtReset),
                (btn) -> this.optionList.reset())
                .pos(xDefaultAll, buttonY)
                .size(resetWidth, buttonHeight).build();

        this.btnUndo = new Button.Builder(
                Component.nullToEmpty(ConfigOptionList.UNDO_CHAR + " " + this.txtUndo),
                (btn) -> this.optionList.undo())
                .pos(xUndoAll, buttonY)
                .size(undoWidth, buttonHeight).build();

        this.btnBack = new Button.Builder(
                Component.nullToEmpty(" " + this.txtDone),
                (btn) -> this.back())
                .pos(xBack, buttonY)
                .size(doneWidth, buttonHeight).build();

        this.addRenderableWidget(this.optionList);
        this.addRenderableWidget(this.btnReset);
        this.addRenderableWidget(this.btnUndo);
        this.addRenderableWidget(this.btnBack);

        this.btnReset.active = false;
        this.btnUndo.active = false;

        this.optionList.updateGui();
    }

    private void back() {
        this.save();
        if (!this.optionList.getCurrentPath().isEmpty()) {
            this.optionList.pop();
            clearBottomButtonFocus();
        } else {
            //Minecraft.getInstance().forceSetScreen(modListScreen);
            Minecraft.getInstance().setScreen(modListScreen);
            clearBottomButtonFocus();
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (Minecraft.getInstance().level == null) {
            this.renderPanorama(guiGraphics, partialTicks);
        }
        this.renderMenuBackground(guiGraphics);
        this.renderBlurredBackground();
        this.optionList.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(this.font, this.getTitle(), this.width / 2, 8, 16777215);
        if (this.categoryTitle != null) {
            guiGraphics.drawCenteredString(this.font, this.categoryTitle, this.width / 2, 24, 16777215);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    private void save() {
        this.optionList.save();
        this.configSpec.save();
        this.optionList.undo();
    }

    @Override
    public void tick() {
        super.tick();
        this.btnReset.active = this.optionList.isResettable();
        this.btnUndo.active = this.optionList.isUndoable();
        // Avoid clearing text-field focus each frame; only defocus bottom buttons if mouse is far away
        if (Minecraft.getInstance().mouseHandler != null) {
            double my = Minecraft.getInstance().mouseHandler.ypos() * (double)Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double)Minecraft.getInstance().getWindow().getScreenHeight();
            int buttonY = this.height - 32 + (32 - 20) / 2;
            if (my < buttonY - 4 || my > buttonY + 24) {
                if (this.btnBack != null) this.btnBack.setFocused(false);
                if (this.btnUndo != null) this.btnUndo.setFocused(false);
                if (this.btnReset != null) this.btnReset.setFocused(false);
            }
        }
    }

    public void updatePath(final List<String> newPath) {
        final String key = this.optionList.categoryTitleKey(newPath);
        if (key == null) {
            this.categoryTitle = null;
        } else {
            this.categoryTitle = I18n.get(key);
        }

        pathCache.clear();
        pathCache.addAll(newPath);
        clearBottomButtonFocus();
    }

    @Override
    public boolean keyPressed(int key, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (key == 256) {
            this.back();
            return true;
        } else {
            return super.keyPressed(key, p_keyPressed_2_, p_keyPressed_3_);
        }
    }
}
