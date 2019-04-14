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
import com.bettercloud.vault.VaultException;
import com.thoughtworks.gocd.secretmanager.vault.builders.VaultConfigBuilder;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;

public class ClientFactory {
    private SecretConfig secretConfig;
    private Vault vault;
    private static final ClientFactory CLIENT_FACTORY = new ClientFactory();
    private VaultConfigBuilder vaultConfigBuilder;

    private ClientFactory() {
        vaultConfigBuilder = new VaultConfigBuilder();
    }

    public Vault create(SecretConfig secretConfig) throws VaultException {
        if (!secretConfig.equals(this.secretConfig)) {
            vault = new Vault(vaultConfigBuilder.from(this.secretConfig));
            this.secretConfig = secretConfig;
        }

        return vault;
    }

    static ClientFactory instance() {
        return CLIENT_FACTORY;
    }
}
