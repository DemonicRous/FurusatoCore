package dev.demonicrous.furusato.core.module;

import dev.demonicrous.furusato.api.module.IFurusatoModule;
import dev.demonicrous.furusato.api.module.IModuleManager;
import dev.demonicrous.furusato.api.module.ModuleContainer;
import dev.demonicrous.furusato.api.module.ModuleContext;
import dev.demonicrous.furusato.api.module.ModuleMetadata;
import dev.demonicrous.furusato.api.module.ModuleState;
import dev.demonicrous.furusato.api.service.IServiceRegistry;
import dev.demonicrous.furusato.core.internal.service.OwnedServiceRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public final class ModuleManager implements IModuleManager {
    private final Map<String, ModuleRecord> records = new TreeMap<String, ModuleRecord>();
    private final IServiceRegistry services;
    private List<ModuleRecord> loadOrder = Collections.emptyList();
    private boolean registrationClosed;

    public ModuleManager(IServiceRegistry services) {
        if (services == null) {
            throw new NullPointerException("services");
        }
        this.services = services;
    }

    @Override
    public synchronized void register(IFurusatoModule module) {
        if (registrationClosed) {
            throw new IllegalStateException("Module registration is closed");
        }
        if (module == null || module.metadata() == null) {
            throw new IllegalArgumentException("Module and metadata are required");
        }
        ModuleMetadata metadata = module.metadata();
        ModuleMetadata.validateId(metadata.id());
        if (records.containsKey(metadata.id())) {
            throw new IllegalStateException("Duplicate module ID: " + metadata.id());
        }
        records.put(metadata.id(), new ModuleRecord(module, metadata));
    }

    @Override
    public synchronized Optional<ModuleContainer> find(String moduleId) {
        ModuleRecord record = records.get(moduleId);
        return record == null ? Optional.<ModuleContainer>empty()
                : Optional.of(record.snapshot());
    }

    @Override
    public synchronized Collection<ModuleContainer> containers() {
        List<ModuleContainer> snapshots = new ArrayList<ModuleContainer>();
        for (ModuleRecord record : records.values()) {
            snapshots.add(record.snapshot());
        }
        return Collections.unmodifiableList(snapshots);
    }

    public synchronized void loadAll() {
        if (registrationClosed) {
            throw new IllegalStateException("Modules were already loaded");
        }
        registrationClosed = true;
        loadOrder = new DependencyResolver().resolve(records);

        for (ModuleRecord record : loadOrder) {
            if (record.state != ModuleState.DISCOVERED) {
                failIfCritical(record);
                continue;
            }
            String unavailable = unavailableRequiredDependency(record);
            if (unavailable != null) {
                record.disable("required dependency is not enabled: " + unavailable);
                failIfCritical(record);
                continue;
            }
            boolean lifecycleStarted = false;
            try {
                lifecycleStarted = true;
                record.module.onLoad(new ModuleContext(services));
                record.state = ModuleState.LOADED;
                record.detail = "loaded";
                record.module.onEnable();
                record.state = ModuleState.ENABLED;
                record.detail = "enabled";
            } catch (Throwable failure) {
                if (lifecycleStarted) {
                    rollback(record, failure);
                }
                removeOwnedServices(record);
                record.fail("module lifecycle failed: " + failure.getClass().getSimpleName(),
                        failure);
                failIfCritical(record);
            }
        }
    }

    public synchronized void shutdownAll() {
        List<ModuleRecord> reverse = new ArrayList<ModuleRecord>(loadOrder);
        Collections.reverse(reverse);
        for (ModuleRecord record : reverse) {
            if (record.state != ModuleState.ENABLED) {
                continue;
            }
            try {
                record.module.onDisable();
                record.disable("shutdown");
            } catch (Throwable failure) {
                record.fail("module shutdown failed: " + failure.getClass().getSimpleName(),
                        failure);
            }
        }
    }

    private void rollback(ModuleRecord record, Throwable originalFailure) {
        try {
            record.module.onDisable();
        } catch (Throwable rollbackFailure) {
            originalFailure.addSuppressed(rollbackFailure);
        }
    }

    private void removeOwnedServices(ModuleRecord record) {
        if (services instanceof OwnedServiceRegistry) {
            ((OwnedServiceRegistry) services).removeOwnedBy(record.metadata().id());
        }
    }

    private String unavailableRequiredDependency(ModuleRecord record) {
        for (String dependency : record.metadata().requiredDependencies()) {
            ModuleRecord dependencyRecord = records.get(dependency);
            if (dependencyRecord == null || dependencyRecord.state != ModuleState.ENABLED) {
                return dependency;
            }
        }
        return null;
    }

    private void failIfCritical(ModuleRecord record) {
        if (record.metadata().isRequiredForCore()
                && record.state != ModuleState.ENABLED
                && record.state != ModuleState.DISCOVERED) {
            shutdownAll();
            throw new ModuleBootstrapException(
                    "Required Core module failed: " + record.metadata().id()
                            + " (" + record.detail + ")",
                    record.failure);
        }
    }

    static final class ModuleRecord {
        private final IFurusatoModule module;
        private final ModuleMetadata metadata;
        private ModuleState state = ModuleState.DISCOVERED;
        private String detail = "discovered";
        private Throwable failure;

        ModuleRecord(IFurusatoModule module, ModuleMetadata metadata) {
            this.module = module;
            this.metadata = metadata;
        }

        ModuleMetadata metadata() {
            return metadata;
        }

        void disable(String detail) {
            state = ModuleState.DISABLED;
            this.detail = detail;
            failure = null;
        }

        void fail(String detail, Throwable failure) {
            state = ModuleState.FAILED;
            this.detail = detail;
            this.failure = failure;
        }

        ModuleContainer snapshot() {
            return new ModuleContainer(metadata(), state, detail, failure);
        }
    }
}
