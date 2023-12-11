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

package com.thoughtworks.gocd.secretmanager.vault;

import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;
import com.thoughtworks.gocd.secretmanager.vault.authenticator.VaultAuthenticator;
import com.thoughtworks.gocd.secretmanager.vault.authenticator.VaultAuthenticatorFactory;
import com.thoughtworks.gocd.secretmanager.vault.builders.VaultConfigBuilder;
import com.thoughtworks.gocd.secretmanager.vault.builders.VaultConfigBuilderFactory;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@MockitoSettings
class VaultProviderTest {
    @Mock
    VaultConfigBuilderFactory vaultConfigBuilderFactory;

    @Mock
    VaultAuthenticatorFactory vaultAuthenticatorFactory;

    private VaultProvider vaultProvider;

    @BeforeEach
    void setUp() {
        vaultProvider = new VaultProvider(vaultConfigBuilderFactory, vaultAuthenticatorFactory);
    }

    @Nested
    class vaultFor {
        @Test
        void shouldReturnAuthenticatedVaultForASecretConfig() throws VaultException {
            SecretConfig secretConfig = mock(SecretConfig.class);
            VaultConfigBuilder configBuilder = mock(VaultConfigBuilder.class);
            VaultAuthenticator vaultAuthenticator = mock(VaultAuthenticator.class);
            VaultConfig vaultConfig = mock(VaultConfig.class);
            ArgumentCaptor<Vault> vaultCaptor = ArgumentCaptor.forClass(Vault.class);

            when(vaultConfigBuilderFactory.builderFor(secretConfig)).thenReturn(configBuilder);
            when(vaultAuthenticatorFactory.authenticatorFor(secretConfig)).thenReturn(vaultAuthenticator);
            when(configBuilder.configFrom(secretConfig)).thenReturn(vaultConfig);
            when(vaultAuthenticator.authenticate(vaultCaptor.capture(), eq(secretConfig))).thenReturn("token");

            Vault vault = vaultProvider.vaultFor(secretConfig);

            Vault captorValue = vaultCaptor.getValue();
            verify(vaultConfig).token("token");
            assertThat(vault).isEqualTo(captorValue);
        }
    }

}