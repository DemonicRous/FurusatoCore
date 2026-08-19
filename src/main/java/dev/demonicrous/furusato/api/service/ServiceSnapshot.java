package dev.demonicrous.furusato.api.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ServiceSnapshot {
    private final String id;
    private final String contractName;
    private final String ownerModuleId;
    private final ServiceThreadPolicy threadPolicy;
    private final ServiceStatus status;
    private final List<String> consumers;

    public ServiceSnapshot(String id, String contractName, String ownerModuleId,
            ServiceThreadPolicy threadPolicy, ServiceStatus status,
            List<String> consumers) {
        this.id = id;
        this.contractName = contractName;
        this.ownerModuleId = ownerModuleId;
        this.threadPolicy = threadPolicy;
        this.status = status;
        List<String> copy = new ArrayList<String>(consumers);
        Collections.sort(copy);
        this.consumers = Collections.unmodifiableList(copy);
    }

    public String id() {
        return id;
    }

    public String contractName() {
        return contractName;
    }

    public String ownerModuleId() {
        return ownerModuleId;
    }

    public ServiceThreadPolicy threadPolicy() {
        return threadPolicy;
    }

    public ServiceStatus status() {
        return status;
    }

    public List<String> consumers() {
        return consumers;
    }
}
