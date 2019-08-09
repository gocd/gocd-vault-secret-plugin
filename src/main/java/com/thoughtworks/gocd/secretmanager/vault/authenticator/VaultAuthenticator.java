package com.thoughtworks.gocd.secretmanager.vault.authenticator;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;

public interface VaultAuthenticator {
    String authenticate(Vault vault, SecretConfig secretConfig) throws VaultException;
}
