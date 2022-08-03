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

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.thoughtworks.gocd.secretmanager.vault.authenticator.VaultAuthenticator;
import com.thoughtworks.gocd.secretmanager.vault.authenticator.VaultAuthenticatorFactory;
import com.thoughtworks.gocd.secretmanager.vault.builders.VaultConfigBuilder;
import com.thoughtworks.gocd.secretmanager.vault.builders.VaultConfigBuilderFactory;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;

public class VaultProvider {
    private final VaultConfigBuilderFactory vaultConfigBuilderFactory;
    private final VaultAuthenticatorFactory vaultAuthenticatorFactory;

    private VaultConfig vaultConfig;

    //  Used only in tests
    VaultProvider(VaultConfigBuilderFactory vaultConfigBuilderFactory, VaultAuthenticatorFactory vaultAuthenticatorFactory) {
        this.vaultConfigBuilderFactory = vaultConfigBuilderFactory;
        this.vaultAuthenticatorFactory = vaultAuthenticatorFactory;
    }

    public VaultProvider() {
        this(new VaultConfigBuilderFactory(), new VaultAuthenticatorFactory());
    }

    public Vault vaultFor(SecretConfig secretConfig) throws VaultException {
        vaultConfig = vaultConfig(secretConfig);
        Vault vault = new Vault(vaultConfig)
                .withRetries(secretConfig.getMaxRetries(), secretConfig.getRetryIntervalMilliseconds());
        String token = authenticate(vault, secretConfig);
        vaultConfig.token(token);
        return vault;
    }

    private VaultConfig vaultConfig(SecretConfig secretConfig) throws VaultException {
        VaultConfigBuilder configBuilder = vaultConfigBuilderFactory.builderFor(secretConfig);
        VaultConfig vaultConfig = configBuilder.configFrom(secretConfig);
        return vaultConfig;
    }

    private String authenticate(Vault vault, SecretConfig secretConfig) throws VaultException {
        VaultAuthenticator vaultAuthenticator = vaultAuthenticatorFactory.authenticatorFor(secretConfig);
        return vaultAuthenticator.authenticate(vault, secretConfig);
    }

    public VaultConfig getVaultConfig() {
        return vaultConfig;
    }
}
