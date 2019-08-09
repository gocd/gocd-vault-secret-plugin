package com.thoughtworks.gocd.secretmanager.vault;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.thoughtworks.gocd.secretmanager.vault.authenticator.VaultAuthenticator;
import com.thoughtworks.gocd.secretmanager.vault.authenticator.VaultAuthenticatorFactory;
import com.thoughtworks.gocd.secretmanager.vault.builders.VaultConfigBuilder;
import com.thoughtworks.gocd.secretmanager.vault.builders.VaultConfigBuilderFactory;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;

public class VaultProvider {
    private final VaultConfigBuilderFactory vaultConfigBuilderFactory;
    private final VaultAuthenticatorFactory vaultAuthenticatorFactory;

//  Used only in tests
    VaultProvider(VaultConfigBuilderFactory vaultConfigBuilderFactory, VaultAuthenticatorFactory vaultAuthenticatorFactory) {
        this.vaultConfigBuilderFactory = vaultConfigBuilderFactory;
        this.vaultAuthenticatorFactory = vaultAuthenticatorFactory;
    }

    public VaultProvider() {
        this(new VaultConfigBuilderFactory(), new VaultAuthenticatorFactory());
    }

    public Vault vaultFor(SecretConfig secretConfig) throws VaultException {
        VaultConfigBuilder configBuilder = vaultConfigBuilderFactory.builderFor(secretConfig);
        VaultConfig vaultConfig = configBuilder.configFrom(secretConfig);

        VaultAuthenticator vaultAuthenticator = vaultAuthenticatorFactory.authenticatorFor(secretConfig);
        Vault vault = new Vault(vaultConfig);

        String token = vaultAuthenticator.authenticate(vault, secretConfig);

        vaultConfig.token(token);

        return vault;
    }
}
