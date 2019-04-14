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

package cd.go.plugin.base.executors;

import cd.go.plugin.base.GsonTransformer;
import cd.go.plugin.base.validation.ValidationResult;
import cd.go.plugin.base.validation.Validator;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.ArrayList;
import java.util.List;

import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.VALIDATION_FAILED;
import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.success;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public class ValidationExecutor implements Executor {
    private static Logger LOGGER = Logger.getLoggerFor(ValidationExecutor.class);
    private final List<Validator> validators = new ArrayList<>();

    public ValidationExecutor(Validator... validators) {
        this.validators.addAll(asList(validators));
    }

    public void addAll(Validator... validators) {
        this.validators.addAll(asList(validators));
    }

    @Override
    public GoPluginApiResponse execute(GoPluginApiRequest request) {
        final ValidationResult validationResult = new ValidationResult();
        if (validators.isEmpty()) {
            LOGGER.debug(format("No validator(s) are provided. Skipping the validation for request %s", request.requestName()));
            return success(GsonTransformer.toJson(validationResult));
        }

        validators.forEach(validator -> {
            if (validator != null) {
                validationResult.merge(validator.validate(request));
            }
        });

        if (validationResult.isEmpty()) {
            LOGGER.debug("Validation successful.");
            return success(GsonTransformer.toJson(validationResult));
        }

        LOGGER.debug(format("Validation failed %s.", validationResult));
        return new DefaultGoPluginApiResponse(VALIDATION_FAILED, GsonTransformer.toJson(validationResult));
    }
}
