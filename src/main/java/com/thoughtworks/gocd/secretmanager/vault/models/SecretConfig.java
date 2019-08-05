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
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class SecretConfig {
    @Expose
    @SerializedName("VaultUrl")
    @Property(name = "VaultUrl", required = true)
    private String vaultUrl;

    @Expose
    @SerializedName("Token")
    @Property(name = "Token", required = true, secure = true)
    private String token;

    @Expose
    @SerializedName("VaultKey")
    @Property(name = "VaultKey", required = true)
    private String vaultKey;

    @Expose
    @SerializedName("ConnectionTimeout")
    @Property(name = "ConnectionTimeout")
    private Integer connectionTimeout = 5;

    @Expose
    @SerializedName("ReadTimeout")
    @Property(name = "ReadTimeout")
    private Integer readTimeout = 30;

    @Expose
    @SerializedName("ClientKeyPem")
    @Property(name = "ClientKeyPem")
    private String clientKeyPem;

    @Expose
    @SerializedName("ClientPem")
    @Property(name = "ClientPem")
    private String clientPem;

    @Expose
    @SerializedName("ServerPem")
    @Property(name = "ServerPem")
    private String serverPem;

    public String getVaultUrl() {
        return vaultUrl;
    }

    public String getToken() {
        return token;
    }

    public String getVaultKey() {
        return vaultKey;
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
                Objects.equals(vaultKey, that.vaultKey) &&
                Objects.equals(connectionTimeout, that.connectionTimeout) &&
                Objects.equals(readTimeout, that.readTimeout) &&
                Objects.equals(clientKeyPem, that.clientKeyPem) &&
                Objects.equals(clientPem, that.clientPem) &&
                Objects.equals(serverPem, that.serverPem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vaultUrl, token, vaultKey, connectionTimeout, readTimeout, clientKeyPem, clientPem, serverPem);
    }
}
