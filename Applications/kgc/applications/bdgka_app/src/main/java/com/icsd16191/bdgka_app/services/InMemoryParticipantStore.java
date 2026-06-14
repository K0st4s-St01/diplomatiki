package com.icsd16191.bdgka_app.services;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.activemq.artemis.utils.collections.ConcurrentHashSet;
import org.springframework.stereotype.Component;

@Component
public class InMemoryParticipantStore {
  private Set<String> ips = new ConcurrentHashSet<>();

  public void add(String ip) {
    this.ips.add(ip);
  }

  public Set<String> get() {
    var set = new TreeSet<String>(new Comparator<String>() {

      @Override
      public int compare(String arg0, String arg1) {
        return arg0.compareTo(arg1);
      }

    });
    set.addAll(ips);
    return set;
  }
}
