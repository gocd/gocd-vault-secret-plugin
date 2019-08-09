package com.thoughtworks.gocd.secretmanager.vault.authenticator;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.AuthResponse;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;

public class CertAuthenticator implements VaultAuthenticator {
    @Override
    public String authenticate(Vault vault, SecretConfig secretConfig) throws VaultException {
        AuthResponse authResponse = vault.auth().loginByCert();

        return authResponse.getAuthClientToken();
    }
}
