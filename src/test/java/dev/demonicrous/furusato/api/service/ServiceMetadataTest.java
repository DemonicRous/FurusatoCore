package dev.demonicrous.furusato.api.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class ServiceMetadataTest {
    @Test
    public void retainsDeclaredContractOwnershipAndPolicy() {
        ServiceMetadata<Runnable> metadata =
                ServiceMetadata.builder("core.tasks", Runnable.class)
                        .ownedBy("scheduler")
                        .threadPolicy(ServiceThreadPolicy.CLIENT_THREAD_ONLY)
                        .build();

        assertEquals("core.tasks", metadata.id());
        assertEquals(Runnable.class, metadata.contract());
        assertEquals("scheduler", metadata.ownerModuleId());
        assertEquals(ServiceThreadPolicy.CLIENT_THREAD_ONLY, metadata.threadPolicy());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidServiceId() {
        ServiceMetadata.builder("Invalid ID", Runnable.class).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidOwnerModuleId() {
        ServiceMetadata.builder("core.tasks", Runnable.class)
                .ownedBy("Invalid Owner")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsConcreteClassAsServiceContract() {
        ServiceMetadata.builder("core.string", String.class).build();
    }
}
