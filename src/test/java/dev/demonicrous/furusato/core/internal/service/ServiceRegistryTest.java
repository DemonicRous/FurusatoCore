package dev.demonicrous.furusato.core.internal.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import dev.demonicrous.furusato.api.service.DuplicateServiceException;
import dev.demonicrous.furusato.api.service.InvalidServiceImplementationException;
import dev.demonicrous.furusato.api.service.ServiceMetadata;
import dev.demonicrous.furusato.api.service.ServiceNotFoundException;
import dev.demonicrous.furusato.api.service.ServiceRegistryFrozenException;
import dev.demonicrous.furusato.api.service.ServiceSnapshot;
import dev.demonicrous.furusato.api.service.ServiceStatus;
import dev.demonicrous.furusato.api.service.ServiceThreadPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.Test;

public final class ServiceRegistryTest {
    @Test
    public void registersFindsAndFreezesService() {
        DefaultServiceRegistry registry = new DefaultServiceRegistry();
        Runnable service = runnable();

        registry.register(metadata("core.tasks", Runnable.class), service);

        assertSame(service, registry.get(Runnable.class));
        assertSame(service, registry.find(Runnable.class).get());
        assertEquals(1, registry.size());
        assertEquals(ServiceStatus.REGISTERED,
                registry.snapshots().iterator().next().status());

        registry.freeze();
        assertTrue(registry.isFrozen());
        assertEquals(ServiceStatus.AVAILABLE,
                registry.snapshots().iterator().next().status());
    }

    @Test
    public void optionalLookupReturnsEmpty() {
        assertFalse(new DefaultServiceRegistry().find(Runnable.class).isPresent());
    }

    @Test(expected = ServiceNotFoundException.class)
    public void getRejectsMissingService() {
        new DefaultServiceRegistry().get(Runnable.class);
    }

    @Test(expected = ServiceNotFoundException.class)
    public void requireRejectsMissingService() {
        new DefaultServiceRegistry().require(Runnable.class, "consumer");
    }

    @Test(expected = DuplicateServiceException.class)
    public void rejectsDuplicateContract() {
        DefaultServiceRegistry registry = new DefaultServiceRegistry();
        registry.register(metadata("core.first", Runnable.class), runnable());
        registry.register(metadata("core.second", Runnable.class), runnable());
    }

    @Test(expected = DuplicateServiceException.class)
    public void rejectsDuplicateIdAcrossContracts() {
        DefaultServiceRegistry registry = new DefaultServiceRegistry();
        registry.register(metadata("core.shared", Runnable.class), runnable());
        registry.register(metadata("core.shared", Callable.class), new Callable<Object>() {
            @Override
            public Object call() {
                return null;
            }
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test(expected = InvalidServiceImplementationException.class)
    public void rejectsImplementationThatDoesNotMatchContract() {
        DefaultServiceRegistry registry = new DefaultServiceRegistry();
        ServiceMetadata raw = metadata("core.invalid", Runnable.class);
        registry.register(raw, "not a runnable");
    }

    @Test(expected = ServiceRegistryFrozenException.class)
    public void rejectsRegistrationAfterFreeze() {
        DefaultServiceRegistry registry = new DefaultServiceRegistry();
        registry.freeze();
        registry.register(metadata("core.tasks", Runnable.class), runnable());
    }

    @Test
    public void tracksAndDeduplicatesConsumers() {
        DefaultServiceRegistry registry = new DefaultServiceRegistry();
        Runnable service = runnable();
        registry.register(metadata("core.tasks", Runnable.class), service);

        assertSame(service, registry.require(Runnable.class, "module.alpha"));
        assertSame(service, registry.require(Runnable.class, "module.alpha"));
        assertSame(service, registry.require(Runnable.class, "module.bravo"));

        ServiceSnapshot snapshot = registry.snapshots().iterator().next();
        assertEquals(2, snapshot.consumers().size());
        assertEquals("module.alpha", snapshot.consumers().get(0));
        assertEquals("module.bravo", snapshot.consumers().get(1));
    }

    @Test
    public void snapshotsAreOrderedByStableId() {
        DefaultServiceRegistry registry = new DefaultServiceRegistry();
        registry.register(metadata("zulu.service", Runnable.class), runnable());
        registry.register(metadata("alpha.service", Callable.class), new Callable<Object>() {
            @Override
            public Object call() {
                return null;
            }
        });

        List<ServiceSnapshot> snapshots =
                new ArrayList<ServiceSnapshot>(registry.snapshots());
        assertEquals("alpha.service", snapshots.get(0).id());
        assertEquals("zulu.service", snapshots.get(1).id());
    }

    @Test
    public void legacyRegistrationRemainsSupported() {
        DefaultServiceRegistry registry = new DefaultServiceRegistry();
        Runnable service = runnable();
        registry.register(Runnable.class, service);
        assertSame(service, registry.get(Runnable.class));
    }

    private <T> ServiceMetadata<T> metadata(String id, Class<T> contract) {
        return ServiceMetadata.builder(id, contract)
                .ownedBy("core")
                .threadPolicy(ServiceThreadPolicy.THREAD_SAFE)
                .build();
    }

    private Runnable runnable() {
        return new Runnable() {
            @Override
            public void run() {
            }
        };
    }
}
