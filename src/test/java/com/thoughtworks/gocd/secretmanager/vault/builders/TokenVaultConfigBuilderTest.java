package com.thoughtworks.gocd.secretmanager.vault.builders;

import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TokenVaultConfigBuilderTest {

    @Nested
    class configFrom {
        @Test
        void shouldBuildVaultConfigFromProvidedSecretConfig() throws VaultException {
            SecretConfig secretConfig = secretConfigWith("https://foo.bar", "10", "10", "token", "");

            VaultConfig vaultConfig = new TokenVaultConfigBuilder().configFrom(secretConfig);

            assertThat(vaultConfig.getAddress()).isEqualTo(secretConfig.getVaultUrl());
            assertThat(vaultConfig.getReadTimeout()).isEqualTo(secretConfig.getReadTimeout());
            assertThat(vaultConfig.getOpenTimeout()).isEqualTo(secretConfig.getConnectionTimeout());
            assertThat(vaultConfig.getSslConfig().isVerify()).isTrue();
        }
    }

    private SecretConfig secretConfigWith(String vaultUrl, String connectionTimeout, String readTimeout, String authMethod, String serverPem) {
        Map<String, String> secretConfigMap = new HashMap<>();

        secretConfigMap.put("VaultUrl", vaultUrl);
        secretConfigMap.put("ConnectionTimeout", connectionTimeout);
        secretConfigMap.put("ReadTimeout", readTimeout);
        secretConfigMap.put("AuthMethod", authMethod);
        secretConfigMap.put("ServerPem", serverPem);

        return SecretConfig.fromJSON(secretConfigMap);
    }
}