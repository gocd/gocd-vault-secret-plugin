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

public class VaultProvider {
    private final VaultConfigBuilderFactory vaultConfigBuilderFactory;
    private final VaultAuthenticatorFactory vaultAuthenticatorFactory;

//  Used only in tests
    VaultProvider(VaultConfigBuilderFactory vaultConfigBuilderFactory, VaultAuthenticatorFactory vaultAuthenticatorFactory) {
        this.vaultConfigBuilderFactory = vaultConfigBuilderFactory;
        this.vaultAuthenticatorFactory = vaultAuthenticatorFactory;
    }

    public VaultProvider() {
        this(new VaultConfigBuilderFactory(), new VaultAuthenticatorFactory());
    }

    public Vault vaultFor(SecretConfig secretConfig) throws VaultException {
        VaultConfigBuilder configBuilder = vaultConfigBuilderFactory.builderFor(secretConfig);
        VaultConfig vaultConfig = configBuilder.configFrom(secretConfig);

        VaultAuthenticator vaultAuthenticator = vaultAuthenticatorFactory.authenticatorFor(secretConfig);
        Vault vault = Vault.create(vaultConfig)
                .withRetries(secretConfig.getMaxRetries(), secretConfig.getRetryIntervalMilliseconds());

        String token = vaultAuthenticator.authenticate(vault, secretConfig);

        vaultConfig.token(token);

        return vault;
    }
}
