package at.feldim2425.moreoverlays.gui;

import java.util.ArrayList;
import java.util.List;

import at.feldim2425.moreoverlays.MoreOverlays;
import at.feldim2425.moreoverlays.gui.config.ConfigOptionList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeConfigSpec;


public class ConfigScreen extends Screen {

    private final String modId;
    private ForgeConfigSpec configSpec;
    private ConfigOptionList optionList;
    private String categoryTitle = null;

    private Button btnReset;
    private Button btnUndo;
    private Button btnBack;

    private List<String> pathCache = new ArrayList<>();
    private String txtUndo = "";
    private String txtReset = "";
    private String txtDone = "";
    
    private Screen modListScreen;

    public ConfigScreen(Screen modListScreen, ForgeConfigSpec spec, String modId) {
        super(new TranslationTextComponent("gui.config."+modId+".tile"));
        this.modListScreen = modListScreen;
        this.configSpec = spec;
        this.modId = modId;        

        this.txtReset = I18n.format("gui.config." + MoreOverlays.MOD_ID + ".reset_config");
        this.txtUndo = I18n.format("gui.config." + MoreOverlays.MOD_ID + ".undo");
        this.txtDone = I18n.format("gui.done");
    }

    @Override
    protected void init() {
    	
    	if (this.optionList == null) {
    		this.optionList = new ConfigOptionList(this.minecraft, this.modId, this);
    		
    		if(pathCache.isEmpty()){
                this.optionList.setConfiguration(configSpec);
            }
            else {
                this.optionList.setConfiguration(configSpec, this.pathCache);
            }            
    	}
        
        FontRenderer font = Minecraft.getInstance().fontRenderer;
        
        int undoGlyphWidth = font.getStringWidth(ConfigOptionList.UNDO_CHAR) * 2;
        int resetGlyphWidth = font.getStringWidth(ConfigOptionList.RESET_CHAR) * 2;
                
        int undoWidth = font.getStringWidth(" " + this.txtUndo) + undoGlyphWidth + 20;
        int resetWidth = font.getStringWidth(" " + this.txtReset) + resetGlyphWidth + 20;
        int doneWidth = Math.max(font.getStringWidth(this.txtDone) + 20, 100);
        
        final int buttonY = this.height - 32 + (32-20)/2;
        final int buttonHeight = 20;
        
        int pad = 10;
        final int xBack = pad;
        final int xDefaultAll = this.width - resetWidth-pad;        
        final int xUndoAll = xDefaultAll - undoWidth;
                        
        this.btnReset = new Button(xDefaultAll, buttonY, 100, buttonHeight, 
        		ConfigOptionList.RESET_CHAR+ " " + this.txtReset, 
        		(btn) -> this.optionList.reset());
        
        this.btnUndo = new Button(xUndoAll, buttonY, 100, buttonHeight, 
        		ConfigOptionList.UNDO_CHAR + " " + this.txtUndo,
        		(btn) -> this.optionList.undo());
        
        this.btnBack = new Button(xBack, buttonY, doneWidth, buttonHeight, 
        		" " + this.txtDone,
                (btn) -> this.back());
        
        this.children.add(this.optionList);
        this.children.add(this.btnReset);
        this.children.add(this.btnUndo);
        this.children.add(this.btnBack);

        this.btnReset.active = false;
        this.btnUndo.active = false;

        this.optionList.updateGui();
    }
    
    private void back() {    	    	
    	this.save();
        if(!this.optionList.getCurrentPath().isEmpty()){
            this.optionList.pop();           
        }
        else {
        	Minecraft.getInstance().displayGuiScreen(modListScreen);
        }
	}

	@Override
    public void render(int mouseX, int mouseY, float partialTick) {
        this.renderBackground();
        this.optionList.render(mouseX, mouseY, partialTick);
        this.btnReset.render(mouseX, mouseY, partialTick);
        this.btnUndo.render(mouseX, mouseY, partialTick); 
        this.btnBack.render(mouseX, mouseY, partialTick);
        this.drawCenteredString(this.font, this.title.getFormattedText(), this.width / 2, 8, 16777215);
        if(this.categoryTitle != null){
            this.drawCenteredString(this.font, this.categoryTitle, this.width / 2, 24, 16777215);
        }
        super.render(mouseX, mouseY, partialTick);
    }

    private void save(){
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
    
    public void updatePath(final List<String> newPath){
        final String key = this.optionList.categoryTitleKey(newPath);
        if(key == null){
            this.categoryTitle = null;
        }
        else {
            this.categoryTitle = I18n.format(key);
        }

        pathCache.clear();
        pathCache.addAll(newPath);
    }

    @Override
    public boolean keyPressed(int key, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (key == 256) {
        	this.back();
        	return true;
        }
        else {
            return super.keyPressed(key, p_keyPressed_2_, p_keyPressed_3_);
        }
    }
}
