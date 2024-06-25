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
package org.wildfly.glow;

import java.util.List;
import org.jboss.galleon.universe.maven.repo.MavenRepoManager;
import org.wildfly.channel.Channel;
import org.wildfly.channel.ChannelSession;

/**
 *
 * @author jdenise
 */
public class ArtifactResolution {
    private final List<Channel> channels;
    private final ChannelSession channelSession;
    private final MavenRepoManager defaultRepoManager;
    private final MavenRepoManager repoManager;
    ArtifactResolution(MavenRepoManager repoManager, List<Channel> channels, ChannelSession channelSession, MavenRepoManager defaultRepoManager) {
        this.channels = channels;
        this.channelSession = channelSession;
        this.defaultRepoManager = defaultRepoManager;
        this.repoManager = repoManager;
    }

    /**
     * @return the channels
     */
    public List<Channel> getChannels() {
        return channels;
    }

    /**
     * @return the session
     */
    public ChannelSession getChannelSession() {
        return channelSession;
    }

    /**
     * @return the defaultMavenRepo
     */
    public MavenRepoManager getDirectMavenRepoManager() {
        return defaultRepoManager;
    }

    public MavenRepoManager getRepoManager() {
        return repoManager;
    }
}
