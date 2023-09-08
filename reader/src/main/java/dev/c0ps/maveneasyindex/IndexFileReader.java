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

import static org.codehaus.plexus.PlexusConstants.SCANNING_INDEX;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.updater.IndexDataReader;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexFileReader {

    private static final Logger LOG = LoggerFactory.getLogger(IndexFileReader.class);

    public Set<Artifact> readIndexFile(File f) {

        try ( //
                var fis = new FileInputStream(f); //
                var bis = new BufferedInputStream(fis)) {

            var reader = new IndexDataReader(bis);
            var context = setupPlexusContext();
            var artifacts = new HashSet<Artifact>();

            reader.readIndex(new IndexDataReader.IndexDataReadVisitor() {
                @Override
                public void visitDocument(Document doc) {
                    if (isValidPackage(doc)) {
                        var artifact = toArtifact(doc);
                        if (artifact != null) {
                            artifacts.add(artifact);
                        }
                    }
                }
            }, context);

            return artifacts;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isValidPackage(Document doc) {
        for (var fieldName : Set.of("l", "del", "DESCRIPTOR", "allGroups", "rootGroups")) {
            var value = doc.getField(fieldName);
            if (value != null) {
                return false;
            }
        }
        return true;
    }

    private IndexingContext setupPlexusContext() {
        PlexusContainer plexusContainer;
        List<IndexCreator> indexers;
        try {
            var pc = new DefaultPlexusContainer();
            var config = new DefaultContainerConfiguration();
            config.setClassWorld(pc.getClassWorld());
            config.setClassPathScanning(SCANNING_INDEX);

            plexusContainer = new DefaultPlexusContainer(config);

            indexers = new ArrayList<IndexCreator>();
            for (Object component : plexusContainer.lookupList(IndexCreator.class)) {
                indexers.add((IndexCreator) component);
            }

        } catch (PlexusContainerException e) {
            throw new RuntimeException("Cannot construct PlexusContainer for MavenCrawler.", e);
        } catch (ComponentLookupException e) {
            throw new RuntimeException("Cannot add IndexCreators for MavenCrawler.", e);
        }

        var context = (IndexingContext) Proxy.newProxyInstance( //
                getClass().getClassLoader(), //
                new Class[] { IndexingContext.class }, //
                new MyInvocationHandler(indexers));
        return context;
    }

    public static Artifact toArtifact(Document doc) {

        // observations:
        // - qualifier is never set :/
        // - e always seems to point to existing file
        // - e is sometimes convoluted (e.g., pom.sha512)
        // - p can be null
        // - p can be "pom" while e is an actual package
        // - p can have weird values
        // - fields can be malformed (extra ':')
        // - parent poms seem to be p=pom, e=pom

        var m = str(doc.getField("m"));

        var id = new Artifact();
        id.groupId = str(doc.getField("g"));
        id.artifactId = str(doc.getField("a"));
        id.version = str(doc.getField("v"));
        id.packaging = str(doc.getField("e"));

        if (m == null || id.groupId == null || id.artifactId == null || id.version == null || id.packaging == null) {
            LOG.info("Skipping artifact with missing data ({}:{}:{}:{} @ {})", id.groupId, id.artifactId, id.version, id.packaging, m);
            return null;
        }

        // some package entries are convoluted
        id.packaging = shorten(id.packaging, ".asc", ".sha256", ".sha512");

        try {
            id.releaseDate = Long.parseLong(m);
        } catch (NumberFormatException e) {
            LOG.error("Skipping artifact with invalid release date ({}:{}:{}:{} @ {})", id.groupId, id.artifactId, id.version, id.packaging, m);
            return null;
        }

        // handle invalid data (e.g., index 717)
        if (hasColonAnywhere(id)) {
            LOG.error("Skipping artifact with excess ':' in a field ({}:{}:{}:{} @ {})", id.groupId, id.artifactId, id.version, id.packaging, id.releaseDate);
            return null;
        }

        return id;
    }

    private static String str(IndexableField f) {
        return f == null ? null : f.stringValue();
    }

    private static String shorten(String s, String... suffixes) {
        for (var suffix : suffixes) {
            if (s.endsWith(suffix)) {
                var t = s.substring(0, s.length() - suffix.length());
                if (!t.isEmpty()) {
                    return shorten(t, suffixes);
                }
            }
        }
        return s;
    }

    private static boolean hasColonAnywhere(Artifact a) {
        return a.groupId.contains(":") || a.artifactId.contains(":") || a.version.contains(":") || a.packaging.contains(":");
    }

    public static class MyInvocationHandler implements InvocationHandler {

        private List<IndexCreator> indexers;

        public MyInvocationHandler(List<IndexCreator> indexers) {
            this.indexers = indexers;
        }

        public List<IndexCreator> getIndexCreators() {
            return indexers;
        }

        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            try {
                final Method localMethod = getClass().getMethod(method.getName(), method.getParameterTypes());
                return localMethod.invoke(this, args);
            } catch (NoSuchMethodException e) {
                throw new UnsupportedOperationException("Method " + method.getName() + "() is not supported");
            } catch (IllegalAccessException e) {
                throw new UnsupportedOperationException("Method " + method.getName() + "() is not supported");
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }
}