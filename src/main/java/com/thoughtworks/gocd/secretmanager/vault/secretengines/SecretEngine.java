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

package com.thoughtworks.gocd.secretmanager.vault.secretengines;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.thoughtworks.gocd.secretmanager.vault.http.exceptions.APIException;

import java.util.Optional;

public abstract class SecretEngine {

    private final Vault vault;

    public SecretEngine(Vault vault) {
        this.vault = vault;
    }

    public abstract Optional<String> getSecret(String path, String key) throws APIException;

    public Vault getVault() {
        return vault;
    }
}
