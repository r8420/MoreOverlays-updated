package at.ridgo8.moreoverlays.gui.config;

import at.ridgo8.moreoverlays.MoreOverlays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class OptionValueEntry<V> extends ConfigOptionList.OptionEntry {

    public static final int CONTROL_WIDTH_NOVALIDATOR = 44;
    public static final int CONTROL_WIDTH_VALIDATOR = 64;
    public static final int TITLE_WIDTH = 160;
    protected final ModConfigSpec.ConfigValue<V> value;
    protected final ModConfigSpec.ValueSpec spec;
    private final List<String> tooltip;
    protected Button btnReset;
    protected Button btnUndo;
    protected V defaultValue;
    protected V newValue;
    protected boolean showValidity = false;
    private String txtUndo = "";
    private String txtReset = "";
    private String name = "";
    private boolean valid = false;
    private boolean changes = false;

    // Cached label bounds for hover detection
    private int labelX;
    private int labelY;
    private int labelW;
    private int labelH;

    @SuppressWarnings("unchecked")
    public OptionValueEntry(ConfigOptionList list, ModConfigSpec.ConfigValue<V> confValue, ModConfigSpec.ValueSpec spec) {
        super(list);
        this.value = confValue;
        this.spec = spec;

        this.btnReset = new Button.Builder(Component.nullToEmpty(ConfigOptionList.RESET_CHAR),
                (btn) -> this.reset())
                    .pos(list.getRowWidth() - 20, 0)
                    .size(20, 20).build();

        this.btnUndo = new Button.Builder(Component.nullToEmpty(ConfigOptionList.UNDO_CHAR),
                (btn) -> this.undo())
                    .pos(list.getRowWidth() - 42, 0)
                    .size(20, 20).build();

        this.txtReset = I18n.get("gui.config." + MoreOverlays.MOD_ID + ".reset_config");
        this.txtUndo = I18n.get("gui.config." + MoreOverlays.MOD_ID + ".undo");

        final Object defaultVal = this.spec.getDefault();
        if (defaultVal != null && spec.getClazz().isAssignableFrom(defaultVal.getClass())) {
            this.defaultValue = (V) defaultVal;
        } else {
            btnReset.active = false;
        }
        final String translationKey = "config.moreoverlays." + getTranslationKey().toLowerCase();
        if(Language.getInstance().has(translationKey)){
            this.name = I18n.get(translationKey);
        } else{
            this.name = this.value.getPath().get(this.value.getPath().size() - 1);
        }

        String[] lines = null;
        if (this.spec.getComment() != null) {
            lines = this.spec.getComment().split("\\n");
            tooltip = new ArrayList<>(lines.length + 1);
        } else {
            tooltip = new ArrayList<>(1);
        }


        tooltip.add(ChatFormatting.RED + this.name);
        if (lines != null) {
            for (final String line : lines) {
                tooltip.add(ChatFormatting.YELLOW + line);
            }
        }

        this.updateValue(this.value.get());
    }

    private String getTranslationKey() {
        // This method generates the translation key based on the config path
        // Assume that your config path segments are connected with dots (.)
        // Example path: ["lightoverlay", "uprange"] becomes "lightoverlay.uprange"
        return String.join(".", this.value.getPath());
    }

    @Override
    protected void renderControls(GuiGraphicsExtractor guiGraphics, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY, boolean mouseOver, float partialTick) {
        // Draw label right-aligned within the fixed title column to avoid overlap with controls
        final var font = Minecraft.getInstance().font;
        int rightEdge = rowLeft + TITLE_WIDTH - 5;
        int startX = rightEdge - font.width(this.name);
        if (startX < rowLeft + 4) startX = rowLeft + 4;
        guiGraphics.text(font, this.name, startX, rowTop + 6, 0xFFFFFFFF);
        // cache label hover rect
        this.labelX = startX;
        this.labelY = rowTop + 6;
        this.labelW = font.width(this.name);
        this.labelH = font.lineHeight;
        // Position row buttons absolutely so hover matches render
        this.btnReset.setPosition(rowLeft + this.getConfigOptionList().getRowWidth() - 20, rowTop);
        this.btnUndo.setPosition(rowLeft + this.getConfigOptionList().getRowWidth() - 42, rowTop);
        this.btnReset.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        this.btnUndo.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        if (this.showValidity) {
            int validityX = rowLeft + this.getConfigOptionList().getRowWidth() - 53;
            int validityY = rowTop + 6;
            if (this.valid) {
                guiGraphics.centeredText(font, ConfigOptionList.VALID, validityX, validityY, 0xFF00FF00);
            } else {
                guiGraphics.centeredText(font, ConfigOptionList.INVALID, validityX, validityY, 0xFFFF0000);
            }
        }
    }


    @Override
    protected void renderTooltip(GuiGraphicsExtractor guiGraphics, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, rowTop, rowLeft, rowWidth, itemHeight, mouseX, mouseY);

        // Show tooltips contextually: label shows spec tooltip, buttons show their own tooltips
        if (mouseX >= this.labelX && mouseX <= this.labelX + this.labelW && mouseY >= this.labelY && mouseY <= this.labelY + this.labelH) {
            if (!this.tooltip.isEmpty()) {
                List<Component> tooltipConverted = new ArrayList<>();
                for (String s : this.tooltip) tooltipConverted.add(Component.nullToEmpty(s));
                drawSimpleTooltip(guiGraphics, tooltipConverted, mouseX, mouseY);
            }
            return;
        }

        if (this.btnUndo != null && this.btnUndo.isMouseOver(mouseX, mouseY)) {
            if (this.txtUndo != null && !this.txtUndo.isEmpty()) {
                drawSimpleTooltip(guiGraphics, java.util.List.of(Component.nullToEmpty(this.txtUndo)), mouseX, mouseY);
            }
            return;
        }

        if (this.btnReset != null && this.btnReset.isMouseOver(mouseX, mouseY)) {
            if (this.txtReset != null && !this.txtReset.isEmpty()) {
                drawSimpleTooltip(guiGraphics, java.util.List.of(Component.nullToEmpty(this.txtReset)), mouseX, mouseY);
            }
        }
    }

    protected static void drawSimpleTooltip(GuiGraphicsExtractor guiGraphics, List<Component> lines, int mouseX, int mouseY) {
        final var font = Minecraft.getInstance().font;
        int maxWidth = 0;
        for (Component c : lines) {
            int w = font.width(c);
            if (w > maxWidth) maxWidth = w;
        }
        int x = mouseX + 12;
        int y = mouseY - 12;
        int padding = 4;
        int lineHeight = font.lineHeight + 1;
        int height = lines.size() * lineHeight + padding * 2 - 1;
        int width = maxWidth + padding * 2;

        int background = 0xF0100010; // vanilla-like dark bg with alpha
        int borderLight = 0x505000FF;
        int borderDark = 0x5028007F;

        // background
        guiGraphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, background);
        // border
        guiGraphics.fill(x - 1, y - 1, x + width + 1, y, borderDark);
        guiGraphics.fill(x - 1, y + height, x + width + 1, y + height + 1, borderLight);
        guiGraphics.fill(x - 1, y, x, y + height, borderDark);
        guiGraphics.fill(x + width, y, x + width + 1, y + height, borderLight);

        int textX = x + padding;
        int textY = y + padding - 1;
        for (Component c : lines) {
            guiGraphics.text(font, c, textX, textY, 0xFFFFFFFF);
            textY += lineHeight;
        }
    }

    protected abstract void overrideUnsaved(V value);

    protected boolean isUndoable(V current) {
        return current == null || !current.equals(this.value.get()) || !this.valid;
    }

    protected void updateValue(@Nullable V value) {
        this.valid = value != null && this.spec.test(value);
        btnReset.active = isResettable();
        this.changes = isUndoable(value);
        btnUndo.active = this.changes;
        this.newValue = value;
    }

    @Override
    public void undo() {
        this.overrideUnsaved(this.value.get());
        this.updateValue(this.value.get());
    }

    @Override
    public void reset() {
        if (this.defaultValue != null) {
            this.value.set(this.defaultValue);
            this.overrideUnsaved(this.defaultValue);
            this.updateValue(this.defaultValue);
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return Arrays.asList(this.btnReset, this.btnUndo);
    }

    @Override
    public boolean isValid() {
        return this.valid;
    }

    @Override
    public boolean hasChanges() {
        return this.changes;
    }

    @Override
    public boolean isResettable() {
        return this.defaultValue != null && (this.value.get() == null || !this.value.get().equals(this.defaultValue));
    }

    @Override
    public void save() {
        this.value.set(this.newValue);
        this.value.save();
    }
}