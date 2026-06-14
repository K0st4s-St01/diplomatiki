package com.icsd16191.bdgka_app_client.messages;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class MessageProducer{
  private final JmsTemplate jmsTemplate;
  private ObjectMapper mapper;


  public void sendMessage(String destination,Message message){
    jmsTemplate.convertAndSend(destination,mapper.writeValueAsString(message));
    System.out.println("from "+message.getFrom()+" "+message.getOperation()+" sent to "+message.getForWho());
  }
}
