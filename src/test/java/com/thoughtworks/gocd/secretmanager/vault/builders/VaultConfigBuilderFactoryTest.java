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