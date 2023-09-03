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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Named;

public class RepositoryUtils {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryUtils.class);

    private final String indexUrl;
    private final String baseFolder;

    @Inject
    public RepositoryUtils(@Named("RepositoryUtils.indexUrl") String indexUrl, @Named("RepositoryUtils.baseFolder") String baseFolder) {
        this.indexUrl = indexUrl;
        this.baseFolder = baseFolder;
    }

    public boolean exists(int index) {
        LOG.info("Checking existence of index #{}", index);
        if (existsLocally(index)) {
            LOG.info("Index #{} exists locally", index);
            return true;
        }
        try {
            downloadRaw(index);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean existsLocally(int index) {
        return getTmpFileFor(index).exists();
    }

    public File download(int index) {
        LOG.debug("Downloading index #{}", index);
        try {
            return downloadRaw(index);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File downloadRaw(int index) throws IOException {
        var to = getTmpFileFor(index);
        if (to.exists()) {
            LOG.info("Index #{} exists locally", index);
            return to;
        }
        var from = getUrl(index);
        LOG.info("Downloading index #{} from {}", index, from);
        FileUtils.copyURLToFile(from, to);
        return to;
    }

    private File getTmpFileFor(int index) {
        var fileName = String.format("maven-crawler-%d.tmp", index);
        var to = new File(baseFolder, fileName);
        return to;
    }

    private URL getUrl(int index) throws MalformedURLException {
        return new URL(String.format(indexUrl, index));
    }
}