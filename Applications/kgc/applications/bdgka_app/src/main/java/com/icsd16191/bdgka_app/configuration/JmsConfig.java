package com.icsd16191.bdgka_app.configuration;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

import jakarta.jms.ConnectionFactory;

@Configuration
public class JmsConfig{
  @Bean
  public ConnectionFactory connectionFactory(){
    var connectionFactory =new ActiveMQConnectionFactory("tcp://localhost:61616");
    // connectionFactory.setClientID("master");
    return connectionFactory;
  }
  @Bean
  public JmsTemplate jsJmsTemplate(ConnectionFactory connectionFactory){
    JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
    jmsTemplate.setPubSubDomain(true);
    jmsTemplate.setDefaultDestinationName("blockchain-topic");
    return jmsTemplate;
  }
  @Bean
  public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
    ConnectionFactory connectionFactory
  ){
      DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
      // factory.setSubscriptionDurable(true);
      factory.setConnectionFactory(connectionFactory);
      factory.setPubSubDomain(true);
      factory.setSessionTransacted(true);
      factory.setConcurrency("1-1");
      factory.setSessionAcknowledgeMode(1);
      return factory;
  }
}
