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

package com.thoughtworks.gocd.secretmanager.vault.models;

import cd.go.plugin.base.annotations.Property;
import com.google.gson.annotations.Expose;

import java.util.Objects;

public class SecretConfig {
    @Expose
    @Property(name = "vaultUrl", required = true)
    private String vaultUrl;

    @Expose
    @Property(name = "token", required = true, secure = true)
    private String token;

    @Expose
    @Property(name = "basePath", required = true)
    private String basePath;

    @Expose
    @Property(name = "connectionTimeout")
    private Integer connectionTimeout = 5;

    @Expose
    @Property(name = "connectionTimeout")
    private Integer readTimeout = 30;

    @Expose
    @Property(name = "clientKeyPem")
    private String clientKeyPem;

    @Expose
    @Property(name = "clientPem")
    private String clientPem;

    @Expose
    @Property(name = "serverPem")
    private String serverPem;

    public String getVaultUrl() {
        return vaultUrl;
    }

    public String getToken() {
        return token;
    }

    public String getBasePath() {
        return basePath;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public String getClientKeyPem() {
        return clientKeyPem;
    }

    public String getClientPem() {
        return clientPem;
    }

    public String getServerPem() {
        return serverPem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SecretConfig)) return false;
        SecretConfig that = (SecretConfig) o;
        return Objects.equals(vaultUrl, that.vaultUrl) &&
                Objects.equals(token, that.token) &&
                Objects.equals(basePath, that.basePath) &&
                Objects.equals(connectionTimeout, that.connectionTimeout) &&
                Objects.equals(readTimeout, that.readTimeout) &&
                Objects.equals(clientKeyPem, that.clientKeyPem) &&
                Objects.equals(clientPem, that.clientPem) &&
                Objects.equals(serverPem, that.serverPem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vaultUrl, token, basePath, connectionTimeout, readTimeout, clientKeyPem, clientPem, serverPem);
    }
}
