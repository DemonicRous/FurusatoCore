package dev.demonicrous.furusato.core.internal.service;

import dev.demonicrous.furusato.api.service.DuplicateServiceException;
import dev.demonicrous.furusato.api.service.IServiceRegistry;
import dev.demonicrous.furusato.api.service.InvalidServiceImplementationException;
import dev.demonicrous.furusato.api.service.ServiceMetadata;
import dev.demonicrous.furusato.api.service.ServiceNotFoundException;
import dev.demonicrous.furusato.api.service.ServiceRegistryFrozenException;
import dev.demonicrous.furusato.api.service.ServiceSnapshot;
import dev.demonicrous.furusato.api.service.ServiceStatus;
import dev.demonicrous.furusato.api.service.ServiceThreadPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public final class DefaultServiceRegistry implements IServiceRegistry, OwnedServiceRegistry {
    private Map<Class<?>, Entry<?>> entries = new HashMap<Class<?>, Entry<?>>();
    private final Map<String, Class<?>> contractsById = new HashMap<String, Class<?>>();
    private boolean frozen;

    @Override
    public synchronized <T> void register(Class<T> service, T implementation) {
        if (service == null) {
            throw new NullPointerException("service");
        }
        String legacyId = "service." + Integer.toHexString(service.getName().hashCode());
        register(ServiceMetadata.builder(legacyId, service)
                .ownedBy("unknown")
                .threadPolicy(ServiceThreadPolicy.THREAD_SAFE)
                .build(), implementation);
    }

    @Override
    public synchronized <T> void register(ServiceMetadata<T> metadata, T implementation) {
        if (frozen) {
            throw new ServiceRegistryFrozenException();
        }
        if (metadata == null || implementation == null) {
            throw new NullPointerException("metadata and implementation are required");
        }
        Class<T> contract = metadata.contract();
        if (!contract.isInstance(implementation)) {
            throw new InvalidServiceImplementationException(
                    contract, implementation.getClass());
        }
        if (entries.containsKey(contract)) {
            throw new DuplicateServiceException(
                    "Service contract is already registered: " + contract.getName());
        }
        if (contractsById.containsKey(metadata.id())) {
            throw new DuplicateServiceException(
                    "Service ID is already registered: " + metadata.id());
        }
        entries.put(contract, new Entry<T>(metadata, implementation));
        contractsById.put(metadata.id(), contract);
    }

    @Override
    public synchronized <T> T get(Class<T> service) {
        Entry<T> entry = entry(service);
        if (entry == null) {
            throw new ServiceNotFoundException(service);
        }
        return entry.implementation;
    }

    @Override
    public synchronized <T> Optional<T> find(Class<T> service) {
        Entry<T> entry = entry(service);
        return entry == null ? Optional.<T>empty() : Optional.of(entry.implementation);
    }

    @Override
    public synchronized <T> T require(Class<T> service, String consumerModuleId) {
        ServiceMetadata.validateId(consumerModuleId, "consumer module ID");
        Entry<T> entry = entry(service);
        if (entry == null) {
            throw new ServiceNotFoundException(service, consumerModuleId);
        }
        entry.consumers.add(consumerModuleId);
        return entry.implementation;
    }

    @Override
    public synchronized Collection<ServiceSnapshot> snapshots() {
        List<ServiceSnapshot> snapshots = new ArrayList<ServiceSnapshot>();
        ServiceStatus status = frozen ? ServiceStatus.AVAILABLE : ServiceStatus.REGISTERED;
        for (Entry<?> entry : entries.values()) {
            snapshots.add(entry.snapshot(status));
        }
        Collections.sort(snapshots, new Comparator<ServiceSnapshot>() {
            @Override
            public int compare(ServiceSnapshot left, ServiceSnapshot right) {
                return left.id().compareTo(right.id());
            }
        });
        return Collections.unmodifiableList(snapshots);
    }

    @Override
    public synchronized int size() {
        return entries.size();
    }

    @Override
    public synchronized boolean isFrozen() {
        return frozen;
    }

    public synchronized void freeze() {
        if (!frozen) {
            entries = Collections.unmodifiableMap(
                    new HashMap<Class<?>, Entry<?>>(entries));
            frozen = true;
        }
    }

    @Override
    public synchronized void removeOwnedBy(String ownerModuleId) {
        if (frozen) {
            throw new ServiceRegistryFrozenException();
        }
        Iterator<Map.Entry<Class<?>, Entry<?>>> iterator = entries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Class<?>, Entry<?>> mapEntry = iterator.next();
            if (mapEntry.getValue().metadata.ownerModuleId().equals(ownerModuleId)) {
                contractsById.remove(mapEntry.getValue().metadata.id());
                iterator.remove();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Entry<T> entry(Class<T> service) {
        if (service == null) {
            throw new NullPointerException("service");
        }
        return (Entry<T>) entries.get(service);
    }

    private static final class Entry<T> {
        private final ServiceMetadata<T> metadata;
        private final T implementation;
        private final Set<String> consumers = new ConcurrentSkipListSet<String>();

        private Entry(ServiceMetadata<T> metadata, T implementation) {
            this.metadata = metadata;
            this.implementation = implementation;
        }

        private ServiceSnapshot snapshot(ServiceStatus status) {
            return new ServiceSnapshot(
                    metadata.id(),
                    metadata.contract().getName(),
                    metadata.ownerModuleId(),
                    metadata.threadPolicy(),
                    status,
                    new ArrayList<String>(consumers));
        }
    }
}
