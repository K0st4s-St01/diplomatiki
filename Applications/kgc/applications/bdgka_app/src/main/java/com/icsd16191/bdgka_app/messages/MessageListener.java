package com.icsd16191.bdgka_app.messages;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.icsd16191.bdgka_app.services.InMemoryParticipantStore;

import lombok.AllArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class MessageListener{
  private InMemoryParticipantStore ipStore;
  private ObjectMapper mapper;
  
  @JmsListener(destination = "blockchain-topic" , containerFactory = "jmsListenerContainerFactory" )
  public void receiveMessage(String message){
    Message msgObj = mapper.readValue(message,Message.class);
    System.out.println(msgObj);
    if(msgObj.getForWho().equals("master")){
      switch(msgObj.getOperation()){
        case "JOIN":
          ipStore.add(msgObj.getPayload());
        break;
      }
    }
  }
}
