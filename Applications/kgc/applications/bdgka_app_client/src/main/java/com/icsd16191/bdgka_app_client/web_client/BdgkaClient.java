package com.icsd16191.bdgka_app_client.web_client;

import com.icsd16191.bdgka_app_client.entities.Block;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class BdgkaClient {
    private WebClient webClient;

    public BdgkaClient(WebClient webClient){
      this.webClient=webClient;
    }

    public Mono<Map<String,Object>> append(Block block){
        return webClient.post()
                .uri("/master/append")
                .bodyValue(block)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {
                });
    }

    public Mono<Map<String,Object>> getAllBlocks(){
        return webClient.get()
                .uri("/master/history")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {
                });
    }
    public Mono<Map<String,Object>> current(){
        return webClient.get()
                .uri("/master/current")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {
                });
    }
    public Mono<Map<String,Object>> getBlockById(String id){
        var sb = new StringBuilder("/master/block/");
        sb.append(id);
        return webClient.get()
                .uri(sb.toString())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {
                });
    }
}
