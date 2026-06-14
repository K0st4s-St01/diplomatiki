package com.icsd16191.bdgka_app.configuration;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ArtemisConfig {
	@Bean(initMethod = "start", destroyMethod = "stop")
	public EmbeddedActiveMQ embeddedActiveMQ() throws Exception{
			var config = new ConfigurationImpl();
			config.setJournalDirectory("./target/artemis/journal");
			config.setBindingsDirectory("./target/artemis/bindings");
			config.setLargeMessagesDirectory("./target/artemis/large-messages");
			config.setPagingDirectory("./target/artemis/paging");

			Set<TransportConfiguration> acceptors = new HashSet<>();
			TransportConfiguration nettyAcceptor = new TransportConfiguration(
				NettyAcceptorFactory.class.getName(),
				Map.of("host","localhost","port",61616)
			);
			acceptors.add(nettyAcceptor);
			config.setAcceptorConfigurations(acceptors);
			config.setSecurityEnabled(false);

			EmbeddedActiveMQ broker = new EmbeddedActiveMQ();
			broker.setConfiguration(config);
			return broker;
	}
}
