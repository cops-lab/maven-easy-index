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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.Test;

public class ArtifactTest {

    @Test
    public void defaultValues() {
        var a = new Artifact();
        assertNull(a.groupId);
        assertNull(a.artifactId);
        assertNull(a.version);
        assertNull(a.packaging);
        assertEquals(0, a.releaseDate);
        assertNull(a.repository);
    }

    @Test
    public void customInit() {
        var a = new Artifact("g", "a", "1.2.3");
        assertEquals("g", a.groupId);
        assertEquals("a", a.artifactId);
        assertEquals("1.2.3", a.version);
        assertNull(a.packaging);
        assertEquals(0, a.releaseDate);
        assertNull(a.repository);
    }

    @Test
    public void customInit2() {
        var a = new Artifact("g", "a", "1.2.3", "jar");
        assertEquals("g", a.groupId);
        assertEquals("a", a.artifactId);
        assertEquals("1.2.3", a.version);
        assertEquals("jar", a.packaging);
        assertEquals(0, a.releaseDate);
        assertNull(a.repository);
    }

    @Test
    public void builderSetReleaseDateLong() {
        var a = new Artifact();
        var actual = a.setReleaseDate(123456);
        var expected = new Artifact();
        expected.releaseDate = 123456;
        assertEquals(expected, actual);
        assertSame(a, actual);
    }

    @Test
    public void builderSetReleaseDateDate() {
        var a = new Artifact();
        var actual = a.setReleaseDate(new Date(1234567));
        var expected = new Artifact();
        expected.releaseDate = 1234567;
        assertEquals(expected, actual);
        assertSame(a, actual);
    }

    @Test
    public void builderSetRepository() {
        var a = new Artifact();
        var actual = a.setRepository("http://x");
        var expected = new Artifact();
        expected.repository = "http://x";
        assertEquals(expected, actual);
        assertSame(a, actual);
    }

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
    public void toStringImplementation() {
        var actual = a("g", "a", "v", "p", 1234, "r").toString();
        var expected = "g:a:v:p:1234@r";
        assertEquals(expected, actual);
    }

    @Test
    public void hasCentralConstant() {
        assertEquals("https://repo.maven.apache.org/maven2/", Artifact.CENTRAL);
    }

    @Test
    public void toStringShortensCentral() {
        var actual = a("g", "a", "v", "p", 1234, Artifact.CENTRAL).toString();
        var expected = "g:a:v:p:1234@CENTRAL";
        assertEquals(expected, actual);
    }

    @Test
    public void toStringWorkWithNullRepo() {
        var actual = a("g", "a", "v", "p", 1234, null).toString();
        var expected = "g:a:v:p:1234@null";
        assertEquals(expected, actual);
    }

    @Test
    public void isCloneable() {
        var a = a("g", "a", "v", "p", 1234, "r");
        var b = a.clone();
        assertEquals(a, b);
        assertNotSame(a, b);
    }

    @Test
    public void hasReleaseDate() {
        assertFalse(new Artifact().hasReleaseDate());
        assertFalse(new Artifact().setReleaseDate(0).hasReleaseDate());
        assertFalse(new Artifact().setReleaseDate(-1).hasReleaseDate());
        assertTrue(new Artifact().setReleaseDate(1).hasReleaseDate());
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