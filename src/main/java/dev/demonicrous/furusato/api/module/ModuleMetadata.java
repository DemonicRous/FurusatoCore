package dev.demonicrous.furusato.api.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class ModuleMetadata {
    private static final Pattern VALID_ID = Pattern.compile("[a-z][a-z0-9_.-]{1,63}");

    private final String id;
    private final String name;
    private final String version;
    private final boolean requiredForCore;
    private final List<String> requiredDependencies;
    private final List<String> optionalDependencies;

    private ModuleMetadata(Builder builder) {
        id = validateId(builder.id);
        name = requireText(builder.name, "name");
        version = requireText(builder.version, "version");
        requiredForCore = builder.requiredForCore;
        requiredDependencies = immutableDependencies(builder.requiredDependencies, id);
        optionalDependencies = immutableDependencies(builder.optionalDependencies, id);

        Set<String> overlap = new LinkedHashSet<String>(requiredDependencies);
        overlap.retainAll(optionalDependencies);
        if (!overlap.isEmpty()) {
            throw new IllegalArgumentException(
                    "Dependencies cannot be both required and optional: " + overlap);
        }
    }

    public static Builder builder(String id, String name, String version) {
        return new Builder(id, name, version);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String version() {
        return version;
    }

    public boolean isRequiredForCore() {
        return requiredForCore;
    }

    public List<String> requiredDependencies() {
        return requiredDependencies;
    }

    public List<String> optionalDependencies() {
        return optionalDependencies;
    }

    public static String validateId(String id) {
        if (id == null || !VALID_ID.matcher(id).matches()) {
            throw new IllegalArgumentException(
                    "Invalid module ID '" + id + "'; expected " + VALID_ID.pattern());
        }
        return id;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be empty");
        }
        return value;
    }

    private static List<String> immutableDependencies(Set<String> dependencies, String ownId) {
        List<String> result = new ArrayList<String>();
        for (String dependency : dependencies) {
            String id = validateId(dependency);
            if (ownId.equals(id)) {
                throw new IllegalArgumentException("Module cannot depend on itself: " + ownId);
            }
            result.add(id);
        }
        Collections.sort(result);
        return Collections.unmodifiableList(result);
    }

    public static final class Builder {
        private final String id;
        private final String name;
        private final String version;
        private final Set<String> requiredDependencies = new LinkedHashSet<String>();
        private final Set<String> optionalDependencies = new LinkedHashSet<String>();
        private boolean requiredForCore;

        private Builder(String id, String name, String version) {
            this.id = id;
            this.name = name;
            this.version = version;
        }

        public Builder requiredForCore() {
            requiredForCore = true;
            return this;
        }

        public Builder requires(String moduleId) {
            requiredDependencies.add(moduleId);
            return this;
        }

        public Builder optionallyDependsOn(String moduleId) {
            optionalDependencies.add(moduleId);
            return this;
        }

        public ModuleMetadata build() {
            return new ModuleMetadata(this);
        }
    }
}
