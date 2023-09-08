/*
 * Copyright 2021 Delft University of Technology
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.c0ps.commons.ResourceUtils;
import dev.c0ps.test.TestLoggerUtils;

public class IndexFileReaderTest {

    private IndexFileReader sut;

    @BeforeEach
    public void setup() {
        // TestLoggerUtils.clearLog();
        sut = new IndexFileReader();
    }

    @Test
    public void integrationTest() {
        var ids = sut.readIndexFile(ResourceUtils.getTestResource("some-index.gz"));
        assertEquals(329, ids.size());

        assertTrue(ids.contains(art("com.github.fcofdez:alcaudon_2.12:0.0.36:jar:1504512767157")));
        assertTrue(ids.contains(art("fi.testee:testeefi-cucumber:0.5.3:jar:1504499481992")));
        assertTrue(ids.contains(art("com.coreoz:plume-archetypes-parent:1.0.0:pom:1504514492021")));
        assertTrue(ids.contains(art("de.carne:java-default:4:jar:1504500653678")));
        // ... and others
    }

    @Test
    public void handleExcessColons() {
        var ids = sut.readIndexFile(ResourceUtils.getTestResource("index-717.gz"));

        var logs = TestLoggerUtils.getFormattedLogs(IndexFileReader.class);
        assertTrue(logs.contains("ERROR Skipping artifact with excess ':' in a field (com.srpago.connectionmanager:connectionmanager:1.0.9:alpha01:pom @ 1626133592475)"));
        assertTrue(logs.contains("ERROR Skipping artifact with excess ':' in a field (com.gitee.archermind-ti:contentmanager_ohos::1.0.0-beta:pom @ 1626159281628)"));

        assertEquals(17062, ids.size());

        assertTrue(ids.contains(art("org.jetbrains.kotlinx:kotlinx-coroutines-rx3:1.5.1:pom:1626081064219")));
        assertTrue(ids.contains(art("org.wso2.carbon.identity.framework:org.wso2.carbon.security.mgt.stub:5.20.107:jar:1626045904323")));
        assertTrue(ids.contains(art("me.shadaj:slinky-core_sjs0.6_2.13:0.6.7+12-65fb2097:jar:1626107574137")));
        assertTrue(ids.contains(art("io.kotest:kotest-assertions-core-watchosarm64:4.6.1:module:1626114350385")));
        // ... and others
    }

    private Artifact art(String s) {
        var parts = s.split(":");
        var id = new Artifact();
        id.releaseDate = Long.parseLong(parts[4]);
        id.groupId = parts[0];
        id.artifactId = parts[1];
        id.version = parts[2];
        id.packaging = parts[3];
        return id;
    }
}