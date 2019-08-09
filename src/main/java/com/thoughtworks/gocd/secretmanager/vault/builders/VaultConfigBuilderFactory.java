package com.thoughtworks.gocd.secretmanager.vault.builders;

import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;

import java.util.HashMap;
import java.util.Map;

public class VaultConfigBuilderFactory {
    private static Map<String, VaultConfigBuilder> authMethodToConfigBuilder = new HashMap<>();

    static {
        authMethodToConfigBuilder.put(SecretConfig.TOKEN_AUTH_METHOD, new TokenVaultConfigBuilder());
        authMethodToConfigBuilder.put(SecretConfig.APPROLE_AUTH_METHOD, new AppRoleVaultConfigBuilder());
        authMethodToConfigBuilder.put(SecretConfig.CERT_AUTH_METHOD, new CertVaultConfigBuilder());
    }

    public VaultConfigBuilder builderFor(SecretConfig secretConfig) {
        return authMethodToConfigBuilder.get(secretConfig.getAuthMethod());
    }
}
