package at.ridgo8.moreoverlays.config;

import blue.endless.jankson.Jankson;
import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.Option;

public class MoreOverlaysConfig extends ConfigWrapper<at.ridgo8.moreoverlays.config.Config> {

    public final Keys keys = new Keys();

    private final Option<java.lang.Boolean> render_spawnNumbers = this.optionForKey(this.keys.render_spawnNumbers);
    private final Option<java.lang.Integer> light_UpRange = this.optionForKey(this.keys.light_UpRange);
    private final Option<java.lang.Integer> light_DownRange = this.optionForKey(this.keys.light_DownRange);
    private final Option<java.lang.Integer> light_HRange = this.optionForKey(this.keys.light_HRange);
    private final Option<java.lang.Boolean> light_IgnoreLayer = this.optionForKey(this.keys.light_IgnoreLayer);
    private final Option<java.lang.Boolean> light_IgnoreSpawnList = this.optionForKey(this.keys.light_IgnoreSpawnList);
    private final Option<java.lang.Boolean> light_SimpleEntityCheck = this.optionForKey(this.keys.light_SimpleEntityCheck);
    private final Option<java.lang.Integer> light_SaveLevel = this.optionForKey(this.keys.light_SaveLevel);
    private final Option<java.lang.Boolean> light_FinishedMigration = this.optionForKey(this.keys.light_FinishedMigration);
    private final Option<java.lang.Integer> light_UpdateIntervalFrames = this.optionForKey(this.keys.light_UpdateIntervalFrames);
    private final Option<java.lang.Integer> chunk_EdgeRadius = this.optionForKey(this.keys.chunk_EdgeRadius);
    private final Option<java.lang.Boolean> chunk_ShowMiddle = this.optionForKey(this.keys.chunk_ShowMiddle);
    private final Option<io.wispforest.owo.ui.core.Color> render_chunkEdgeColor = this.optionForKey(this.keys.render_chunkEdgeColor);
    private final Option<io.wispforest.owo.ui.core.Color> render_chunkGridColor = this.optionForKey(this.keys.render_chunkGridColor);
    private final Option<io.wispforest.owo.ui.core.Color> render_chunkMiddleColor = this.optionForKey(this.keys.render_chunkMiddleColor);
    private final Option<java.lang.Double> render_chunkLineWidth = this.optionForKey(this.keys.render_chunkLineWidth);
    private final Option<java.lang.Boolean> render_chunkThick = this.optionForKey(this.keys.render_chunkThick);
    private final Option<io.wispforest.owo.ui.core.Color> render_spawnAColor = this.optionForKey(this.keys.render_spawnAColor);
    private final Option<io.wispforest.owo.ui.core.Color> render_spawnNColor = this.optionForKey(this.keys.render_spawnNColor);
    private final Option<java.lang.Double> render_spawnLineWidth = this.optionForKey(this.keys.render_spawnLineWidth);
    private final Option<io.wispforest.owo.ui.core.Color> render_spawnSafeColor = this.optionForKey(this.keys.render_spawnSafeColor);
    private final Option<java.lang.Double> render_spawnNumberScale = this.optionForKey(this.keys.render_spawnNumberScale);
    private final Option<java.lang.Boolean> search_enabled = this.optionForKey(this.keys.search_enabled);
    private final Option<java.lang.Boolean> search_searchCustom = this.optionForKey(this.keys.search_searchCustom);
    private final Option<java.lang.Boolean> search_searchTooltip = this.optionForKey(this.keys.search_searchTooltip);
    private final Option<java.lang.Integer> search_maxResults = this.optionForKey(this.keys.search_maxResults);
    private final Option<io.wispforest.owo.ui.core.Color> search_searchBoxColor = this.optionForKey(this.keys.search_searchBoxColor);
    private final Option<io.wispforest.owo.ui.core.Color> search_filteredSlotColor = this.optionForKey(this.keys.search_filteredSlotColor);
    private final Option<java.lang.Double> search_filteredSlotTransparancy = this.optionForKey(this.keys.search_filteredSlotTransparancy);

    private MoreOverlaysConfig() {
        super(at.ridgo8.moreoverlays.config.Config.class);
    }

    private MoreOverlaysConfig(java.util.function.Consumer<Jankson.Builder> janksonBuilder) {
        super(at.ridgo8.moreoverlays.config.Config.class, janksonBuilder);
    }

