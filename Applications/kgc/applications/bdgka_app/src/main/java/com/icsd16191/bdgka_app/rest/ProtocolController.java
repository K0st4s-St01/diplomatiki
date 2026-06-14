package com.icsd16191.bdgka_app.rest;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.icsd16191.bdgka_app.messages.Message;
import com.icsd16191.bdgka_app.messages.MessageProducer;
import com.icsd16191.bdgka_app.services.InMemoryParticipantStore;

import lombok.AllArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/protocol")
@AllArgsConstructor
public class ProtocolController{
    private MessageProducer messageProducer;
    private InMemoryParticipantStore participantStore;
    private ObjectMapper mapper;

  
    @PostMapping("/start")
    private Map<String,Object> start(){
        try{
            var participantsMsg = new Message();
            participantsMsg.setFrom("master");
            participantsMsg.setForWho("peers");
            participantsMsg.setOperation("PARTICIPANTS");
            participantsMsg.setPayload(mapper.writeValueAsString(participantStore.get()));

            var msg = new Message();
            msg.setFrom("master");
            msg.setForWho("peers");
            msg.setOperation("START");
            msg.setPayload("");
            
            messageProducer.sendMessage("blockchain-topic", msg);
            messageProducer.sendMessage("blockchain-topic", participantsMsg);
            return Map.of("result","protocol started","participants",participantStore.get());            
        }catch(Exception e){
            e.printStackTrace();
            return Map.of("result",e.getMessage());
        }
    }
}
