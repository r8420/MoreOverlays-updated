package at.ridgo8.moreoverlays.config;

import at.ridgo8.moreoverlays.MoreOverlays;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Small self-contained replacement for NeoForge's ModConfigSpec.
 * Holds sections of typed {@link ConfigValue}s and persists them as JSON.
 */
public class ConfigSpec {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<String, Section> sections = new LinkedHashMap<>();
    private Path filePath;

    public static class Section {
        private final String name;
        private final String comment;
        private final Map<String, ConfigValue<?>> values = new LinkedHashMap<>();

        Section(String name, String comment) {
            this.name = name;
            this.comment = comment;
        }

        public String getName() {
            return this.name;
        }

        public String getComment() {
            return this.comment;
        }

        public Map<String, ConfigValue<?>> getValues() {
            return Collections.unmodifiableMap(this.values);
        }
    }

    public Map<String, Section> getSections() {
        return Collections.unmodifiableMap(this.sections);
    }

    public Section getSection(String name) {
        return this.sections.get(name);
    }

    public ConfigValue<?> getValue(String section, String key) {
        final Section sec = this.sections.get(section);
        return sec == null ? null : sec.values.get(key);
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }

    public Path getFilePath() {
        return this.filePath;
    }

    public void save() {
        if (this.filePath == null) {
            return;
        }
        final JsonObject root = new JsonObject();
        for (final Section section : this.sections.values()) {
            final JsonObject secObj = new JsonObject();
            for (final ConfigValue<?> value : section.values.values()) {
                final Object v = value.get();
                if (v instanceof Boolean b) {
                    secObj.addProperty(value.getKey(), b);
                } else if (v instanceof Number n) {
                    secObj.addProperty(value.getKey(), n);
                } else if (v != null) {
                    secObj.addProperty(value.getKey(), v.toString());
                }
            }
            root.add(section.name, secObj);
        }
        try {
            Files.createDirectories(this.filePath.getParent());
            try (Writer writer = Files.newBufferedWriter(this.filePath)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            MoreOverlays.logger.error("Failed to save config file " + this.filePath, e);
        }
    }

    public void load() {
        if (this.filePath == null || !Files.exists(this.filePath)) {
            this.save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(this.filePath)) {
            final JsonElement parsed = JsonParser.parseReader(reader);
            if (parsed != null && parsed.isJsonObject()) {
                final JsonObject root = parsed.getAsJsonObject();
                for (final Section section : this.sections.values()) {
                    final JsonElement secElem = root.get(section.name);
                    if (secElem == null || !secElem.isJsonObject()) {
                        continue;
                    }
                    final JsonObject secObj = secElem.getAsJsonObject();
                    for (final ConfigValue<?> value : section.values.values()) {
                        final JsonElement elem = secObj.get(value.getKey());
                        if (elem != null && elem.isJsonPrimitive()) {
                            value.loadFrom(elem.getAsJsonPrimitive());
                        }
                    }
                }
            }
        } catch (Exception e) {
            MoreOverlays.logger.error("Failed to load config file " + this.filePath + ", using defaults.", e);
        }
        // Re-write so missing/new options get persisted
        this.save();
    }

    public static class Builder {

        private final ConfigSpec spec = new ConfigSpec();
        private Section currentSection;
        private String pendingComment;

        public Builder comment(String comment) {
            this.pendingComment = comment;
            return this;
        }

        public Builder push(String section) {
            this.currentSection = new Section(section, this.pendingComment);
            this.pendingComment = null;
            this.spec.sections.put(section, this.currentSection);
            return this;
        }

        public Builder pop() {
            this.currentSection = null;
            return this;
        }

        public ConfigValue.BooleanValue define(String key, boolean defaultValue) {
            final ConfigValue.BooleanValue value = new ConfigValue.BooleanValue(
                    this.spec, this.currentSection.name, key, this.pendingComment, defaultValue);
            this.pendingComment = null;
            this.currentSection.values.put(key, value);
            return value;
        }

        public ConfigValue.IntValue defineInRange(String key, int defaultValue, int min, int max) {
            final ConfigValue.IntValue value = new ConfigValue.IntValue(
                    this.spec, this.currentSection.name, key, this.pendingComment, defaultValue, min, max);
            this.pendingComment = null;
            this.currentSection.values.put(key, value);
            return value;
        }

        public ConfigValue.DoubleValue defineInRange(String key, double defaultValue, double min, double max) {
            final ConfigValue.DoubleValue value = new ConfigValue.DoubleValue(
                    this.spec, this.currentSection.name, key, this.pendingComment, defaultValue, min, max);
            this.pendingComment = null;
            this.currentSection.values.put(key, value);
            return value;
        }

        public ConfigSpec build() {
            return this.spec;
        }
    }

    /**
     * Typed configuration value. Also acts as its own "value spec"
     * (comment, default, range validation) - the roles that NeoForge splits
     * between ConfigValue and ValueSpec.
     */
    public abstract static class ConfigValue<V> {

        protected final ConfigSpec spec;
        private final String section;
        private final String key;
        private final String comment;
        protected final V defaultValue;
        protected V value;

        protected ConfigValue(ConfigSpec spec, String section, String key, String comment, V defaultValue) {
            this.spec = spec;
            this.section = section;
            this.key = key;
            this.comment = comment;
            this.defaultValue = defaultValue;
            this.value = defaultValue;
        }

        public V get() {
            return this.value;
        }

        public void set(V value) {
            if (this.test(value)) {
                this.value = value;
            }
        }

        public void save() {
            this.spec.save();
        }

        public String getKey() {
            return this.key;
        }

        public String getSection() {
            return this.section;
        }

        public List<String> getPath() {
            return List.of(this.section, this.key);
        }

        public String getComment() {
            return this.comment;
        }

        public V getDefault() {
            return this.defaultValue;
        }

        public abstract Class<V> getClazz();

        /**
         * @return true if the (possibly null) candidate value is acceptable for this option.
         */
        public abstract boolean test(Object candidate);

        abstract void loadFrom(JsonPrimitive json);

        public static class BooleanValue extends ConfigValue<Boolean> {

            BooleanValue(ConfigSpec spec, String section, String key, String comment, boolean defaultValue) {
                super(spec, section, key, comment, defaultValue);
            }

            @Override
            public Class<Boolean> getClazz() {
                return Boolean.class;
            }

            @Override
            public boolean test(Object candidate) {
                return candidate instanceof Boolean;
            }

            @Override
            void loadFrom(JsonPrimitive json) {
                if (json.isBoolean()) {
                    this.value = json.getAsBoolean();
                } else if (json.isString()) {
                    this.value = Boolean.parseBoolean(json.getAsString());
                }
            }
        }

        public static class IntValue extends ConfigValue<Integer> {

            private final int min;
            private final int max;

            IntValue(ConfigSpec spec, String section, String key, String comment, int defaultValue, int min, int max) {
                super(spec, section, key, comment, defaultValue);
                this.min = min;
                this.max = max;
            }

            @Override
            public Class<Integer> getClazz() {
                return Integer.class;
            }

            @Override
            public boolean test(Object candidate) {
                if (!(candidate instanceof Integer i)) {
                    return false;
                }
                return i >= this.min && i <= this.max;
            }

            @Override
            void loadFrom(JsonPrimitive json) {
                try {
                    final int loaded = json.isNumber() ? json.getAsInt() : Integer.parseInt(json.getAsString());
                    if (this.test(loaded)) {
                        this.value = loaded;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        public static class DoubleValue extends ConfigValue<Double> {

            private final double min;
            private final double max;

            DoubleValue(ConfigSpec spec, String section, String key, String comment, double defaultValue, double min, double max) {
                super(spec, section, key, comment, defaultValue);
                this.min = min;
                this.max = max;
            }

            @Override
            public Class<Double> getClazz() {
                return Double.class;
            }

            @Override
            public boolean test(Object candidate) {
                if (!(candidate instanceof Double d)) {
                    return false;
                }
                return d >= this.min && d <= this.max;
            }

            @Override
            void loadFrom(JsonPrimitive json) {
                try {
                    final double loaded = json.isNumber() ? json.getAsDouble() : Double.parseDouble(json.getAsString());
                    if (this.test(loaded)) {
                        this.value = loaded;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }
}
