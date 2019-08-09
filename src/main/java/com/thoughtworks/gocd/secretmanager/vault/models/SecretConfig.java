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

import cd.go.plugin.base.GsonTransformer;
import cd.go.plugin.base.annotations.Property;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Arrays.asList;

public class SecretConfig {
    private static final Gson GSON = new GsonBuilder().
            excludeFieldsWithoutExposeAnnotation().
            create();

    public static final String TOKEN_AUTH_METHOD = "token";
    public static final String APPROLE_AUTH_METHOD = "approle";
    public static final String CERT_AUTH_METHOD = "cert";

    private List<String> supportedAuthMethods = asList(TOKEN_AUTH_METHOD, APPROLE_AUTH_METHOD, CERT_AUTH_METHOD);

    @Expose
    @SerializedName("VaultUrl")
    @Property(name = "VaultUrl", required = true)
    private String vaultUrl;

    @Expose
    @SerializedName("VaultPath")
    @Property(name = "VaultPath", required = true)
    private String vaultPath;

    @Expose
    @SerializedName("ConnectionTimeout")
    @Property(name = "ConnectionTimeout")
    private Integer connectionTimeout = 5;

    @Expose
    @SerializedName("ReadTimeout")
    @Property(name = "ReadTimeout")
    private Integer readTimeout = 30;

    @Expose
    @SerializedName("AuthMethod")
    @Property(name = "AuthMethod", required = true)
    private String authMethod;

    @Expose
    @SerializedName("Token")
    @Property(name = "Token", secure = true)
    private String token;

    @Expose
    @SerializedName("RoleId")
    @Property(name = "RoleId")
    private String roleId;

    @Expose
    @SerializedName("SecretId")
    @Property(name = "SecretId", secure = true)
    private String secretId;

    @Expose
    @SerializedName("ClientKeyPem")
    @Property(name = "ClientKeyPem", secure = true)
    private String clientKeyPem;

    @Expose
    @SerializedName("ClientPem")
    @Property(name = "ClientPem", secure = true)
    private String clientPem;

    @Expose
    @SerializedName("ServerPem")
    @Property(name = "ServerPem", secure = true)
    private String serverPem;

    public String getVaultUrl() {
        return vaultUrl;
    }

    public String getVaultPath() {
        return vaultPath;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public String getToken() {
        return token;
    }

    public String getRoleId() {
        return roleId;
    }

    public String getSecretId() {
        return secretId;
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

    public boolean isAuthMethodSupported() {
        return supportedAuthMethods.contains(authMethod.toLowerCase());
    }

    public static SecretConfig fromJSON(Map<String, String>     request) {
        String json = GsonTransformer.toJson(request);
        return GSON.fromJson(json, SecretConfig.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecretConfig that = (SecretConfig) o;
        return Objects.equals(vaultUrl, that.vaultUrl) &&
                Objects.equals(vaultPath, that.vaultPath) &&
                Objects.equals(connectionTimeout, that.connectionTimeout) &&
                Objects.equals(readTimeout, that.readTimeout) &&
                Objects.equals(authMethod, that.authMethod) &&
                Objects.equals(token, that.token) &&
                Objects.equals(roleId, that.roleId) &&
                Objects.equals(secretId, that.secretId) &&
                Objects.equals(clientKeyPem, that.clientKeyPem) &&
                Objects.equals(clientPem, that.clientPem) &&
                Objects.equals(serverPem, that.serverPem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vaultUrl, vaultPath, connectionTimeout, readTimeout, authMethod, token, roleId, secretId, clientKeyPem, clientPem, serverPem);
    }

    public boolean isTokenAuthentication() {
        return TOKEN_AUTH_METHOD.equalsIgnoreCase(authMethod);
    }

    public boolean isAppRoleAuthentication() {
        return APPROLE_AUTH_METHOD.equalsIgnoreCase(authMethod);
    }

    public boolean isCertAuthentication() {
        return CERT_AUTH_METHOD.equalsIgnoreCase(authMethod);
    }
}
