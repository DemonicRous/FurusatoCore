package dev.demonicrous.furusato.core.internal.service;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class DefaultServiceRegistryTest {
    @Test
    public void registersFindsAndFreezesService() {
        DefaultServiceRegistry registry = new DefaultServiceRegistry();
        Runnable service = new Runnable() {
            @Override
            public void run() {
            }
        };

        registry.register(Runnable.class, service);
        assertSame(service, registry.get(Runnable.class));
        registry.freeze();
        assertTrue(registry.isFrozen());
    }

    @Test(expected = IllegalStateException.class)
    public void rejectsRegistrationAfterFreeze() {
        DefaultServiceRegistry registry = new DefaultServiceRegistry();
        registry.freeze();
        registry.register(Runnable.class, new Runnable() {
            @Override
            public void run() {
            }
        });
    }
}
