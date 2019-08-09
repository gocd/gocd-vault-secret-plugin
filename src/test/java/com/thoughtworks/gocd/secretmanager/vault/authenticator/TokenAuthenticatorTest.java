package com.thoughtworks.gocd.secretmanager.vault.authenticator;

import com.bettercloud.vault.Vault;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

class TokenAuthenticatorTest {
    @Nested
    class authenticate {
        @Test
        void shouldBeNoopsAndReturnTokenInSecretConfig() {
            Map<String, String> secretConfigMap = new HashMap<>();
            secretConfigMap.put("AuthMethod", "token");
            secretConfigMap.put("Token", "auth_token");
            SecretConfig secretConfig = SecretConfig.fromJSON(secretConfigMap);
            Vault vault = mock(Vault.class);

            String token = new TokenAuthenticator().authenticate(vault, secretConfig);

            assertThat(token).isEqualTo(secretConfig.getToken());
            verifyZeroInteractions(vault);
        }
    }
}