package at.ridgo8.moreoverlays.gui;

import at.ridgo8.moreoverlays.MoreOverlays;
import at.ridgo8.moreoverlays.gui.config.ConfigOptionList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;


public class ConfigScreen extends Screen {

    private final String modId;
    private final ForgeConfigSpec configSpec;
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

    public ConfigScreen(Screen modListScreen, ForgeConfigSpec spec, String modId) {
        super(Component.translatable("gui.config." + modId + ".tile"));
        this.modListScreen = modListScreen;
        this.configSpec = spec;
        this.modId = modId;

        this.txtReset = I18n.get("gui.config." + MoreOverlays.MOD_ID + ".reset_config");
        this.txtUndo = I18n.get("gui.config." + MoreOverlays.MOD_ID + ".undo");
        this.txtDone = I18n.get("gui.done");
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

        int undoGlyphWidth = font.width(ConfigOptionList.UNDO_CHAR) * 2;
        int resetGlyphWidth = font.width(ConfigOptionList.RESET_CHAR) * 2;

        int undoWidth = font.width(" " + this.txtUndo) + undoGlyphWidth + 20;
        int resetWidth = font.width(" " + this.txtReset) + resetGlyphWidth + 20;
        int doneWidth = Math.max(font.width(this.txtDone) + 20, 100);

        final int buttonY = this.height - 32 + (32 - 20) / 2;
        final int buttonHeight = 20;

        int pad = 10;
        final int xBack = pad;
        final int xDefaultAll = this.width - resetWidth - pad;
        final int xUndoAll = xDefaultAll - undoWidth;

        this.btnReset = new Button.Builder(
                Component.nullToEmpty(ConfigOptionList.RESET_CHAR + " " + this.txtReset),
                (btn) -> this.optionList.reset())
                .pos(xDefaultAll, buttonY)
                .size(100, buttonHeight).build();

        this.btnUndo = new Button.Builder(
                Component.nullToEmpty(ConfigOptionList.UNDO_CHAR + " " + this.txtUndo),
                (btn) -> this.optionList.undo())
                .pos(xUndoAll, buttonY)
                .size(100, buttonHeight).build();

        this.btnBack = new Button.Builder(
                Component.nullToEmpty(" " + this.txtDone),
                (btn) -> this.back())
                .pos(xBack, buttonY)
                .size(doneWidth, buttonHeight).build();

        this.addWidget(this.optionList);
        this.addWidget(this.btnReset);
        this.addWidget(this.btnUndo);
        this.addWidget(this.btnBack);

        this.btnReset.active = false;
        this.btnUndo.active = false;

        this.optionList.updateGui();
    }

    private void back() {
        this.save();
        if (!this.optionList.getCurrentPath().isEmpty()) {
            this.optionList.pop();
        } else {
            //Minecraft.getInstance().forceSetScreen(modListScreen);
            Minecraft.getInstance().setScreen(modListScreen);
        }
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        this.optionList.render(matrixStack, mouseX, mouseY, partialTicks);
        this.btnReset.render(matrixStack, mouseX, mouseY, partialTicks);
        this.btnUndo.render(matrixStack, mouseX, mouseY, partialTicks);
        this.btnBack.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.font, this.getTitle(), this.width / 2, 8, 16777215);
        if (this.categoryTitle != null) {
            drawCenteredString(matrixStack, this.font, this.categoryTitle, this.width / 2, 24, 16777215);
        }
        super.render(matrixStack, mouseX, mouseY, partialTicks);
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
