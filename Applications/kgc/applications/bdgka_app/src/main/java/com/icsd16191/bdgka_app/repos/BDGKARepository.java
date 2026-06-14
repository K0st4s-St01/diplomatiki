package com.icsd16191.bdgka_app.repos;

import lombok.Getter;

import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.Hash;
import org.hyperledger.fabric.client.Network;
import org.hyperledger.fabric.client.identity.Identities;
import org.hyperledger.fabric.client.identity.Identity;
import org.hyperledger.fabric.client.identity.Signer;
import org.hyperledger.fabric.client.identity.Signers;
import org.hyperledger.fabric.client.identity.X509Identity;
import org.springframework.stereotype.Component;

import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
@Component
public class BDGKARepository {
  private final String mspId = "Org1MSP";
  private final String channelName = "mychannel";
  private final String chaincodeName = "bdgka_chaincode";

  private final Path cryptoRootDir = Paths
      .get("../../fabric-samples/test-network/organizations/peerOrganizations/org1.example.com");
  private final Path certDirPath = cryptoRootDir.resolve("users/User1@org1.example.com/msp/signcerts");
  private final Path keyDirPath = cryptoRootDir.resolve("users/User1@org1.example.com/msp/keystore");
  private final Path tlsCertPath = cryptoRootDir.resolve("peers/peer0.org1.example.com/tls/ca.crt");

  private final Gateway gateway;
  private final Network network;
  private final Contract contract;
  public BDGKARepository() throws Exception {
    var channel = connect();

    var builder = Gateway.newInstance();
    builder.identity(newIdentity());
    builder.signer(newSigner());
    builder.hash(Hash::sha256);
    builder.connection(channel);
    gateway = builder.connect();
    network = gateway.getNetwork(this.channelName);
    contract = network.getContract(this.chaincodeName);
  }

  public String getMspId() {
	return mspId;
}

  public String getChannelName() {
	return channelName;
  }

  public String getChaincodeName() {
	return chaincodeName;
  }

  public Path getCryptoRootDir() {
	return cryptoRootDir;
  }

  public Path getCertDirPath() {
	return certDirPath;
  }

  public Path getKeyDirPath() {
	return keyDirPath;
  }

  public Path getTlsCertPath() {
	return tlsCertPath;
  }

  public Gateway getGateway() {
	return gateway;
  }

  public Network getNetwork() {
	return network;
  }

  public Contract getContract() {
	return contract;
  }

  @PreDestroy
  public void close() {
    gateway.close();
  }

  private ManagedChannel connect() throws IOException {
    var creds = TlsChannelCredentials.newBuilder()
        .trustManager(tlsCertPath.toFile())
        .build();
    return Grpc.newChannelBuilder("localhost:7051", creds)
        .overrideAuthority("peer0.org1.example.com")
        .build();
  }

  private Identity newIdentity() throws Exception {
    try (var certReader = Files.newBufferedReader(getFirstFilePath(certDirPath))) {
      var cert = Identities.readX509Certificate(certReader);
      return new X509Identity(mspId, cert);
    }
  }
  private Path getFirstFilePath(Path dirPath) throws Exception {
    try (var keyFiles = Files.list(dirPath)) {
      return keyFiles.findFirst().orElseThrow();
    }
  }
  private Signer newSigner() throws Exception{
    try(var keyReader = Files.newBufferedReader(getFirstFilePath(keyDirPath))){
      var privateKey = Identities.readPrivateKey(keyReader);
      return Signers.newPrivateKeySigner(privateKey);
    }
  }
}
