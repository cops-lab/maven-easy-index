/*
 * Copyright 2022 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.c0ps.maveneasyindex;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class ArtifactModule extends SimpleModule {

    private static final long serialVersionUID = -1L;
    private static final String CENTRAL = "https://repo.maven.apache.org/maven2/";

    private static boolean isNullOrCentral(Artifact a) {
        return a.repository == null || CENTRAL.equals(a.repository);
    }

    public ArtifactModule() {
        addSerializer(Artifact.class, new JsonSerializer<Artifact>() {
            @Override
            public void serialize(Artifact a, JsonGenerator gen, SerializerProvider serializers) throws IOException {

                var sb = new StringBuilder() //
                        .append(a.groupId).append(":")//
                        .append(a.artifactId).append(":")//
                        .append(a.version).append(":") //
                        .append(a.packaging).append(":") //
                        .append(a.releaseDate);

                if (!isNullOrCentral(a)) {
                    sb.append('@').append(a.repository);
                }

                var s = sb.toString();
                gen.writeString(s);
            }

        });

        addDeserializer(Artifact.class, new JsonDeserializer<Artifact>() {
            @Override
            public Artifact deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {

                var a = new Artifact();
                var json = p.getValueAsString();

                var parts = json.split("@");
                if (parts.length != 1 && parts.length != 2) {
                    throw new JsonParseException("Cannot parse repository: " + json);
                }

                if (parts.length == 2) {
                    a.repository = parts[1];
                } else {
                    a.repository = CENTRAL;
                }

                parts = parts[0].split(":");
                if (parts.length != 5) {
                    throw new JsonParseException("Cannot parse artifact: " + json);
                }

                a.groupId = parts[0];
                a.artifactId = parts[1];
                a.version = parts[2];
                a.packaging = parts[3];
                try {
                    a.releaseDate = Long.parseLong(parts[4]);
                } catch (NumberFormatException e) {
                    throw new JsonParseException("Cannot parse release date: " + json);
                }
                return a;
            }
        });
    }
}