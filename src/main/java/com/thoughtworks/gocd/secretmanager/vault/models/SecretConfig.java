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

import java.util.*;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class SecretConfig {
    private static final Gson GSON = new GsonBuilder().
            excludeFieldsWithoutExposeAnnotation().
            create();

    public static final String TOKEN_AUTH_METHOD = "token";
    public static final String APPROLE_AUTH_METHOD = "approle";
    public static final String CERT_AUTH_METHOD = "cert";
    public static final String SECRET_ENGINE = "secret";
    public static final String OIDC_ENGINE = "oidc";

    public static final List<String> SUPPORTED_AUTH_METHODS = asList(TOKEN_AUTH_METHOD, APPROLE_AUTH_METHOD, CERT_AUTH_METHOD);
    public static final List<String> SUPPORTED_SECRET_ENGINES = asList(SECRET_ENGINE, OIDC_ENGINE);
    public static final int DEFAULT_CONNECTION_TIMEOUT = 5;
    public static final int DEFAULT_READ_TIMEOUT = 30;
    public static final int DEFAULT_MAX_RETRIES = 0;
    public static final int DEFAULT_RETRY_INTERVAL_MS = 100;
    public static final String DEFAULT_SECRET_ENGINE = SECRET_ENGINE;

    @Expose
    @SerializedName("VaultUrl")
    @Property(name = "VaultUrl", required = true)
    private String vaultUrl;

    @Expose
    @SerializedName("SecretEngine")
    @Property(name = "SecretEngine")
    private String secretEngine;

    @Expose
    @SerializedName("PipelineTokenAuthBackendRole")
    @Property(name = "PipelineTokenAuthBackendRole")
    private String pipelineTokenAuthBackendRole;

    @Expose
    @SerializedName("PipelinePolicy")
    @Property(name = "PipelinePolicy")
    private String pipelinePolicy;

    @Expose
    @SerializedName("VaultPath")
    @Property(name = "VaultPath", required = true)
    private String vaultPath;

    @Expose
    @SerializedName("NameSpace")
    @Property(name = "NameSpace")
    private String nameSpace;

    @Expose
    @SerializedName("ConnectionTimeout")
    @Property(name = "ConnectionTimeout")
    private String connectionTimeout;

    @Expose
    @SerializedName("ReadTimeout")
    @Property(name = "ReadTimeout")
    private String readTimeout;

    @Expose
    @SerializedName("MaxRetries")
    @Property(name = "MaxRetries")
    private String maxRetries;

    @Expose
    @SerializedName("RetryIntervalMilliseconds")
    @Property(name = "RetryIntervalMilliseconds")
    private String retryIntervalMilliseconds;

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

    @Expose
    @SerializedName("GoCDServerUrl")
    @Property(name = "GoCDServerUrl")
    private String gocdServerURL;

    @Expose
    @SerializedName("GoCDUsername")
    @Property(name = "GoCDUsername")
    private String gocdUsername;

    @Expose
    @SerializedName("GoCDPassword")
    @Property(name = "GoCDPassword", secure = true)
    private String gocdPassword;

    public String getVaultUrl() {
        return vaultUrl;
    }

    public String getVaultPath() {
        return vaultPath;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public Integer getConnectionTimeout() {
        if (isBlank(connectionTimeout)) {
            return DEFAULT_CONNECTION_TIMEOUT;
        }
        return Integer.valueOf(connectionTimeout);
    }

    public Integer getReadTimeout() {
        if (isBlank(readTimeout)) {
            return DEFAULT_READ_TIMEOUT;
        }
        return Integer.valueOf(readTimeout);
    }

    public Integer getMaxRetries() {
        if (isBlank(maxRetries)) {
            return DEFAULT_MAX_RETRIES;
        }
        return Integer.valueOf(maxRetries);
    }

    public Integer getRetryIntervalMilliseconds() {
        if (isBlank(retryIntervalMilliseconds)) {
            return DEFAULT_RETRY_INTERVAL_MS;
        }
        return Integer.valueOf(retryIntervalMilliseconds);
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

    public String getSecretEngine() {
        if (isBlank(secretEngine)) {
            return DEFAULT_SECRET_ENGINE;
        }
        return secretEngine;
    }

    public String getPipelineTokenAuthBackendRole() {
        return pipelineTokenAuthBackendRole;
    }

    public List<String> getPipelinePolicy() {
        if (isBlank(pipelinePolicy)) {
            return new ArrayList<>();
        }
        return Arrays.asList(pipelinePolicy.split(",\\s*"));
    }

    public String getGocdServerURL() {
        if (gocdServerURL.endsWith("/")) {
            return gocdServerURL.substring(0, gocdServerURL.length() - 1);
        }
        return gocdServerURL;
    }

    public String getGoCDUsername() {
        return gocdUsername;
    }

    public String getGoCDPassword() {
        return gocdPassword;
    }

    public boolean isAuthMethodSupported() {
        return SUPPORTED_AUTH_METHODS.contains(authMethod.toLowerCase());
    }

    public boolean isSecretEngineSupported() {
        return SUPPORTED_SECRET_ENGINES.contains(getSecretEngine().toLowerCase());
    }

    public static SecretConfig fromJSON(Map<String, String> request) {
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
                Objects.equals(nameSpace, that.nameSpace) &&
                Objects.equals(connectionTimeout, that.connectionTimeout) &&
                Objects.equals(readTimeout, that.readTimeout) &&
                Objects.equals(maxRetries, that.maxRetries) &&
                Objects.equals(retryIntervalMilliseconds, that.retryIntervalMilliseconds) &&
                Objects.equals(authMethod, that.authMethod) &&
                Objects.equals(token, that.token) &&
                Objects.equals(roleId, that.roleId) &&
                Objects.equals(secretId, that.secretId) &&
                Objects.equals(clientKeyPem, that.clientKeyPem) &&
                Objects.equals(clientPem, that.clientPem) &&
                Objects.equals(serverPem, that.serverPem) &&
                Objects.equals(secretEngine, that.secretEngine) &&
                Objects.equals(pipelineTokenAuthBackendRole, that.pipelineTokenAuthBackendRole) &&
                Objects.equals(pipelinePolicy, that.pipelinePolicy) &&
                Objects.equals(gocdUsername, that.gocdUsername) &&
                Objects.equals(gocdPassword, that.gocdPassword);

    }

    @Override
    public int hashCode() {
        return Objects.hash(vaultUrl, vaultPath, nameSpace, connectionTimeout, readTimeout, maxRetries, retryIntervalMilliseconds, authMethod, token, roleId, secretId, clientKeyPem, clientPem, serverPem, secretEngine);
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

    public boolean isOIDCSecretEngine() {
        return OIDC_ENGINE.equalsIgnoreCase(secretEngine);
    }
}
