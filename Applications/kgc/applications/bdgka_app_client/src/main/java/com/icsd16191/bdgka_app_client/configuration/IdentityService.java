package com.icsd16191.bdgka_app_client.configuration;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Configuration
public class IdentityService{
  private final ServerProperties context;

  @Bean
  public Identity identity() throws UnknownHostException{
    String ip = Inet4Address.getLocalHost().toString();
    return new Identity(ip+":"+context.getPort(),false);
  }
  @AllArgsConstructor
  @Getter
  public static class Identity{
    private String id;
    private Boolean first;
    public void setFirst(Boolean value){
      first = value;
    }
  }
}

