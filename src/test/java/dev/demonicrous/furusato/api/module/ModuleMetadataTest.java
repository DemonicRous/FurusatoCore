package dev.demonicrous.furusato.api.module;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class ModuleMetadataTest {
    @Test
    public void sortsDependenciesForStableGraphResolution() {
        ModuleMetadata metadata = ModuleMetadata.builder("feature", "Feature", "1.0.0")
                .requires("zulu")
                .requires("alpha")
                .build();
        assertEquals("alpha", metadata.requiredDependencies().get(0));
        assertEquals("zulu", metadata.requiredDependencies().get(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidId() {
        ModuleMetadata.builder("Invalid ID", "Invalid", "1.0.0").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsSelfDependency() {
        ModuleMetadata.builder("feature", "Feature", "1.0.0")
                .requires("feature")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsRequiredOptionalOverlap() {
        ModuleMetadata.builder("feature", "Feature", "1.0.0")
                .requires("base")
                .optionallyDependsOn("base")
                .build();
    }
}
