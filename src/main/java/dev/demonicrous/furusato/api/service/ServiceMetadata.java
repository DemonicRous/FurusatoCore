package dev.demonicrous.furusato.api.service;

import java.util.regex.Pattern;

public final class ServiceMetadata<T> {
    private static final Pattern VALID_ID = Pattern.compile("[a-z][a-z0-9_.-]{1,63}");

    private final String id;
    private final Class<T> contract;
    private final String ownerModuleId;
    private final ServiceThreadPolicy threadPolicy;

    private ServiceMetadata(Builder<T> builder) {
        id = validateId(builder.id, "service ID");
        if (builder.contract == null) {
            throw new NullPointerException("contract");
        }
        contract = builder.contract;
        if (!contract.isInterface()) {
            throw new IllegalArgumentException(
                    "Service contract must be an interface: " + contract.getName());
        }
        ownerModuleId = validateId(builder.ownerModuleId, "owner module ID");
        if (builder.threadPolicy == null) {
            throw new NullPointerException("threadPolicy");
        }
        threadPolicy = builder.threadPolicy;
    }

    public static <T> Builder<T> builder(String id, Class<T> contract) {
        return new Builder<T>(id, contract);
    }

    public String id() {
        return id;
    }

    public Class<T> contract() {
        return contract;
    }

    public String ownerModuleId() {
        return ownerModuleId;
    }

    public ServiceThreadPolicy threadPolicy() {
        return threadPolicy;
    }

    public static String validateId(String value, String label) {
        if (value == null || !VALID_ID.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "Invalid " + label + " '" + value + "'; expected "
                            + VALID_ID.pattern());
        }
        return value;
    }

    public static final class Builder<T> {
        private final String id;
        private final Class<T> contract;
        private String ownerModuleId = "unknown";
        private ServiceThreadPolicy threadPolicy = ServiceThreadPolicy.THREAD_SAFE;

        private Builder(String id, Class<T> contract) {
            this.id = id;
            this.contract = contract;
        }

        public Builder<T> ownedBy(String moduleId) {
            ownerModuleId = moduleId;
            return this;
        }

        public Builder<T> threadPolicy(ServiceThreadPolicy policy) {
            threadPolicy = policy;
            return this;
        }

        public ServiceMetadata<T> build() {
            return new ServiceMetadata<T>(this);
        }
    }
}
