package com.thoughtworks.gocd.secretmanager.vault.authenticator;

import com.bettercloud.vault.Vault;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;

public class TokenAuthenticator implements VaultAuthenticator {
    @Override
    public String authenticate(Vault vault, SecretConfig secretConfig) {
        return secretConfig.getToken();
    }
}

