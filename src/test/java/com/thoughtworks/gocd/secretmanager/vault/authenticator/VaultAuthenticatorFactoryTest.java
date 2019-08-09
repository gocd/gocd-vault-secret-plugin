package com.thoughtworks.gocd.secretmanager.vault.authenticator;

import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

class VaultAuthenticatorFactoryTest {
    @Nested
    class authenticatorFor {
        @Test
        void shouldReturnTokenVaultAuthenticator_forTokenAuthMethod() {
            SecretConfig secretConfig = SecretConfig.fromJSON(singletonMap("AuthMethod", "token"));

            VaultAuthenticator authenticator = new VaultAuthenticatorFactory().authenticatorFor(secretConfig);

            assertThat(authenticator).isInstanceOf(TokenAuthenticator.class);
        }

        @Test
        void shouldReturnCertVaultAuthenticator_forCertAuthMethod() {
            SecretConfig secretConfig = SecretConfig.fromJSON(singletonMap("AuthMethod", "cert"));

            VaultAuthenticator authenticator = new VaultAuthenticatorFactory().authenticatorFor(secretConfig);

            assertThat(authenticator).isInstanceOf(CertAuthenticator.class);
        }

        @Test
        void shouldReturnAppRoleVaultAuthenticator_forAppRoleAuthMethod() {
            SecretConfig secretConfig = SecretConfig.fromJSON(singletonMap("AuthMethod", "approle"));

            VaultAuthenticator authenticator = new VaultAuthenticatorFactory().authenticatorFor(secretConfig);

            assertThat(authenticator).isInstanceOf(AppRoleAuthenticator.class);
        }
    }

}