    public static MoreOverlaysConfig createAndLoad() {
        var wrapper = new MoreOverlaysConfig();
        wrapper.load();
        return wrapper;
    }

    public static MoreOverlaysConfig createAndLoad(java.util.function.Consumer<Jankson.Builder> janksonBuilder) {
        var wrapper = new MoreOverlaysConfig(janksonBuilder);
        wrapper.load();
        return wrapper;
    }

    public boolean render_spawnNumbers() { return render_spawnNumbers.value(); }
    public void render_spawnNumbers(boolean value) { render_spawnNumbers.set(value); }

    public int light_UpRange() { return light_UpRange.value(); }
    public void light_UpRange(int value) { light_UpRange.set(value); }

    public int light_DownRange() { return light_DownRange.value(); }
    public void light_DownRange(int value) { light_DownRange.set(value); }

    public int light_HRange() { return light_HRange.value(); }
    public void light_HRange(int value) { light_HRange.set(value); }

    public boolean light_IgnoreLayer() { return light_IgnoreLayer.value(); }
    public void light_IgnoreLayer(boolean value) { light_IgnoreLayer.set(value); }

    public boolean light_IgnoreSpawnList() { return light_IgnoreSpawnList.value(); }
    public void light_IgnoreSpawnList(boolean value) { light_IgnoreSpawnList.set(value); }

    public boolean light_SimpleEntityCheck() { return light_SimpleEntityCheck.value(); }
    public void light_SimpleEntityCheck(boolean value) { light_SimpleEntityCheck.set(value); }

    public int light_SaveLevel() { return light_SaveLevel.value(); }
    public void light_SaveLevel(int value) { light_SaveLevel.set(value); }

    public boolean light_FinishedMigration() { return light_FinishedMigration.value(); }
    public void light_FinishedMigration(boolean value) { light_FinishedMigration.set(value); }

    public int light_UpdateIntervalFrames() { return light_UpdateIntervalFrames.value(); }
    public void light_UpdateIntervalFrames(int value) { light_UpdateIntervalFrames.set(value); }

    public int chunk_EdgeRadius() { return chunk_EdgeRadius.value(); }
    public void chunk_EdgeRadius(int value) { chunk_EdgeRadius.set(value); }

    public boolean chunk_ShowMiddle() { return chunk_ShowMiddle.value(); }
    public void chunk_ShowMiddle(boolean value) { chunk_ShowMiddle.set(value); }

    public io.wispforest.owo.ui.core.Color render_chunkEdgeColor() { return render_chunkEdgeColor.value(); }
    public void render_chunkEdgeColor(io.wispforest.owo.ui.core.Color value) { render_chunkEdgeColor.set(value); }

    public io.wispforest.owo.ui.core.Color render_chunkGridColor() { return render_chunkGridColor.value(); }
    public void render_chunkGridColor(io.wispforest.owo.ui.core.Color value) { render_chunkGridColor.set(value); }

    public io.wispforest.owo.ui.core.Color render_chunkMiddleColor() { return render_chunkMiddleColor.value(); }
    public void render_chunkMiddleColor(io.wispforest.owo.ui.core.Color value) { render_chunkMiddleColor.set(value); }

    public double render_chunkLineWidth() { return render_chunkLineWidth.value(); }
    public void render_chunkLineWidth(double value) { render_chunkLineWidth.set(value); }

    public boolean render_chunkThick() { return render_chunkThick.value(); }
    public void render_chunkThick(boolean value) { render_chunkThick.set(value); }

    public io.wispforest.owo.ui.core.Color render_spawnAColor() { return render_spawnAColor.value(); }
    public void render_spawnAColor(io.wispforest.owo.ui.core.Color value) { render_spawnAColor.set(value); }

    public io.wispforest.owo.ui.core.Color render_spawnNColor() { return render_spawnNColor.value(); }
    public void render_spawnNColor(io.wispforest.owo.ui.core.Color value) { render_spawnNColor.set(value); }

    public double render_spawnLineWidth() { return render_spawnLineWidth.value(); }
    public void render_spawnLineWidth(double value) { render_spawnLineWidth.set(value); }

