package dev.demonicrous.furusato.core.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dev.demonicrous.furusato.api.module.IFurusatoModule;
import dev.demonicrous.furusato.api.module.ModuleContainer;
import dev.demonicrous.furusato.api.module.ModuleContext;
import dev.demonicrous.furusato.api.module.ModuleMetadata;
import dev.demonicrous.furusato.api.module.ModuleState;
import dev.demonicrous.furusato.api.service.ServiceMetadata;
import dev.demonicrous.furusato.api.service.ServiceThreadPolicy;
import dev.demonicrous.furusato.core.internal.service.DefaultServiceRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public final class ModuleManagerTest {
    @Test
    public void loadsDependenciesInDeterministicOrder() {
        List<String> events = new ArrayList<String>();
        ModuleManager manager = manager();
        manager.register(module(metadata("feature").requires("base").build(), events));
        manager.register(module(metadata("base").build(), events));

        manager.loadAll();

        assertEquals(Arrays.asList("load:base", "enable:base",
                "load:feature", "enable:feature"), events);
        assertState(manager, "base", ModuleState.ENABLED);
        assertState(manager, "feature", ModuleState.ENABLED);
    }

    @Test
    public void disablesModuleWithMissingRequiredDependency() {
        ModuleManager manager = manager();
        manager.register(module(metadata("feature").requires("missing").build(),
                new ArrayList<String>()));

        manager.loadAll();

        ModuleContainer feature = manager.find("feature").get();
        assertEquals(ModuleState.DISABLED, feature.state());
        assertTrue(feature.statusDetail().contains("missing"));
    }

    @Test
    public void ignoresAbsentOptionalDependency() {
        ModuleManager manager = manager();
        manager.register(module(metadata("feature").optionallyDependsOn("optional").build(),
                new ArrayList<String>()));

        manager.loadAll();

        assertState(manager, "feature", ModuleState.ENABLED);
    }

    @Test
    public void marksDependencyCycleAsFailed() {
        ModuleManager manager = manager();
        manager.register(module(metadata("alpha").requires("bravo").build(),
                new ArrayList<String>()));
        manager.register(module(metadata("bravo").requires("alpha").build(),
                new ArrayList<String>()));

        manager.loadAll();

        assertState(manager, "alpha", ModuleState.FAILED);
        assertState(manager, "bravo", ModuleState.FAILED);
        assertTrue(manager.find("alpha").get().statusDetail().contains("cycle"));
    }

    @Test(expected = IllegalStateException.class)
    public void rejectsDuplicateId() {
        ModuleManager manager = manager();
        manager.register(module(metadata("duplicate").build(), new ArrayList<String>()));
        manager.register(module(metadata("duplicate").build(), new ArrayList<String>()));
    }

    @Test
    public void isolatesFailureAndDisablesRequiredDependent() {
        ModuleManager manager = manager();
        manager.register(failingModule(metadata("broken").build()));
        manager.register(module(metadata("dependent").requires("broken").build(),
                new ArrayList<String>()));
        manager.register(module(metadata("independent").build(), new ArrayList<String>()));

        manager.loadAll();

        assertState(manager, "broken", ModuleState.FAILED);
        assertState(manager, "dependent", ModuleState.DISABLED);
        assertState(manager, "independent", ModuleState.ENABLED);
    }

    @Test
    public void optionalDependencyFailureDoesNotDisableModule() {
        ModuleManager manager = manager();
        manager.register(failingModule(metadata("optional").build()));
        manager.register(module(metadata("feature").optionallyDependsOn("optional").build(),
                new ArrayList<String>()));

        manager.loadAll();

        assertState(manager, "optional", ModuleState.FAILED);
        assertState(manager, "feature", ModuleState.ENABLED);
    }

    @Test(expected = ModuleBootstrapException.class)
    public void criticalFailureAbortsBootstrap() {
        ModuleManager manager = manager();
        manager.register(failingModule(metadata("critical").requiredForCore().build()));
        manager.loadAll();
    }

    @Test
    public void rollsBackPartiallyLoadedModule() {
        final List<String> events = new ArrayList<String>();
        ModuleManager manager = manager();
        final ModuleMetadata metadata = metadata("broken").build();
        manager.register(new IFurusatoModule() {
            @Override
            public ModuleMetadata metadata() {
                return metadata;
            }

            @Override
            public void onLoad(ModuleContext context) {
                events.add("load:broken");
                throw new IllegalStateException("expected");
            }

            @Override
            public void onEnable() {
            }

            @Override
            public void onDisable() {
                events.add("disable:broken");
            }
        });

        manager.loadAll();

        assertEquals(Arrays.asList("load:broken", "disable:broken"), events);
        assertState(manager, "broken", ModuleState.FAILED);
    }

    @Test
    public void criticalFailureShutsDownPreviouslyEnabledModules() {
        List<String> events = new ArrayList<String>();
        ModuleManager manager = manager();
        manager.register(module(metadata("base").build(), events));
        manager.register(failingModule(metadata("critical").requiredForCore()
                .requires("base").build()));

        try {
            manager.loadAll();
        } catch (ModuleBootstrapException expected) {
            // Expected: verify cleanup below.
        }

        assertTrue(events.contains("disable:base"));
        assertState(manager, "base", ModuleState.DISABLED);
        assertState(manager, "critical", ModuleState.FAILED);
    }

    @Test
    public void removesServicesOwnedByFailedModule() {
        final DefaultServiceRegistry services = new DefaultServiceRegistry();
        ModuleManager manager = new ModuleManager(services);
        final ModuleMetadata metadata = metadata("broken").build();
        manager.register(new IFurusatoModule() {
            @Override
            public ModuleMetadata metadata() {
                return metadata;
            }

            @Override
            public void onLoad(ModuleContext context) {
                context.services().register(ServiceMetadata
                        .builder("broken.service", TestService.class)
                        .ownedBy("broken")
                        .threadPolicy(ServiceThreadPolicy.THREAD_SAFE)
                        .build(), new TestService() {
                        });
                throw new IllegalStateException("expected");
            }

            @Override
            public void onEnable() {
            }

            @Override
            public void onDisable() {
            }
        });

        manager.loadAll();

        assertFalse(services.find(TestService.class).isPresent());
        assertEquals(0, services.size());
    }

    @Test
    public void shutsDownInReverseLoadOrder() {
        List<String> events = new ArrayList<String>();
        ModuleManager manager = manager();
        manager.register(module(metadata("base").build(), events));
        manager.register(module(metadata("feature").requires("base").build(), events));
        manager.loadAll();
        events.clear();

        manager.shutdownAll();

        assertEquals(Arrays.asList("disable:feature", "disable:base"), events);
        assertState(manager, "base", ModuleState.DISABLED);
        assertState(manager, "feature", ModuleState.DISABLED);
    }

    private ModuleManager manager() {
        return new ModuleManager(new DefaultServiceRegistry());
    }

    private ModuleMetadata.Builder metadata(String id) {
        return ModuleMetadata.builder(id, id, "1.0.0");
    }

    private IFurusatoModule module(final ModuleMetadata metadata,
            final List<String> events) {
        return new IFurusatoModule() {
            @Override
            public ModuleMetadata metadata() {
                return metadata;
            }

            @Override
            public void onLoad(ModuleContext context) {
                events.add("load:" + metadata.id());
            }

            @Override
            public void onEnable() {
                events.add("enable:" + metadata.id());
            }

            @Override
            public void onDisable() {
                events.add("disable:" + metadata.id());
            }
        };
    }

    private IFurusatoModule failingModule(final ModuleMetadata metadata) {
        return new IFurusatoModule() {
            @Override
            public ModuleMetadata metadata() {
                return metadata;
            }

            @Override
            public void onLoad(ModuleContext context) {
                throw new IllegalStateException("expected test failure");
            }

            @Override
            public void onEnable() {
            }

            @Override
            public void onDisable() {
            }
        };
    }

    private void assertState(ModuleManager manager, String id, ModuleState state) {
        assertEquals(state, manager.find(id).get().state());
    }

    private interface TestService {
    }
}
