package com.thoughtworks.gocd.secretmanager.vault.builders;

import com.bettercloud.vault.SslConfig;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class CertVaultConfigBuilder extends VaultConfigBuilder {
    @Override
    protected SslConfig sslConfig(SecretConfig secretConfig) {
        SslConfig sslConfig = super.sslConfig(secretConfig);

        if (isNotBlank(secretConfig.getClientKeyPem()) && isNotBlank(secretConfig.getClientPem())) {
            sslConfig.clientPemUTF8(secretConfig.getClientPem());
            sslConfig.clientKeyPemUTF8(secretConfig.getClientKeyPem());
        }

        return sslConfig;
    }
}

