package com.icsd16191.bdgka_app_client.configuration;

import java.net.UnknownHostException;
// import java.util.UUID;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
// import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

import jakarta.jms.ConnectionFactory;
import lombok.AllArgsConstructor;

@Configuration
@EnableJms
@AllArgsConstructor
public class MessagingConfiguration {
  // private ServerProperties context;
  @Bean
  ConnectionFactory connectionFactory() throws UnknownHostException {
    var connection = new ActiveMQConnectionFactory("tcp://localhost:61616");
    // connection.setClientID("client:"+context.getPort()+":"+UUID.randomUUID());
    return connection;
  }

  @Bean
  public DefaultJmsListenerContainerFactory durableTopicListenerFactory(ConnectionFactory connectionFactory) {
    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    factory.setPubSubDomain(true);
    // factory.setSubscriptionDurable(true);
    factory.setConnectionFactory(connectionFactory);
    return factory;
  }

  @Bean
  public JmsTemplate template(ConnectionFactory connectionFactory) {
    var template = new JmsTemplate(connectionFactory);
    template.setPubSubDomain(true);
    return template;
  }
}
