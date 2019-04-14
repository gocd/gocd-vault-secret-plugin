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
import org.apache.commons.lang3.StringUtils;

public class VaultConfigBuilder {
    public VaultConfig from(SecretConfig secretConfig) throws VaultException {
        return new VaultConfig()
                .address(secretConfig.getVaultUrl())
                .token(secretConfig.getToken())
                .openTimeout(secretConfig.getConnectionTimeout())
                .readTimeout(secretConfig.getReadTimeout())
                .sslConfig(sslConfig(secretConfig))
                .build();
    }

    private SslConfig sslConfig(SecretConfig secretConfig) throws VaultException {
        if (StringUtils.isNoneBlank(secretConfig.getClientKeyPem(), secretConfig.getClientPem(), secretConfig.getServerPem())) {
            return new SslConfig()
                    .clientKeyPemUTF8(secretConfig.getClientKeyPem())
                    .clientPemUTF8(secretConfig.getClientPem())
                    .pemUTF8(secretConfig.getServerPem())
                    .verify(true)
                    .build();
        }
        return null;
    }
}
