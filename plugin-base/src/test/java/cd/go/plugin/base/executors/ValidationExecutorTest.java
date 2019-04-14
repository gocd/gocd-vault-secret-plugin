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

import cd.go.plugin.base.validation.ValidationResult;
import cd.go.plugin.base.validation.Validator;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class ValidationExecutorTest {

    @Test
    void shouldValidateRequestAgainstAllValidators() {
        final Validator validator1 = mock(Validator.class);
        final Validator validator2 = mock(Validator.class);
        final GoPluginApiRequest request = mock(GoPluginApiRequest.class);

        when(validator1.validate(request)).thenReturn(new ValidationResult());
        when(validator2.validate(request)).thenReturn(new ValidationResult());

        new ValidationExecutor(validator1, validator2).execute(request);

        final InOrder inOrder = inOrder(validator1, validator2);
        inOrder.verify(validator1).validate(request);
        inOrder.verify(validator2).validate(request);
    }

    @Test
    void shouldReturnSuccessResponseWhenThereIsNoValidationErrors() {
        final Validator validator = mock(Validator.class);
        final GoPluginApiRequest request = mock(GoPluginApiRequest.class);

        when(validator.validate(request)).thenReturn(new ValidationResult());

        final GoPluginApiResponse response = new ValidationExecutor(validator).execute(request);

        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo("[]");
    }

    @Test
    void shouldReturnValidationFailedResponseWhenThereIsValidationErrors() throws JSONException {
        final Validator validator = mock(Validator.class);
        final GoPluginApiRequest request = mock(GoPluginApiRequest.class);

        final ValidationResult validationResult = new ValidationResult();
        validationResult.add("Path", "Path must not be black.");
        when(validator.validate(request)).thenReturn(validationResult);

        final GoPluginApiResponse response = new ValidationExecutor(validator).execute(request);

        assertThat(response.responseCode()).isEqualTo(412);
        final String expectedResponse = "[\n" +
                "  {\n" +
                "    \"key\": \"Path\",\n" +
                "    \"message\": \"Path must not be black.\"\n" +
                "  }\n" +
                "]";

        assertEquals(expectedResponse, response.responseBody(), true);
    }
}