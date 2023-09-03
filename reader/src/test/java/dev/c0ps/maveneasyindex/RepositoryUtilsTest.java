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

import static dev.c0ps.test.TestLoggerUtils.clearLog;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.c0ps.test.HttpTestServer;

public class RepositoryUtilsTest {

    private static final String FILE_FORMAT = "index-%d.gz";
    private static final String INDEX_URL = "http://127.0.0.1:1234/" + FILE_FORMAT;
    private static final String SOME_CONTENT = "abcd";

    private static HttpTestServer httpd;

    @TempDir
    private File dirTmp;
    private RepositoryUtils sut;

    @BeforeAll
    public static void setupAll() throws IOException {
        httpd = new HttpTestServer(1234);
        httpd.start();
    }

    @AfterAll
    public static void teardownAll() {
        httpd.stop();
    }

    @BeforeEach
    public void setup() {
        httpd.reset();
        clearLog();
        sut = new RepositoryUtils(INDEX_URL, dirTmp.getAbsolutePath());
    }

    @Test
    public void fileDoesNotExist() {
        httpd.setResponse(404, "text/plain", "...");
        assertFalse(sut.exists(1));
    }

    @Test
    public void fileDoesExist() {
        httpd.setResponse(200, "text/plain", "...");
        assertTrue(sut.exists(1));
    }

    @Test
    public void repeatedExistsOnlyOneRequest() {
        assertTrue(sut.exists(1));
        assertEquals(1, httpd.requests.size());
        httpd.reset();
        assertTrue(sut.exists(1));
        assertEquals(0, httpd.requests.size());
    }

    @Test
    public void repeatedDownloadsOnlyOneRequest() {
        assertTrue(sut.download(1).exists());
        assertEquals(1, httpd.requests.size());
        httpd.reset();
        assertTrue(sut.download(1).exists());
        assertEquals(0, httpd.requests.size());
    }

    @Test
    public void downloadingNonExistingFails() {
        httpd.setResponse(404, "text/plain", "...");
        assertThrows(RuntimeException.class, () -> {
            sut.download(1);
        });
    }

    @Test
    public void downloadingExistingWorks() throws IOException {
        httpd.setResponse(200, "text/plain", SOME_CONTENT);
        var actual = sut.download(1);
        assertTrue(actual.exists());
        assertTrue(actual.getAbsolutePath().startsWith(dirTmp.getAbsolutePath()));
        assertEquals("maven-crawler-1.tmp", actual.getName());
        assertEquals(SOME_CONTENT, readFileToString(actual, UTF_8.name()));
    }
}