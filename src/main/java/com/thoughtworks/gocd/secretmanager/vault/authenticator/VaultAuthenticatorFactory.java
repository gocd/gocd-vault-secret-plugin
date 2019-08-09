package com.thoughtworks.gocd.secretmanager.vault.authenticator;

import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;

import java.util.HashMap;
import java.util.Map;

public class VaultAuthenticatorFactory {
    private static Map<String, VaultAuthenticator> authMethodToAuthenticator = new HashMap<>();

    static {
        authMethodToAuthenticator.put(SecretConfig.TOKEN_AUTH_METHOD, new TokenAuthenticator());
        authMethodToAuthenticator.put(SecretConfig.APPROLE_AUTH_METHOD, new AppRoleAuthenticator());
        authMethodToAuthenticator.put(SecretConfig.CERT_AUTH_METHOD, new CertAuthenticator());
    }

    public VaultAuthenticator authenticatorFor(SecretConfig secretConfig) {
        return authMethodToAuthenticator.get(secretConfig.getAuthMethod());
    }
}
