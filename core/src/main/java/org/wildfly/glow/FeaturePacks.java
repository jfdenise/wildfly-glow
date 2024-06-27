/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2023 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
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
package org.wildfly.glow;

import java.io.File;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author jdenise
 */
public class FeaturePacks {

    private static final String VERSIONS = "versions.yaml";
    private static final String PROVISIONING_FILE_RADICAL = "/provisioning-";
    private static final String MANIFEST_FILE_RADICAL = "/manifest-";
    private static final String TECH_PREVIEW = "/tech-preview/";

    public static final String URL_PROPERTY = "wildfly-glow-galleon-feature-packs-url";

    public static Path getFeaturePacks(String version, String context, boolean techPreview) throws Exception {
        try {
            String rootURL = getFeaturePacksURL();
            Yaml yaml = new Yaml();
            if (version == null) {
                Map<String, String> map = yaml.load(new URI(rootURL + VERSIONS).toURL().openStream());
                version = map.get("latest");
            }
            Path p = Files.createTempFile("glow-provisioning-", context);
            try (InputStream in = new URL(rootURL + version + (techPreview ? TECH_PREVIEW : "") + PROVISIONING_FILE_RADICAL + context + ".xml").openStream()) {
                Files.copy(in, p,
                        StandardCopyOption.REPLACE_EXISTING);
            }
            p.toFile().deleteOnExit();
            return p;
        } catch (Exception ex) {
            throw new Exception("Exception occured while retrieving known Galleon feature-packs for version " + version + ". Cause: " + ex.getLocalizedMessage());
        }
    }

    public static String getFeaturePacksURL() throws Exception {
        String rootURL = Utils.getConfigEntry(URL_PROPERTY);
        if (rootURL == null) {
            throw new Exception("No " + URL_PROPERTY + " entry found");
        }
        if (!rootURL.endsWith("/")) {
            rootURL = rootURL + "/";
        }
        return rootURL;
    }

    public static Set<String> getAllVersions() throws Exception {
        String rootURL = getFeaturePacksURL();
        Set<String> set = new TreeSet<>();
        Yaml yaml = new Yaml();
        Map<String, String> map = yaml.load(new URI(rootURL + VERSIONS).toURL().openStream());
        set.addAll(Arrays.asList(map.get("versions").split(",")));
        return set;
    }

    public static String getLatestVersion() throws Exception {
        String rootURL = getFeaturePacksURL();
        Yaml yaml = new Yaml();
        Map<String, String> map = yaml.load(new URI(rootURL + VERSIONS).toURL().openStream());
        return map.get("latest");
    }

    public static URL getManifest(String version, String context, boolean techPreview) throws Exception {
        String rootURL = getFeaturePacksURL();
        Yaml yaml = new Yaml();
        if (version == null) {
            Map<String, String> map = yaml.load(new URI(rootURL + VERSIONS).toURL().openStream());
            version = map.get("latest");
        }
        URL url = new URL(rootURL + version + (techPreview ? TECH_PREVIEW : "") + MANIFEST_FILE_RADICAL + context + ".yaml");
        boolean exists = false;
        if ("file".equals(url.getProtocol())) {
            File f = new File(url.toURI());
            exists = Files.exists(f.toPath());
        } else {
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            int responseCode = huc.getResponseCode();
            exists = HttpURLConnection.HTTP_OK == responseCode;
        }
        if (exists) {
            return url;
        } else {
            return null;
        }
    }
}
