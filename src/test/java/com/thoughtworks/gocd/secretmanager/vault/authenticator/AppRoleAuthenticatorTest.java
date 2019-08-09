package com.thoughtworks.gocd.secretmanager.vault.authenticator;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Auth;
import com.bettercloud.vault.response.AuthResponse;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppRoleAuthenticatorTest {
    @Nested
    class authenticate {
        @Test
        void shouldAuthenticateWithVault() throws VaultException {
            Map<String, String> secretConfigMap = new HashMap<>();
            secretConfigMap.put("AuthMethod", "approle");
            secretConfigMap.put("RoleId", "some_role_id");
            secretConfigMap.put("SecretId", "some_secret_id");
            SecretConfig secretConfig = SecretConfig.fromJSON(secretConfigMap);
            Vault vault = mock(Vault.class);
            Auth auth = mock(Auth.class);
            AuthResponse authResponse = mock(AuthResponse.class);

            when(vault.auth()).thenReturn(auth);
            when(auth.loginByAppRole(secretConfig.getRoleId(), secretConfig.getSecretId())).thenReturn(authResponse);
            when(authResponse.getAuthClientToken()).thenReturn("auth_token");

            String token = new AppRoleAuthenticator().authenticate(vault, secretConfig);

            assertThat(token).isEqualTo("auth_token");
        }
    }
}