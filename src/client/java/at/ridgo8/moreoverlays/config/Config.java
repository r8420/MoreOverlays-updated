package at.ridgo8.moreoverlays.config;

import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.SectionHeader;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.config.annotation.ExcludeFromScreen;
import io.wispforest.owo.config.annotation.WithAlpha;
import io.wispforest.owo.ui.core.Color;

@Modmenu(modId = "moreoverlays")
@io.wispforest.owo.config.annotation.Config(name = "moreoverlays", wrapperName = "MoreOverlaysConfig")
public class Config {

    @SectionHeader("light")
    public boolean render_spawnNumbers = false;
    @RangeConstraint(min = 0, max = 32)
    public int light_UpRange = 4;
    @RangeConstraint(min = 0, max = 512)
    public int light_DownRange = 16;
    @RangeConstraint(min = 0, max = 512)
    public int light_HRange = 16;
    public boolean light_IgnoreLayer = false;
    public boolean light_IgnoreSpawnList = false;
    public boolean light_SimpleEntityCheck = false;
    @RangeConstraint(min = 0, max = 15)
    public int light_SaveLevel = 1;
    @ExcludeFromScreen
    public boolean light_FinishedMigration = true;
    @RangeConstraint(min = 1, max = 100)
    public int light_UpdateIntervalFrames = 1;

    @SectionHeader("chunkbounds")
    @RangeConstraint(min = 0, max = 15)
    public int chunk_EdgeRadius = 1;
    public boolean chunk_ShowMiddle = true;

    @SectionHeader("rendering")
    public Color render_chunkEdgeColor = Color.ofRgb(0xFF0000);
    public Color render_chunkGridColor = Color.ofRgb(0x00FF00);
    public Color render_chunkMiddleColor = Color.ofRgb(0xFFFF00);
    @RangeConstraint(min = 0, max = 100, decimalPlaces = 2)
    public double render_chunkLineWidth = 1.5;
    public boolean render_chunkThick = false;
    public Color render_spawnAColor = Color.ofRgb(0xFF0000);
    public Color render_spawnNColor = Color.ofRgb(0xFFFF00);
    @RangeConstraint(min = 0, max = 400, decimalPlaces = 2)
    public double render_spawnLineWidth = 2;
    public Color render_spawnSafeColor = Color.ofRgb(0x00FF00);
    @RangeConstraint(min = 0.01, max = 0.13, decimalPlaces = 3)
    public double render_spawnNumberScale = 0.07;

    @SectionHeader("search")
    public boolean search_enabled = true;
    public boolean search_searchCustom = true;
    public boolean search_searchTooltip = true;
    @RangeConstraint(min = 256, max = 271360)
    public int search_maxResults = 16384;
    public Color search_searchBoxColor = Color.ofRgb(0xFFFF00);
    public Color search_filteredSlotColor = Color.ofRgb(0x000000);
    @RangeConstraint(min = 0, max = 1, decimalPlaces = 2)
    public double search_filteredSlotTransparancy = 0.5F;
}
