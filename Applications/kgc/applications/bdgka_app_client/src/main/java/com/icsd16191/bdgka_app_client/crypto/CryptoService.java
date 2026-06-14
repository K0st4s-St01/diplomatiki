package com.icsd16191.bdgka_app_client.crypto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.stereotype.Service;

import com.icsd16191.bdgka_app_client.configuration.IdentityService.Identity;
import com.icsd16191.bdgka_app_client.entities.Block;
import com.icsd16191.bdgka_app_client.messages.CryptographicMessageParameters;
import com.icsd16191.bdgka_app_client.services.InMemoryKeyStoreService;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class CryptoService {

  private TypeACurveGenerator gen = new TypeACurveGenerator(256, 1024);// group security, size of base field
  private final InMemoryKeyStoreService keyStoreService;
  private Pairing pairing;
  private Element genitorElement;
  private final Identity id;
  {
    try {
      if (!Files.exists(Path.of("src/main/resources/parameters.properties"))) {
        System.out.println("generating parameters");
        generateECParameters();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    pairing = PairingFactory.getPairing("src/main/resources/parameters.properties");
    if (!Files.exists(Path.of("src/main/resources/genitor.element"))) {
      genitorElement = pairing.getG1().newRandomElement().getImmutable();
      try {
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("src/main/resources/genitor.element")));
        var genitorStr = Base64.getEncoder().encodeToString(genitorElement.toBytes());
        writer.write(genitorStr);
        writer.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/genitor.element"))) {
        var genitorStr = reader.readLine().toString();
        genitorElement = pairing.getG1().newElementFromBytes(Base64.getDecoder().decode(genitorStr)).getImmutable();
        reader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void generateECParameters() throws Exception {
    BufferedWriter bw = new BufferedWriter(new FileWriter(new File("src/main/resources/parameters.properties")));
    var params = gen.generate();
    bw.write(params.toString());
    bw.flush();
    bw.close();
  }

  public void generateMiKeys() {
    var mi = pairing.getZr().newRandomElement().getImmutable();
    var Mi = this.genitorElement.duplicate().powZn(mi).getImmutable();
    keyStoreService.setMi_public(Mi);
    keyStoreService.setMi_private(mi);
  }

  public void generateSignatureKeys() {
    var mi = pairing.getZr().newRandomElement().getImmutable();
    var Mi = this.genitorElement.duplicate().powZn(mi).getImmutable();
    keyStoreService.setPublicKey(Mi);
    keyStoreService.setPrivateKey(mi);
  }

  public String base64Encode(byte[] value) {
    var encoder = Base64.getEncoder();
    return encoder.encodeToString(value);
  }

  public byte[] base64Decode(String base64) {
    var decoder = Base64.getDecoder();
    return decoder.decode(base64);
  }

  public Element elementFromHash(byte[] hash) {
    return pairing.getG1().newElementFromHash(hash, 0, hash.length).getImmutable();

  }

  public Element elementFromBytes(byte[] bytes) {
    return pairing.getG1().newElementFromBytes(bytes);
  }

  public Element elementFromBase64(String base64) {
    var bytes = base64Decode(base64);
    var el = pairing.getG1().newElementFromBytes(bytes).getImmutable();
    return el;
  }

  public String blsSignature(byte[] content) {
    var h = elementFromHash(content);
    var sig = h.duplicate().powZn(keyStoreService.getPrivateKey());
    return base64Encode(sig.toBytes());
  }

  // this one gets used
  public Element h2(Element h0, Element h1) {
    var sig = h0.duplicate().powZn(keyStoreService.getPrivateKey()).add(
        h1.duplicate().powZn(keyStoreService.getPrivateKey())).getImmutable();
    return sig;

  }

  public Boolean blsVerify(byte[] content, byte[] signature, Element publicKey) {
    var h = elementFromBytes(content);
    var sig = pairing.getG1().newElementFromBytes(signature);

    Element temp1 = pairing.pairing(sig, genitorElement);
    Element temp2 = pairing.pairing(h, publicKey);

    return temp1.isEqual(temp2);
  }

  public Boolean h2Verify(Element el1, Element el2, Element sig, Element publicKey) {
    Element left = pairing.pairing(sig, this.genitorElement.duplicate()).getImmutable();
    // System.out.println("left-> " + left);
    Element right0 = pairing.pairing(el1, publicKey).getImmutable();
    Element right1 = pairing.pairing(el2, publicKey).getImmutable();

    // System.out.println("right-> " + right0.duplicate().mul(right1));
    return left.isEqual(right0.duplicate().mul(right1));
  }

  public Block createBlock(String id, List<String> ms) {
    var block = new Block();
    ms.add(base64Encode(keyStoreService.getMi_public().toBytes()));
    block.setMs(ms);
    var bytes = new ByteArrayOutputStream();

    try {
      bytes.write(keyStoreService.getPublicKey().toBytes());
      bytes.write(keyStoreService.getMi_public().toBytes());
      // bytes.write(keyStoreService.getPrivateKey().toBytes());
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    byte[] hi2data = bytes.toByteArray();
    var hi2 = elementFromHash(hi2data);
    block.setHi2(base64Encode(hi2.toBytes()));

    var signature = h2(hi2, keyStoreService.getMi_public());
    block.setSignature(base64Encode(signature.toBytes()));

    block.setId(id);
    block.setTimestamp(System.currentTimeMillis());
    block.setNextIp(keyStoreService.getNext());
    block.setPk(base64Encode(keyStoreService.getPublicKey().toBytes()));
    block.setHeader("Key-Computation");

    // tests
    if (keyStoreService.getMi_public().isEqual(this.genitorElement.powZn(keyStoreService.getMi_private()))) {
      System.out.println("Mi test ok");
    } else {
      System.out.println("Mi test error");
    }
    if (elementFromBase64(block.getHi2()).isEqual(hi2)) {
      System.out.println("Hi2 ok");
    } else {
      System.out.println("Hi2 error");
    }

    if (elementFromBase64(block.getSignature()).isEqual(signature)) {
      System.out.println("signature ok");
    } else {
      System.out.println("signature error");
    }

    if (h2Verify(
        elementFromBase64(block.getHi2()),
        keyStoreService.getMi_public(),
        elementFromBase64(block.getSignature()),
        keyStoreService.getPublicKey())) {
      System.out.println("Block " + block.getId() + " is ok");
    } else {
      System.out.println("Block " + block.getId() + " not verified");
    }

    return block;
  }

  public CryptographicMessageParameters produceMessageParametersFromBlock(Block block) {
    var cm = new CryptographicMessageParameters();
    cm.setHi2(block.getHi2());
    cm.setHii(blsSignature(keyStoreService.getMi_public().toBytes()));
    cm.setIp(block.getNextIp());
    cm.setPku(block.getPk());
    cm.setSign1(block.getSignature());
    // System.out.println(block.getSignature());
    cm.setMi(base64Encode(keyStoreService.getMi_public().toBytes()));

    if (!verifyUser(block, cm)) {
      System.out.println("verification process error");
    }

    return cm;
  }

  public Boolean verifyUser(Block block, CryptographicMessageParameters params) {
    return h2Verify(
        elementFromBase64(params.getHi2()),
        elementFromBase64(params.getMi()),
        elementFromBase64(block.getSignature()),
        elementFromBase64(params.getPku()));
  }

  public List<String> computeFsListAndGkForUn(List<String> ms) {
    List<String> fs = new ArrayList<>();
    Element tn = pairing.getZr().newRandomElement().getImmutable();

    Element sumFr = null;

    for (int i = 0; i < ms.size(); i++) {
      var m = ms.get(i);
      var fcurrent = elementFromBase64(m).duplicate().powZn(tn).getImmutable();
      fs.add(base64Encode(fcurrent.toBytes()));

      if (sumFr == null) {
        sumFr = fcurrent.duplicate().getImmutable();
      } else {
        sumFr = sumFr.duplicate().add(fcurrent).getImmutable();
      }
    }

    var betaN = this.genitorElement.duplicate().mul(sumFr).getImmutable();
    var alpha = this.genitorElement.duplicate().powZn(tn).getImmutable();
    // System.out.println("\n\t" + alpha + " \n\t" + betaN);
    keyStoreService.setGroupKey(pairing.pairing(
        alpha,
        betaN).getImmutable());
    System.out.println("START");
    System.out.println(id.getId());
    System.out.println("group key for Un " + keyStoreService.getGroupKey());
    System.out.println("END");

    return fs;
  }

  public void computeGkForUi(List<String> fs, Integer index) {
    var invertedMi = keyStoreService.getMi_private().duplicate().invert().getImmutable();
    Element betaK = null;
    var alphak = invertedMi.duplicate().getImmutable();
    var fk = elementFromBase64(fs.get(fs.size() - 1 - index));
    for (String f : fs) {
      if (betaK == null) {
        betaK = elementFromBase64(f).duplicate().getImmutable();
      } else {
        betaK = betaK.duplicate().add(elementFromBase64(f)).getImmutable();
      }
    }
    alphak = fk.duplicate().powZn(alphak.duplicate()).getImmutable();
    keyStoreService.setGroupKey(pairing.pairing(alphak, this.genitorElement.duplicate().mul(betaK).getImmutable()));
    // System.out.println("\n\t" + alphak + "\n\t" + betaK);
    System.out.println("START");
    System.out.println(id.getId());
    System.out.println("group key for Un-" + index + " " + keyStoreService.getGroupKey());
    System.out.println("END");
  }
  public String h1Gk() throws NoSuchAlgorithmException{
    var hash = MessageDigest.getInstance("SHA-512");
    var gk = keyStoreService.getGroupKey().duplicate();
    return base64Encode(hash.digest(gk.toBytes()));
  }
  public Boolean h1VerifyGk(String base64GkHash) throws NoSuchAlgorithmException{
    return MessageDigest.isEqual(base64Decode(h1Gk()), base64Decode(base64GkHash));
  }

}
