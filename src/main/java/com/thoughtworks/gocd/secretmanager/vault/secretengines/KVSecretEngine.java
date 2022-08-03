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

import java.util.Map;
import java.util.Optional;

public class KVSecretEngine extends SecretEngine {

    private Map<String, String> secretsFromVault;

    public KVSecretEngine(Vault vault) {
        super(vault);
    }

    @Override
    public Optional<String> getSecret(String path, String key) throws APIException {
        if (secretsFromVault == null) {
            try {
                secretsFromVault = getSecretData(path);
            } catch (VaultException vaultException) {
                throw new APIException(vaultException);
            }
        }

        return Optional.ofNullable(secretsFromVault.get(key));
    }

    private Map<String, String> getSecretData(String path) throws VaultException {
        return getVault().logical()
                .read(path)
                .getData();
    }
}
