package com.icsd16191.bdgka_app_client.services;

import java.util.List;

import org.springframework.stereotype.Component;

import com.icsd16191.bdgka_app_client.configuration.IdentityService;
import com.icsd16191.bdgka_app_client.crypto.CryptoService;
import com.icsd16191.bdgka_app_client.messages.Message;
import com.icsd16191.bdgka_app_client.messages.MessageProducer;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ProtocolService {
  private InMemoryKeyStoreService keyStoreService;
  private MessageProducer messageProducer;
  private CryptoService cryptoService;
  private IdentityService.Identity identity;


  public void generateAndBroadcastPublicKey() throws Exception {
    var msg = new Message();
    msg.setFrom(identity.getId());
    msg.setForWho("peers");
    msg.setOperation("INITIAL_PK_BROADCAST");
    if(keyStoreService.getPublicKey() == null){
      cryptoService.generateSignatureKeys();
    }
    msg.setPayload(cryptoService.base64Encode(keyStoreService.getPublicKey().toBytes()));
    messageProducer.sendMessage("blockchain-topic", msg);
  }
  public void resolveParticipantsAndSaveNextPrevious(List<String> participants){
        for (int i = 0; i < participants.size(); i++) {
          if (participants.get(i).equals(identity.getId())) {
            if (i == 0) {
              identity.setFirst(true);
              keyStoreService.setNext(participants.get(i + 1));
              keyStoreService.setPrevious("<None>");
            } else if (i == participants.size() - 1) {
              keyStoreService.setPrevious(participants.get(i - 1));
              keyStoreService.setNext("<None>");
            } else {
              keyStoreService.setPrevious(participants.get(i - 1));
              keyStoreService.setNext(participants.get(i + 1));
            }
          }
        }
    }

}
