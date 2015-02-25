/**
 * Copyright 2015 Netflix, Inc.
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
package com.netflix.spectator.tdigest;

import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.netflix.config.ConfigurationManager;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.iep.http.EurekaServerRegistry;
import com.netflix.iep.http.RxHttp;
import com.netflix.iep.http.ServerRegistry;
import com.netflix.spectator.api.ExtendedRegistry;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.Spectator;
import com.netflix.spectator.nflx.Plugin;
import org.apache.commons.configuration.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 *
 */
public class TDigestModule extends AbstractModule {

  private static final Logger LOGGER = LoggerFactory.getLogger(TDigestModule.class);

  private void loadProperties(String name) {
    try {
      ConfigurationManager.loadCascadedPropertiesFromResources(name);
    } catch (IOException e) {
      LOGGER.warn("failed to load properties for '" + name + "'");
    }
  }

  private AmazonKinesisClient newKinesisClient(AbstractConfiguration cfg) {
    String endpoint = cfg.getString("spectator.tdigest.kinesis.endpoint");
    AmazonKinesisClient client = new AmazonKinesisClient();
    client.setEndpoint(endpoint);
    return client;
  }

  @Override protected void configure() {
    bind(RxHttp.class).asEagerSingleton();
    bind(Plugin.class).asEagerSingleton();
    bind(ExtendedRegistry.class).toInstance(Spectator.registry());
    bind(Registry.class).toInstance(Spectator.registry());

    loadProperties("spectator-tdigest");
    AbstractConfiguration cfg = ConfigurationManager.getConfigInstance();
    String stream = cfg.getString("spectator.tdigest.kinesis.stream");
    TDigestRegistry registry = Spectator.registry().underlying(TDigestRegistry.class);
    if (stream != null && registry != null) {
      KinesisTDigestWriter writer = new KinesisTDigestWriter(newKinesisClient(cfg), stream);
      TDigestPlugin plugin = new TDigestPlugin(registry, writer);
      plugin.init();
      bind(TDigestPlugin.class).toInstance(plugin);
    }
  }

  /** Returns an instance of a server registry based on eureka. */
  @Provides
  public ServerRegistry getServerRegistry(DiscoveryClient client) {
    return new EurekaServerRegistry(client);
  }
}
