/*
 * Copyright 2019 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thoughtworks.gocd.secretmanager.vault.builders;

import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CertVaultConfigBuilderTest {
    @Nested
    class configFrom {
        @Test
        void shouldBuildVaultConfigFromProvidedSecretConfig() throws VaultException {
            SecretConfig secretConfig = secretConfigWith("https://foo.bar", "10", "10",
                    "cert", "", null);

            VaultConfig vaultConfig = new CertVaultConfigBuilder().configFrom(secretConfig);

            assertThat(vaultConfig.getAddress()).isEqualTo(secretConfig.getVaultUrl());
            assertThat(vaultConfig.getReadTimeout()).isEqualTo(secretConfig.getReadTimeout());
            assertThat(vaultConfig.getOpenTimeout()).isEqualTo(secretConfig.getConnectionTimeout());
            assertThat(vaultConfig.getSslConfig().isVerify()).isTrue();
            assertThat(vaultConfig.getNameSpace()).isNullOrEmpty();
            assertThat(vaultConfig.getNameSpace()).isNullOrEmpty();
        }

        @Test
        void shouldUseNamespaceIfConfigured() throws VaultException {
            SecretConfig secretConfig = secretConfigWith("https://foo.bar", "10", "10", "cert", "", "test");

            VaultConfig vaultConfig = new CertVaultConfigBuilder().configFrom(secretConfig);

            assertThat(vaultConfig.getNameSpace()).isEqualTo(secretConfig.getNameSpace());
        }
    }

    private SecretConfig secretConfigWith(String vaultUrl, String connectionTimeout, String readTimeout, String authMethod,
                                          String serverPem, String namespace) {
        Map<String, String> secretConfigMap = new HashMap<>();

        secretConfigMap.put("VaultUrl", vaultUrl);
        secretConfigMap.put("ConnectionTimeout", connectionTimeout);
        secretConfigMap.put("ReadTimeout", readTimeout);
        secretConfigMap.put("AuthMethod", authMethod);
        secretConfigMap.put("ServerPem", serverPem);
        secretConfigMap.put("Namespace", namespace);

        return SecretConfig.fromJSON(secretConfigMap);
    }
}
