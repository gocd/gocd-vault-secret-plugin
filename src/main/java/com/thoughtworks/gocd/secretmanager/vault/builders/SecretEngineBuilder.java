/*
 * Copyright 2022 ThoughtWorks, Inc.
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

import com.bettercloud.vault.Vault;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import com.thoughtworks.gocd.secretmanager.vault.secretengines.KVSecretEngine;
import com.thoughtworks.gocd.secretmanager.vault.secretengines.OIDCPipelineIdentityProvider;
import com.thoughtworks.gocd.secretmanager.vault.secretengines.SecretEngine;

public class SecretEngineBuilder {

    private Vault vault;
    private String secretEngineIdentifier;


    public SecretEngineBuilder secretConfig(SecretConfig secretConfig) {
        this.secretEngineIdentifier = secretConfig.getSecretEngine();
        return this;
    }

    public SecretEngineBuilder vault(Vault vault) {
        this.vault = vault;
        return this;
    }

    public SecretEngine build() {
        switch (secretEngineIdentifier) {
            case SecretConfig.OIDC_ENGINE:
                return new OIDCPipelineIdentityProvider(vault);
            case SecretConfig.SECRET_ENGINE:
            default:
                return new KVSecretEngine(vault);
        }
    }


}
