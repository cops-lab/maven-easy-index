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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class ArtifactTest {

    @Test
    public void equality() {
        var a = a("g", "a", "v", "p", 1234, "r");
        var b = a("g", "a", "v", "p", 1234, "r");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void equality_diffGroup() {
        var a = a("g", "a", "v", "p", 1234, "r");
        var b = a("g2", "a", "v", "p", 1234, "r");
        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void equality_diffArtifact() {
        var a = a("g", "a", "v", "p", 1234, "r");
        var b = a("g", "a2", "v", "p", 1234, "r");
        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void equality_diffVersion() {
        var a = a("g", "a", "v", "p", 1234, "r");
        var b = a("g", "a", "v2", "p", 1234, "r");
        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void equality_diffPkg() {
        var a = a("g", "a", "v", "p", 1234, "r");
        var b = a("g", "a", "v", "p2", 1234, "r");
        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void equality_diffRelease() {
        var a = a("g", "a", "v", "p", 1234, "r");
        var b = a("g", "a", "v", "p", 2345, "r");
        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void equality_diffRepository() {
        var a = a("g", "a", "v", "p", 1234, "r");
        var b = a("g", "a", "v", "p", 2345, "r2");
        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void hasToString() {
        var actual = a("g", "a", "v", "p", 1234, "r").toString();
        var expected = "g:a:v:p:1234@r";
        assertEquals(expected, actual);
    }

    private static Artifact a(String g, String a, String v, String p, long rel, String rep) {
        var res = new Artifact();
        res.groupId = g;
        res.artifactId = a;
        res.version = v;
        res.packaging = p;
        res.releaseDate = rel;
        res.repository = rep;
        return res;
    }
}