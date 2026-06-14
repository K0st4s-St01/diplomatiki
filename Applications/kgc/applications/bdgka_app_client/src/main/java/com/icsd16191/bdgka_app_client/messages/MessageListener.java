package com.icsd16191.bdgka_app_client.messages;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.icsd16191.bdgka_app_client.configuration.IdentityService;
import com.icsd16191.bdgka_app_client.crypto.CryptoService;
import com.icsd16191.bdgka_app_client.entities.Block;
import com.icsd16191.bdgka_app_client.services.InMemoryKeyStoreService;
import com.icsd16191.bdgka_app_client.services.ProtocolService;
import com.icsd16191.bdgka_app_client.web_client.BdgkaClient;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class MessageListener {
  private final ObjectMapper mapper;
  private final ProtocolService protocolService;
  private final InMemoryKeyStoreService keyStoreService;
  private final IdentityService.Identity identity;
  private final CryptoService cryptoService;
  private final MessageProducer messageProducer;
  private final BdgkaClient blockchainClient;
  private int reicevedMessages = 0;

  @JmsListener(destination = "blockchain-topic", containerFactory = "durableTopicListenerFactory")
  public void receiveMessage(String message) {
    var msg = mapper.readValue(message, Message.class);
    this.reicevedMessages++;
    try {
      switch (msg.getOperation()) {
        case "START":
          System.out.println(identity.getId() + "started at " + new Date() + " -> " + System.currentTimeMillis());
          try {
            protocolService.generateAndBroadcastPublicKey();
          } catch (Exception e) {
            e.printStackTrace();
          }
          break;
        case "PARTICIPANTS":
          var participants = mapper.readValue(msg.getPayload(), new TypeReference<Set<String>>() {
          }).stream().toList();
          protocolService.resolveParticipantsAndSaveNextPrevious(participants);
          System.out.println("ORDER\n\t" +
              keyStoreService.getPrevious() + "->(current)" +
              identity.getId() + "->" +
              keyStoreService.getNext());

          break;
        case "INITIAL_PK_BROADCAST":
          String next = keyStoreService.getNext();
          String previous = keyStoreService.getPrevious();
          if (!next.equals("<None>") && next.equals(msg.getFrom())) {
            keyStoreService.getPublicKeys().put(
                next,
                cryptoService.elementFromBase64(msg.getPayload()));
            if (previous.equals("<None>")) {
              try {
                if (keyStoreService.getMi_public() == null) {
                  cryptoService.generateMiKeys();
                }
                var ms = new ArrayList<String>();
                System.out.println(identity.getId() + "started creating block  " + new Date() + " -> " + System.currentTimeMillis());
                var block = cryptoService.createBlock("U-Block-" + UUID.randomUUID().toString(), ms);
                System.out.println(identity.getId() + "created block " + new Date() + " -> " + System.currentTimeMillis());

                System.out.println("produced " + block.getId());

                var msgParams = cryptoService.produceMessageParametersFromBlock(block);
                var result = blockchainClient.append(block);
                System.out.println("blockchain network " + result.block());

                var messageToSend = new Message();
                messageToSend.setFrom(identity.getId());
                messageToSend.setForWho(keyStoreService.getNext());
                messageToSend.setOperation("GK-COMPUTATION");
                messageToSend.setPayload(mapper.writeValueAsString(msgParams));
                messageProducer.sendMessage("blockchain-topic", messageToSend);

              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }

          if (!previous.equals("<None>") && previous.equals(msg.getFrom())) {
            keyStoreService.getPublicKeys().put(
                previous,
                cryptoService.elementFromBase64(msg.getPayload()));
          }
          break;
        case "GK-COMPUTATION":
          if (msg.getForWho().equals(identity.getId())) {
            CryptographicMessageParameters params = mapper.readValue(msg.getPayload(),
                CryptographicMessageParameters.class);
            var lastBlockResponse = blockchainClient.current().block();
            if (lastBlockResponse.get("result").equals("OK") && params.getIp().equals(identity.getId())) {

              if (keyStoreService.getMi_public() == null) {
                System.out.println("generating Mi keys");
                cryptoService.generateMiKeys();
              }

              Block currentBlock = mapper.readValue(mapper.writeValueAsString(lastBlockResponse.get("data")),
                  Block.class);
              System.out.println("read " + currentBlock.getId());

              if (cryptoService.verifyUser(currentBlock, params)) {

                var ms = currentBlock.getMs();
                if (!keyStoreService.getNext().equals("<None>")) {
                  var block = cryptoService.createBlock("CONTRIBUTE-Block-" + UUID.randomUUID().toString(), ms);
                  var result = blockchainClient.append(block).block();
                  System.out.println("blockchain-network " + result + "\n" + block.getId() + " produced");

                  var msgParams = cryptoService.produceMessageParametersFromBlock(block);
                  var messageToSend = new Message();
                  messageToSend.setFrom(identity.getId());
                  messageToSend.setForWho(keyStoreService.getNext());
                  messageToSend.setOperation("GK-COMPUTATION");
                  messageToSend.setPayload(mapper.writeValueAsString(msgParams));
                  messageProducer.sendMessage("blockchain-topic", messageToSend);
                } else {
                  var block = cryptoService.createBlock("CONTRIBUTE-Last-Block-" + UUID.randomUUID().toString(), ms);

                  var fs = cryptoService.computeFsListAndGkForUn(block.getMs());
                  block.setHeader(cryptoService.h1Gk());
                  var result = blockchainClient.append(block).block();
                  System.out.println("blockchain-network final " + result);
                  var messageToSend = new Message();

                  messageToSend.setFrom(identity.getId());
                  messageToSend.setForWho(keyStoreService.getPrevious());
                  messageToSend.setOperation("GK-FINAL");
                  messageToSend.setPayload(mapper.writeValueAsString(Map.of(
                      "fs", fs, "index", 1)));

                  messageProducer.sendMessage("blockchain-topic", messageToSend);
                }
              } else {
                System.out.println("user " + identity.getId() + " not verifying previous");
              }
            } else {
              System.out.println("reading block from blockchain error");
            }
          }

          break;
        case "GK-FINAL":
          if (msg.getForWho().equals(identity.getId())) {
            System.out.println("GK-FINAL " + identity.getId());
            Map<String, Object> fsAndIndex = mapper.readValue(msg.getPayload(),
                new TypeReference<Map<String, Object>>() {
                });
            List<String> fs;
            try {
              fs = (List<String>) fsAndIndex.get("fs");

              var index = (Integer) fsAndIndex.get("index");
              cryptoService.computeGkForUi(fs, index);

              var lastBlockResponse = blockchainClient.current().block();
              if (lastBlockResponse.get("result").equals("OK")) {
                Block currentBlock = mapper.readValue(mapper.writeValueAsString(lastBlockResponse.get("data")),
                    Block.class);
                System.out.println("read " + currentBlock.getId());
                if (cryptoService.h1VerifyGk(currentBlock.getHeader())) {
                  System.out.println("group key is ok");
                } else {
                  System.out.println("group key not verified");
                }
              }

              var messageToSend = new Message();
              if (!keyStoreService.getPrevious().equals("<None>")) {
                messageToSend.setFrom(identity.getId());
                messageToSend.setForWho(keyStoreService.getPrevious());
                messageToSend.setOperation("GK-FINAL");
                messageToSend.setPayload(mapper.writeValueAsString(Map.of(
                    "fs", fs, "index", index + 1)));
                messageProducer.sendMessage("blockchain-topic", messageToSend);
              }
            } catch (Exception e) {
              e.printStackTrace();
            }

            System.out.println(identity.getId() + "gk-final at " + new Date() + " -> " + System.currentTimeMillis());
            System.out.println("received " + this.reicevedMessages);
          }
          break;
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

}
