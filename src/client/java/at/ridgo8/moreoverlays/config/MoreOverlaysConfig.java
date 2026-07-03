package at.ridgo8.moreoverlays.config;

/**
 * Facade with the same accessor names the rest of the mod already uses.
 * Delegates to the static {@link Config} spec values. Colors are plain RGB ints.
 */
public class MoreOverlaysConfig {

    public void load() {
        Config.config_client.load();
    }

    public void save() {
        Config.config_client.save();
    }

    public boolean render_spawnNumbers() { return Config.render_spawnNumbers.get(); }

    public int light_UpRange() { return Config.light_UpRange.get(); }

    public int light_DownRange() { return Config.light_DownRange.get(); }

    public int light_HRange() { return Config.light_HRange.get(); }

    public boolean light_IgnoreLayer() { return Config.light_IgnoreLayer.get(); }

    public boolean light_IgnoreSpawnList() { return Config.light_IgnoreSpawnList.get(); }

    public boolean light_SimpleEntityCheck() { return Config.light_SimpleEntityCheck.get(); }

    public int light_SaveLevel() { return Config.light_SaveLevel.get(); }

    public boolean light_FinishedMigration() { return Config.light_FinishedMigration.get(); }

    public int light_UpdateIntervalFrames() { return Config.light_UpdateIntervalFrames.get(); }

    public int chunk_EdgeRadius() { return Config.chunk_EdgeRadius.get(); }

    public boolean chunk_ShowMiddle() { return Config.chunk_ShowMiddle.get(); }

    public int render_chunkEdgeColor() { return Config.render_chunkEdgeColor.get(); }

    public int render_chunkGridColor() { return Config.render_chunkGridColor.get(); }

    public int render_chunkMiddleColor() { return Config.render_chunkMiddleColor.get(); }

    public double render_chunkLineWidth() { return Config.render_chunkLineWidth.get(); }

    public boolean render_chunkThick() { return Config.render_chunkThick.get(); }

    public int render_spawnAColor() { return Config.render_spawnAColor.get(); }

    public int render_spawnNColor() { return Config.render_spawnNColor.get(); }

    public double render_spawnLineWidth() { return Config.render_spawnLineWidth.get(); }

    public int render_spawnSafeColor() { return Config.render_spawnSafeColor.get(); }

    public double render_spawnNumberScale() { return Config.render_spawnNumberScale.get(); }

    public boolean search_enabled() { return Config.search_enabled.get(); }

    public boolean search_searchCustom() { return Config.search_searchCustom.get(); }

    public boolean search_searchTooltip() { return Config.search_searchTooltip.get(); }

    public int search_maxResults() { return Config.search_maxResults.get(); }

    public int search_searchBoxColor() { return Config.search_searchBoxColor.get(); }

    public int search_filteredSlotColor() { return Config.search_filteredSlotColor.get(); }

    public double search_filteredSlotTransparancy() { return Config.search_filteredSlotTransparancy.get(); }
}
