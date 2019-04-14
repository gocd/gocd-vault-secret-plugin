/*
 * Copyright 2019 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.plugin.base.validation;

import cd.go.plugin.base.metadata.MetadataExtractor;
import cd.go.plugin.base.metadata.MetadataHolder;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cd.go.plugin.base.GsonTransformer.fromJson;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class DefaultValidator implements Validator {
    @Override
    public ValidationResult validate(GoPluginApiRequest request) {
        final Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        final Map<String, String> secretConfigs = fromJson(request.requestBody(), type);
        final List<MetadataHolder> metadataHolders = new MetadataExtractor().forClass(SecretConfig.class);

        final ValidationResult validationResult = new ValidationResult();
        Set<String> knownFields = new HashSet<>();
        metadataHolders.forEach(metadataHolder -> {
            knownFields.add(metadataHolder.getKey());
            final String value = secretConfigs.get(metadataHolder.getKey());
            if (metadataHolder.getMetadata().isRequired() && isBlank(value)) {
                validationResult.add(metadataHolder.getKey(), format("%s must not be blank.", metadataHolder.getKey()));
            }
        });

        final Set<String> unknownFields = new HashSet<>(secretConfigs.keySet());
        unknownFields.removeAll(knownFields);
        unknownFields.forEach(field -> validationResult.add(field, "Is an unknown property"));

        return validationResult;
    }
}
