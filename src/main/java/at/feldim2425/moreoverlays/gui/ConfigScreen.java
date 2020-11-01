package at.feldim2425.moreoverlays.gui;

import at.feldim2425.moreoverlays.MoreOverlays;
import at.feldim2425.moreoverlays.gui.config.ConfigOptionList;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;


public class ConfigScreen extends Screen {

    private final String modId;
    private final ForgeConfigSpec configSpec;
    private final List<String> pathCache = new ArrayList<>();
    private final Screen modListScreen;
    private ConfigOptionList optionList;
    private String categoryTitle;
    private Button btnReset;
    private Button btnUndo;
    private Button btnBack;
    private String txtUndo = "";
    private String txtReset = "";
    private String txtDone = "";

    public ConfigScreen(final Screen modListScreen, final ForgeConfigSpec spec, final String modId) {
        super(new TranslationTextComponent("gui.config." + modId + ".tile"));
        this.modListScreen = modListScreen;
        configSpec = spec;
        this.modId = modId;

        txtReset = I18n.format("gui.config." + MoreOverlays.MOD_ID + ".reset_config");
        txtUndo = I18n.format("gui.config." + MoreOverlays.MOD_ID + ".undo");
        txtDone = I18n.format("gui.done");
    }

    @Override
    protected void init() {

        if (optionList == null) {
            optionList = new ConfigOptionList(minecraft, modId, this);

            if (this.pathCache.isEmpty()) {
                optionList.setConfiguration(this.configSpec);
            } else {
                optionList.setConfiguration(this.configSpec, pathCache);
            }
        }

        final FontRenderer font = Minecraft.getInstance().fontRenderer;

        final int undoGlyphWidth = font.getStringWidth(ConfigOptionList.UNDO_CHAR) * 2;
        final int resetGlyphWidth = font.getStringWidth(ConfigOptionList.RESET_CHAR) * 2;

        final int undoWidth = font.getStringWidth(" " + txtUndo) + undoGlyphWidth + 20;
        final int resetWidth = font.getStringWidth(" " + txtReset) + resetGlyphWidth + 20;
        final int doneWidth = Math.max(font.getStringWidth(txtDone) + 20, 100);

        int buttonY = height - 32 + (32 - 20) / 2;
        final int buttonHeight = 20;

        final int pad = 10;
        int xBack = pad;
        int xDefaultAll = width - resetWidth - pad;
        int xUndoAll = xDefaultAll - undoWidth;

        btnReset = new Button(xDefaultAll, buttonY, 100, buttonHeight,
                ITextComponent.getTextComponentOrEmpty(ConfigOptionList.RESET_CHAR + " " + txtReset),
                (btn) -> optionList.reset());

        btnUndo = new Button(xUndoAll, buttonY, 100, buttonHeight,
                ITextComponent.getTextComponentOrEmpty(ConfigOptionList.UNDO_CHAR + " " + txtUndo),
                (btn) -> optionList.undo());

        btnBack = new Button(xBack, buttonY, doneWidth, buttonHeight,
                ITextComponent.getTextComponentOrEmpty(" " + txtDone),
                (btn) -> back());

        children.add(optionList);
        children.add(btnReset);
        children.add(btnUndo);
        children.add(btnBack);

        btnReset.active = false;
        btnUndo.active = false;

        optionList.updateGui();
    }

    private void back() {
        save();
        if (!optionList.getCurrentPath().isEmpty()) {
            optionList.pop();
        } else {
            Minecraft.getInstance().displayGuiScreen(this.modListScreen);
        }
    }

    @Override
    public void render(final MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        renderBackground(matrixStack);
        optionList.render(matrixStack, mouseX, mouseY, partialTicks);
        btnReset.render(matrixStack, mouseX, mouseY, partialTicks);
        btnUndo.render(matrixStack, mouseX, mouseY, partialTicks);
        btnBack.render(matrixStack, mouseX, mouseY, partialTicks);
        AbstractGui.drawCenteredString(matrixStack, font, getTitle(), width / 2, 8, 16777215);
        if (categoryTitle != null) {
            AbstractGui.drawCenteredString(matrixStack, font, categoryTitle, width / 2, 24, 16777215);
        }
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private void save() {
        optionList.save();
        configSpec.save();
        optionList.undo();
    }

    @Override
    public void tick() {
        super.tick();
        btnReset.active = optionList.isResettable();
        btnUndo.active = optionList.isUndoable();
    }

    public void updatePath(List<String> newPath) {
        String key = optionList.categoryTitleKey(newPath);
        if (key == null) {
            categoryTitle = null;
        } else {
            categoryTitle = I18n.format(key);
        }

        this.pathCache.clear();
        this.pathCache.addAll(newPath);
    }

    @Override
    public boolean keyPressed(final int key, final int p_keyPressed_2_, final int p_keyPressed_3_) {
        if (key == 256) {
            back();
            return true;
        } else {
            return super.keyPressed(key, p_keyPressed_2_, p_keyPressed_3_);
        }
    }
}
