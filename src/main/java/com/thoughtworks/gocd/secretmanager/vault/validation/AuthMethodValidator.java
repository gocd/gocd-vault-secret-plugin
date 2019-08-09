package com.thoughtworks.gocd.secretmanager.vault.validation;

import cd.go.plugin.base.validation.ValidationResult;
import cd.go.plugin.base.validation.Validator;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;

import java.util.Map;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.join;

public class AuthMethodValidator implements Validator {
    @Override
    public ValidationResult validate(Map<String, String> requestBody) {
        SecretConfig secretConfig = SecretConfig.fromJSON(requestBody);
        ValidationResult validationResult = new ValidationResult();

        if (isNotEmpty(secretConfig.getAuthMethod()) && !secretConfig.isAuthMethodSupported()) {
            validationResult.add("AuthMethod", format("Invalid 'AuthMethod`, should be one of [%s]", join(SecretConfig.SUPPORTED_AUTH_METHODS, ",")));
        }

        return validationResult;
    }
}
