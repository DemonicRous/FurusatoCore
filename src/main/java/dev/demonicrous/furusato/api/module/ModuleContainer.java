package dev.demonicrous.furusato.api.module;

import java.util.Optional;

public final class ModuleContainer {
    private final ModuleMetadata metadata;
    private final ModuleState state;
    private final String statusDetail;
    private final Throwable failure;

    public ModuleContainer(ModuleMetadata metadata, ModuleState state, String statusDetail,
            Throwable failure) {
        if (metadata == null || state == null) {
            throw new IllegalArgumentException("Metadata and state are required");
        }
        this.metadata = metadata;
        this.state = state;
        this.statusDetail = statusDetail == null ? "" : statusDetail;
        this.failure = failure;
    }

    public ModuleMetadata metadata() {
        return metadata;
    }

    public ModuleState state() {
        return state;
    }

    public String statusDetail() {
        return statusDetail;
    }

    public Optional<Throwable> failure() {
        return Optional.ofNullable(failure);
    }

}
