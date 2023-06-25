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

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;

import static com.thoughtworks.gocd.secretmanager.vault.VaultPlugin.*;

public class VaultConfigBuilder {
    public VaultConfig configFrom(SecretConfig secretConfig) throws VaultException {
        VaultConfig request = new VaultConfig()
                .address(secretConfig.getVaultUrl())
                .openTimeout(secretConfig.getConnectionTimeout())
                .readTimeout(secretConfig.getReadTimeout())
                .sslConfig(sslConfig(secretConfig).build());
        if (!isBlank(secretConfig.getNameSpace()))
            request = request.nameSpace(secretConfig.getNameSpace());
        return request.build();
    }

    protected SslConfig sslConfig(SecretConfig secretConfig) {
        if (!isBlank(secretConfig.getServerPem())) {
            return new SslConfig()
                    .pemUTF8(secretConfig.getServerPem())
                    .verify(true);
        }

        return new SslConfig();
    }
}
