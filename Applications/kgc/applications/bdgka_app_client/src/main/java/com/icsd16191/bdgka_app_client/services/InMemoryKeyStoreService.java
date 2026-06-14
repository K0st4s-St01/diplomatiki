package com.icsd16191.bdgka_app_client.services;

import java.util.Comparator;
import java.util.TreeMap;

import org.springframework.stereotype.Component;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Getter;
import lombok.Setter;

@Component
@Setter
@Getter
public class InMemoryKeyStoreService {
  private Element privateKey; // for signatures
  private Element publicKey; //for signatures
  private TreeMap<String, Element> publicKeys = new TreeMap<>(new IpComparator());// previous and next
  private String next,previous;
  private Element mi_private;
  private Element Mi_public;
  private Element groupKey;


  private static class IpComparator implements Comparator<String> {

    @Override
    public int compare(String arg0, String arg1) {
      return arg0.compareTo(arg1);
    }

  }
}
