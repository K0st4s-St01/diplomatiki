package com.icsd16191.bdgka_app_client.rest;

import java.net.InetAddress;
import java.util.Map;

import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.icsd16191.bdgka_app_client.configuration.IdentityService;
import com.icsd16191.bdgka_app_client.messages.Message;
import com.icsd16191.bdgka_app_client.messages.MessageProducer;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/peer")
@AllArgsConstructor
public class BdgkaController {
  private MessageProducer messageProducer;
  private IdentityService.Identity id;

  @PostMapping("/join")
  public Map<String, Object> join() {
    try {
      Message message = new Message();
      message.setForWho("master");
      message.setOperation("JOIN");
      message.setPayload(id.getId());
      messageProducer.sendMessage("blockchain-topic", message);
      return Map.of("result", "ip " + id.getId() + " join message sent");
    } catch (Exception e) {
      return Map.of("result", e.getMessage());
    }
  }

}
