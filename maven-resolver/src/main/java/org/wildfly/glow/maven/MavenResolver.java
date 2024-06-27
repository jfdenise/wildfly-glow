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
package org.wildfly.glow.maven;

import java.nio.file.Path;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.jboss.galleon.maven.plugin.util.MavenArtifactRepositoryManager;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.wildfly.channel.Channel;
import org.wildfly.channel.ChannelManifestCoordinate;
import org.wildfly.channel.Repository;
import org.wildfly.glow.ChannelBuilder;

/**
 *
 * @author jdenise
 */
public final class MavenResolver {

    public static final String JBOSS_REPO_URL = "https://repository.jboss.org/nexus/content/groups/public/";
    public static final String CENTRAL_REPO_URL = "https://repo1.maven.org/maven2/";
    public static final String GA_REPO_URL = "https://maven.repository.redhat.com/ga/";
    public static final String SPRING_REPO_URL = "https://repo.spring.io/milestone";

    public static MavenRepoManager newMavenResolver() {
        RepositorySystem repoSystem = newRepositorySystem();
        MavenRepoManager resolver
                = new MavenArtifactRepositoryManager(repoSystem, newMavenSession(repoSystem), getRemoteRepositories());
        return resolver;
    }

    public static MavenRepoManager newMavenResolver(List<Channel> channels) throws Exception {
        RepositorySystem repoSystem = newRepositorySystem();
        MavenRepoManager resolver
                = new ChannelMavenArtifactRepositoryManager(channels, repoSystem, newMavenSession(repoSystem), getRemoteRepositories());
        return resolver;
    }
    public static MavenRepoManager newRHMavenResolver(List<Channel> channels) throws Exception {
        RepositorySystem repoSystem = newRepositorySystem();
        MavenRepoManager resolver
                = new ChannelMavenArtifactRepositoryManager(channels, repoSystem, newMavenSession(repoSystem), getRHRemoteRepositories());
        return resolver;
    }
    public static MavenRepoManager newRHMavenResolver() {
        RepositorySystem repoSystem = newRepositorySystem();
        MavenArtifactRepositoryManager resolver
                = new MavenArtifactRepositoryManager(repoSystem, newMavenSession(repoSystem), getRHRemoteRepositories());
        return resolver;
    }

    public static ChannelBuilder newChannelBuilder() throws Exception {
        RepositorySystem system = newRepositorySystem();
        return new ChannelBuilderImpl(system, newMavenSession(system), getRemoteRepositories());
    }

    public static ChannelBuilder newRHChannelBuilder() throws Exception {
        RepositorySystem system = newRepositorySystem();
        return new ChannelBuilderImpl(system, newMavenSession(system), getRHRemoteRepositories());
    }

    public static List<RemoteRepository> getRemoteRepositories() {
        List<RemoteRepository> repos = new ArrayList<>();
        RemoteRepository.Builder central = new RemoteRepository.Builder("central", "default", CENTRAL_REPO_URL);
        central.setSnapshotPolicy(new RepositoryPolicy(false, RepositoryPolicy.UPDATE_POLICY_NEVER,
                RepositoryPolicy.CHECKSUM_POLICY_IGNORE));
        repos.add(central.build());
        RemoteRepository.Builder ga = new RemoteRepository.Builder("redhat-ga", "default", GA_REPO_URL);
        ga.setSnapshotPolicy(new RepositoryPolicy(false, RepositoryPolicy.UPDATE_POLICY_NEVER,
                RepositoryPolicy.CHECKSUM_POLICY_IGNORE));
        repos.add(ga.build());
        RemoteRepository.Builder nexus = new RemoteRepository.Builder("jboss-nexus", "default", JBOSS_REPO_URL);
        nexus.setSnapshotPolicy(new RepositoryPolicy(false, RepositoryPolicy.UPDATE_POLICY_NEVER,
                RepositoryPolicy.CHECKSUM_POLICY_IGNORE));
        repos.add(nexus.build());
        RemoteRepository.Builder spring = new RemoteRepository.Builder("spring-repo", "default", SPRING_REPO_URL);
        spring.setSnapshotPolicy(new RepositoryPolicy(false, RepositoryPolicy.UPDATE_POLICY_NEVER,
                RepositoryPolicy.CHECKSUM_POLICY_IGNORE));
        repos.add(spring.build());
        return repos;
    }

    public static List<RemoteRepository> getRHRemoteRepositories() {
        List<RemoteRepository> repos = new ArrayList<>();
        RemoteRepository.Builder ga = new RemoteRepository.Builder("redhat-ga", "default", GA_REPO_URL);
        ga.setSnapshotPolicy(new RepositoryPolicy(false, RepositoryPolicy.UPDATE_POLICY_NEVER,
                RepositoryPolicy.CHECKSUM_POLICY_IGNORE));
        ga.setReleasePolicy(new RepositoryPolicy(true, RepositoryPolicy.UPDATE_POLICY_NEVER,
                RepositoryPolicy.CHECKSUM_POLICY_IGNORE));
        repos.add(ga.build());
        return repos;
    }

    public static RepositorySystemSession newMavenSession(RepositorySystem repoSystem) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        String localPath = System.getProperty("maven.repo.local");
        Path localCache = localPath == null ? Paths.get(System.getProperty("user.home"), ".m2", "repository") : Paths.get(localPath);
        LocalRepository localRepo = new LocalRepository(localCache.toFile());
        session.setLocalRepositoryManager(repoSystem.newLocalRepositoryManager(session, localRepo));
        return session;
    }

    public static RepositorySystem newRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        return locator.getService(RepositorySystem.class);
    }

    public static Channel buildChannel(ChannelManifestCoordinate manifest, List<RemoteRepository> remoteRepos) throws Exception {
        List<Repository> repos = new ArrayList<>();
        for (RemoteRepository r : remoteRepos) {
            repos.add(new Repository(r.getId(), r.getUrl()));
        }
        return new Channel(null, null, null, repos, manifest, null, null);
    }
}
