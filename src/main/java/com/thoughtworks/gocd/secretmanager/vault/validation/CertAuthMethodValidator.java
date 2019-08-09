package com.thoughtworks.gocd.secretmanager.vault.validation;


import cd.go.plugin.base.validation.ValidationResult;
import cd.go.plugin.base.validation.Validator;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class CertAuthMethodValidator implements Validator {
    @Override
    public ValidationResult validate(Map<String, String> requestBody) {
        SecretConfig secretConfig = SecretConfig.fromJSON(requestBody);

        ValidationResult result = new ValidationResult();

        if (secretConfig.isCertAuthentication()) {
            if (isEmpty(secretConfig.getClientPem())) {
                result.add("ClientPem", "ClientPem must not be blank.");
            }

            if (isEmpty(secretConfig.getClientKeyPem())) {
                result.add("ClientKeyPem", "ClientKeyPem must not be blank.");
            }
        }

        return result;
    }
}
