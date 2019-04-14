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

import java.util.Arrays;

import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.VALIDATION_FAILED;
import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.success;
import static java.lang.String.format;

public class ValidationExecutor implements Executor {
    private static Logger LOGGER = Logger.getLoggerFor(ValidationExecutor.class);
    private final Validator[] validators;

    public ValidationExecutor(Validator... validators) {
        this.validators = validators;
    }

    @Override
    public GoPluginApiResponse execute(GoPluginApiRequest request) {
        final ValidationResult mergedValidationResult = new ValidationResult();
        if (validators == null || validators.length == 0) {
            LOGGER.debug(format("No validators provided. Skipping the validation for request %s", request.requestName()));
            return success(GsonTransformer.toJson(mergedValidationResult));
        }


        Arrays.stream(validators).forEach(validator -> {
            if (validator != null) {
                mergedValidationResult.merge(validator.validate(request));
            }
        });

        if (mergedValidationResult.isEmpty()) {
            return success(GsonTransformer.toJson(mergedValidationResult));
        }

        return new DefaultGoPluginApiResponse(VALIDATION_FAILED, GsonTransformer.toJson(mergedValidationResult));
    }
}
