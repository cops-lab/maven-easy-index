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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ArtifactModuleTest {

    private ObjectMapper om;

    @BeforeEach
    public void setup() {
        om = new ObjectMapper().registerModule(new ArtifactModule());
    }

    private static Artifact someArtifact() {
        var a = new Artifact();
        a.groupId = "g";
        a.artifactId = "a";
        a.version = "v";
        a.packaging = "p";
        a.releaseDate = 1234;
        return a;
    }

    @Test
    public void allFieldsAreContained() throws JsonProcessingException {
        var a = someArtifact();
        var json = om.writeValueAsString(a);

        var expected = "\"g:a:v:p:1234\"";
        assertEquals(json, expected);
    }

    @Test
    public void roundtrip() throws JsonProcessingException {
        var a = someArtifact();

        var json = om.writeValueAsString(a);
        var b = om.readValue(json, Artifact.class);

        assertNotSame(a, b);
        assertEquals(a, b);
    }

    @Test
    public void invalidFormat() throws JsonProcessingException {
        var json = "\"not:enough:parts\"";
        var e = assertThrows(JsonParseException.class, () -> {
            om.readValue(json, Artifact.class);
        });
        assertEquals("Cannot parse artifact: not:enough:parts", e.getMessage());
    }

    @Test
    public void invalidReleaseDate() throws JsonProcessingException {
        var json = "\"g:a:v:p:NOT_A_NUMBER\"";
        var e = assertThrows(JsonParseException.class, () -> {
            om.readValue(json, Artifact.class);
        });
        assertEquals("Cannot parse release date: g:a:v:p:NOT_A_NUMBER", e.getMessage());
    }
}