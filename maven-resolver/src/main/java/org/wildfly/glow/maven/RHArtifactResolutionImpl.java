/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2024 Red Hat, Inc., and individual contributors
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

import java.util.List;
import org.eclipse.aether.repository.RemoteRepository;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;
import org.wildfly.channel.Channel;
import org.wildfly.channel.ChannelManifestCoordinate;
import org.wildfly.channel.ChannelSession;
import org.wildfly.glow.ArtifactResolutionBuilder;
import org.wildfly.glow.ChannelMavenArtifactRepositoryManager;

/**
 *
 * @author jdenise
 */
class RHArtifactResolutionImpl implements ArtifactResolutionBuilder {

    private final MavenRepoManager defaultMavenRepoManager;
    private final List<RemoteRepository> remoteRepos;
    RHArtifactResolutionImpl() {
        remoteRepos = MavenResolver.getRHRemoteRepositories();
        defaultMavenRepoManager = MavenResolver.newRHMavenResolver();
    }
    @Override
    public Channel buildChannel(ChannelManifestCoordinate coordinates) throws Exception {
        return MavenResolver.buildChannel(coordinates, remoteRepos);
    }

    @Override
    public ChannelSession buildChannelSession(List<Channel> channels) throws Exception {
        return MavenResolver.buildChannelSession(channels, remoteRepos);
    }

    @Override
    public MavenRepoManager getMavenRepoManager() throws Exception {
        return defaultMavenRepoManager;
    }

    @Override
    public MavenRepoManager getChannelRepoManager(ChannelSession session) throws Exception {
        return new ChannelMavenArtifactRepositoryManager(session, getMavenRepoManager());
    }

}
