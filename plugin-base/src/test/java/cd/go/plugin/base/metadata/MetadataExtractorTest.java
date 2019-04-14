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

package cd.go.plugin.base.metadata;

import cd.go.plugin.base.annotations.Property;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class MetadataExtractorTest {
    @Test
    void shouldExtractMedata() {
        final List<MetadataHolder> metadataList = new MetadataExtractor().forClass(ValidType.class);

        assertThat(metadataList)
                .hasSize(3)
                .contains(
                        new MetadataHolder("name", "Name", false, true),
                        new MetadataHolder("lastname", "", false, false),
                        new MetadataHolder("password", "", true, true)
                );
    }

    @Test
    void shouldErrorOutWhenFieldHasExposeAnnotationWithoutPropertyAnnotation() {
        assertThatCode(() -> new MetadataExtractor().forClass(ExposedWithoutProperty.class))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Must have @Property annotation along with @Expose.");
    }

    @Test
    void shouldErrorOutIfExposedFieldNameIsDifferentThenTheNameSpecifiedInPropertyInAbsenceOfSerializeName() {
        assertThatCode(() -> new MetadataExtractor().forClass(DifferentFieldName.class))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Must have @SerializeName annotation along with @Expose.");
    }

    @Test
    void shouldErrorOutIfSerializeNameAndPropertyNameIsNotSame() {
        assertThatCode(() -> new MetadataExtractor().forClass(SerializeNameIsNotSameAsPropertyName.class))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("@Property name is not same as @SerializeName value for field 'name'.");
    }


    class SerializeNameIsNotSameAsPropertyName {
        @Expose
        @Property(name = "username", required = true)
        @SerializedName("name")
        private String name = "bob";
    }

    class DifferentFieldName {
        @Expose
        @Property(name = "username", required = true)
        private String name = "bob";
    }

    class ExposedWithoutProperty {
        @Expose
        private String name = "bob";
    }

    class ValidType {
        @Expose
        @Property(name = "name", displayName = "Name", required = true)
        private String name = "bob";

        @Expose
        @SerializedName("lastname")
        @Property(name = "lastname")
        private String ln = "Ford";

        @Expose
        @Property(name = "password", required = true, secure = true)
        private String password = "s3cr3t";

        private String excludeMe = "Not considering field without @Expose annotation";
    }
}