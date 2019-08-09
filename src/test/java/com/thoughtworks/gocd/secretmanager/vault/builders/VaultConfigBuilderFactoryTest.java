package com.thoughtworks.gocd.secretmanager.vault.builders;

import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

class VaultConfigBuilderFactoryTest {
    @Nested
    class builderFor {
        @Test
        void shouldReturnTokenVaultConfigBuilder_forTokenAuthMethod() {
            SecretConfig secretConfig = SecretConfig.fromJSON(singletonMap("AuthMethod", "token"));

            VaultConfigBuilder configBuilder = new VaultConfigBuilderFactory().builderFor(secretConfig);

            assertThat(configBuilder).isInstanceOf(TokenVaultConfigBuilder.class);
        }

        @Test
        void shouldReturnCertVaultConfigBuilder_forCertAuthMethod() {
            SecretConfig secretConfig = SecretConfig.fromJSON(singletonMap("AuthMethod", "cert"));

            VaultConfigBuilder configBuilder = new VaultConfigBuilderFactory().builderFor(secretConfig);

            assertThat(configBuilder).isInstanceOf(CertVaultConfigBuilder.class);
        }

        @Test
        void shouldReturnAppRoleVaultConfigBuilder_forAppRoleAuthMethod() {
            SecretConfig secretConfig = SecretConfig.fromJSON(singletonMap("AuthMethod", "approle"));

            VaultConfigBuilder configBuilder = new VaultConfigBuilderFactory().builderFor(secretConfig);

            assertThat(configBuilder).isInstanceOf(AppRoleVaultConfigBuilder.class);
        }
    }
}