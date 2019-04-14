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

import java.util.Objects;

public class MetadataHolder {
    @Expose
    private String key;
    @Expose
    private Metadata metadata;

    MetadataHolder(Property property) {
        this(property.name(), property.displayName(), property.required(), property.secure());
    }

    public MetadataHolder(String name, String displayName, boolean required, boolean secure) {
        this.key = name;
        this.metadata = new Metadata(displayName, required, secure);
    }

    public String getKey() {
        return key;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetadataHolder)) return false;
        MetadataHolder that = (MetadataHolder) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, metadata);
    }
}