    public io.wispforest.owo.ui.core.Color render_spawnSafeColor() { return render_spawnSafeColor.value(); }
    public void render_spawnSafeColor(io.wispforest.owo.ui.core.Color value) { render_spawnSafeColor.set(value); }

    public double render_spawnNumberScale() { return render_spawnNumberScale.value(); }
    public void render_spawnNumberScale(double value) { render_spawnNumberScale.set(value); }

    public boolean search_enabled() { return search_enabled.value(); }
    public void search_enabled(boolean value) { search_enabled.set(value); }

    public boolean search_searchCustom() { return search_searchCustom.value(); }
    public void search_searchCustom(boolean value) { search_searchCustom.set(value); }

    public boolean search_searchTooltip() { return search_searchTooltip.value(); }
    public void search_searchTooltip(boolean value) { search_searchTooltip.set(value); }

    public int search_maxResults() { return search_maxResults.value(); }
    public void search_maxResults(int value) { search_maxResults.set(value); }

    public io.wispforest.owo.ui.core.Color search_searchBoxColor() { return search_searchBoxColor.value(); }
    public void search_searchBoxColor(io.wispforest.owo.ui.core.Color value) { search_searchBoxColor.set(value); }

    public io.wispforest.owo.ui.core.Color search_filteredSlotColor() { return search_filteredSlotColor.value(); }
    public void search_filteredSlotColor(io.wispforest.owo.ui.core.Color value) { search_filteredSlotColor.set(value); }

    public double search_filteredSlotTransparancy() { return search_filteredSlotTransparancy.value(); }
    public void search_filteredSlotTransparancy(double value) { search_filteredSlotTransparancy.set(value); }

    public static class Keys {
        public final Option.Key render_spawnNumbers = new Option.Key("render_spawnNumbers");
        public final Option.Key light_UpRange = new Option.Key("light_UpRange");
        public final Option.Key light_DownRange = new Option.Key("light_DownRange");
        public final Option.Key light_HRange = new Option.Key("light_HRange");
        public final Option.Key light_IgnoreLayer = new Option.Key("light_IgnoreLayer");
        public final Option.Key light_IgnoreSpawnList = new Option.Key("light_IgnoreSpawnList");
        public final Option.Key light_SimpleEntityCheck = new Option.Key("light_SimpleEntityCheck");
        public final Option.Key light_SaveLevel = new Option.Key("light_SaveLevel");
        public final Option.Key light_FinishedMigration = new Option.Key("light_FinishedMigration");
        public final Option.Key light_UpdateIntervalFrames = new Option.Key("light_UpdateIntervalFrames");
        public final Option.Key chunk_EdgeRadius = new Option.Key("chunk_EdgeRadius");
        public final Option.Key chunk_ShowMiddle = new Option.Key("chunk_ShowMiddle");
        public final Option.Key render_chunkEdgeColor = new Option.Key("render_chunkEdgeColor");
        public final Option.Key render_chunkGridColor = new Option.Key("render_chunkGridColor");
        public final Option.Key render_chunkMiddleColor = new Option.Key("render_chunkMiddleColor");
        public final Option.Key render_chunkLineWidth = new Option.Key("render_chunkLineWidth");
        public final Option.Key render_chunkThick = new Option.Key("render_chunkThick");
        public final Option.Key render_spawnAColor = new Option.Key("render_spawnAColor");
        public final Option.Key render_spawnNColor = new Option.Key("render_spawnNColor");
        public final Option.Key render_spawnLineWidth = new Option.Key("render_spawnLineWidth");
        public final Option.Key render_spawnSafeColor = new Option.Key("render_spawnSafeColor");
        public final Option.Key render_spawnNumberScale = new Option.Key("render_spawnNumberScale");
        public final Option.Key search_enabled = new Option.Key("search_enabled");
        public final Option.Key search_searchCustom = new Option.Key("search_searchCustom");
        public final Option.Key search_searchTooltip = new Option.Key("search_searchTooltip");
        public final Option.Key search_maxResults = new Option.Key("search_maxResults");
        public final Option.Key search_searchBoxColor = new Option.Key("search_searchBoxColor");
        public final Option.Key search_filteredSlotColor = new Option.Key("search_filteredSlotColor");
        public final Option.Key search_filteredSlotTransparancy = new Option.Key("search_filteredSlotTransparancy");
    }
}


