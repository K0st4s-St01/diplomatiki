package com.icsd16191.bdgka_app.messages;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
@NoArgsConstructor
public class Message implements Serializable{
  private String from;
  private String forWho;
  private String operation;
  private String payload;
}
