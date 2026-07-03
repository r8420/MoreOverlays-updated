package at.ridgo8.moreoverlays.config;

import at.ridgo8.moreoverlays.MoreOverlays;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One-time migration of values from the old owo-lib config file
 * (config/moreoverlays.json5) into the new JSON config.
 */
final class OwoConfigMigration {

    // Matches lines like:  "light_UpRange": 4,   or   render_chunkEdgeColor: "#FF0000",
    private static final Pattern ENTRY = Pattern.compile(
            "\"?([A-Za-z0-9_]+)\"?\\s*:\\s*(\"[^\"]*\"|[-+0-9.eEtruefals]+)\\s*,?\\s*$");

    private OwoConfigMigration() {
    }

    static void migrateIfNeeded(ConfigSpec spec) {
        final Path newFile = spec.getFilePath();
        final Path oldFile = FabricLoader.getInstance().getConfigDir().resolve(MoreOverlays.MOD_ID + ".json5");
        if (newFile == null || Files.exists(newFile) || !Files.exists(oldFile)) {
            return;
        }
        try {
            for (final String line : Files.readAllLines(oldFile)) {
                final String trimmed = line.trim();
                if (trimmed.startsWith("//") || trimmed.isEmpty()) {
                    continue;
                }
                final Matcher m = ENTRY.matcher(trimmed);
                if (m.find()) {
                    applyValue(m.group(1), m.group(2));
                }
            }
            spec.save();
            MoreOverlays.logger.info("Migrated old owo-lib config from {} to {}", oldFile, newFile);
        } catch (Exception e) {
            MoreOverlays.logger.warn("Failed to migrate old owo-lib config, using defaults.", e);
        }
    }

    private static void applyValue(String owoKey, String rawValue) {
        switch (owoKey) {
            case "render_spawnNumbers" -> setBool(Config.render_spawnNumbers, rawValue);
            case "light_UpRange" -> setInt(Config.light_UpRange, rawValue);
            case "light_DownRange" -> setInt(Config.light_DownRange, rawValue);
            case "light_HRange" -> setInt(Config.light_HRange, rawValue);
            case "light_IgnoreLayer" -> setBool(Config.light_IgnoreLayer, rawValue);
            case "light_IgnoreSpawnList" -> setBool(Config.light_IgnoreSpawnList, rawValue);
            case "light_SimpleEntityCheck" -> setBool(Config.light_SimpleEntityCheck, rawValue);
            case "light_SaveLevel" -> setInt(Config.light_SaveLevel, rawValue);
            case "light_FinishedMigration" -> setBool(Config.light_FinishedMigration, rawValue);
            case "light_UpdateIntervalFrames" -> setInt(Config.light_UpdateIntervalFrames, rawValue);
            case "chunk_EdgeRadius" -> setInt(Config.chunk_EdgeRadius, rawValue);
            case "chunk_ShowMiddle" -> setBool(Config.chunk_ShowMiddle, rawValue);
            case "render_chunkEdgeColor" -> setColor(Config.render_chunkEdgeColor, rawValue);
            case "render_chunkGridColor" -> setColor(Config.render_chunkGridColor, rawValue);
            case "render_chunkMiddleColor" -> setColor(Config.render_chunkMiddleColor, rawValue);
            case "render_chunkLineWidth" -> setDouble(Config.render_chunkLineWidth, rawValue);
            case "render_chunkThick" -> setBool(Config.render_chunkThick, rawValue);
            case "render_spawnAColor" -> setColor(Config.render_spawnAColor, rawValue);
            case "render_spawnNColor" -> setColor(Config.render_spawnNColor, rawValue);
            case "render_spawnLineWidth" -> setDouble(Config.render_spawnLineWidth, rawValue);
            case "render_spawnSafeColor" -> setColor(Config.render_spawnSafeColor, rawValue);
            case "render_spawnNumberScale" -> setDouble(Config.render_spawnNumberScale, rawValue);
            case "search_enabled" -> setBool(Config.search_enabled, rawValue);
            case "search_searchCustom" -> setBool(Config.search_searchCustom, rawValue);
            case "search_searchTooltip" -> setBool(Config.search_searchTooltip, rawValue);
            case "search_maxResults" -> setInt(Config.search_maxResults, rawValue);
            case "search_searchBoxColor" -> setColor(Config.search_searchBoxColor, rawValue);
            case "search_filteredSlotColor" -> setColor(Config.search_filteredSlotColor, rawValue);
            case "search_filteredSlotTransparancy" -> setDouble(Config.search_filteredSlotTransparancy, rawValue);
            default -> {
            }
        }
    }

    private static void setBool(ConfigSpec.ConfigValue.BooleanValue value, String raw) {
        value.set(Boolean.parseBoolean(unquote(raw)));
    }

    private static void setInt(ConfigSpec.ConfigValue.IntValue value, String raw) {
        try {
            value.set((int) Double.parseDouble(unquote(raw)));
        } catch (NumberFormatException ignored) {
        }
    }

    private static void setDouble(ConfigSpec.ConfigValue.DoubleValue value, String raw) {
        try {
            value.set(Double.parseDouble(unquote(raw)));
        } catch (NumberFormatException ignored) {
        }
    }

    private static void setColor(ConfigSpec.ConfigValue.IntValue value, String raw) {
        String s = unquote(raw).trim();
        if (s.startsWith("#")) {
            s = s.substring(1);
        } else if (s.startsWith("0x") || s.startsWith("0X")) {
            s = s.substring(2);
        } else {
            setInt(value, s);
            return;
        }
        try {
            value.set((int) (Long.parseLong(s, 16) & 0xFFFFFF));
        } catch (NumberFormatException ignored) {
        }
    }

    private static String unquote(String raw) {
        if (raw.length() >= 2 && raw.startsWith("\"") && raw.endsWith("\"")) {
            return raw.substring(1, raw.length() - 1);
        }
        return raw;
    }
}
