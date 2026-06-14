package com.icsd16191.bdgka_app_client.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class Block {
  @JsonProperty
  private String id;
  @JsonProperty
  private String header;
  @JsonProperty
  private String nextIp;
  @JsonProperty
  private String hi2;
  @JsonProperty
  private String pk;
  @JsonProperty
  private List<String> ms;
  @JsonProperty
  private String signature;
  @JsonProperty
  private Long timestamp;
  @JsonProperty
  private String prevHash;

}
