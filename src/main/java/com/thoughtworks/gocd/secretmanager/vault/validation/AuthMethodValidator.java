package com.thoughtworks.gocd.secretmanager.vault.validation;

import cd.go.plugin.base.validation.ValidationResult;
import cd.go.plugin.base.validation.Validator;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class AuthMethodValidator implements Validator {
    @Override
    public ValidationResult validate(Map<String, String> requestBody) {
        SecretConfig secretConfig = SecretConfig.fromJSON(requestBody);
        ValidationResult validationResult = new ValidationResult();

        if (isNotEmpty(secretConfig.getAuthMethod()) && !secretConfig.isAuthMethodSupported()) {
            validationResult.add("AuthMethod", "Invalid 'AuthMethod`, should be one of ['token', 'approle', 'cert']");
        }

        return validationResult;
    }
}
