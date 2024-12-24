package com.project.ParellelApiCall.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletableFuture;

@Service
public class ApiCallService {

    private final WebClient webClient;

    public ApiCallService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public CompletableFuture<String> asynchronisedApiCall(String url){

        return CompletableFuture.supplyAsync( () ->
                webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block()
        );

    }

    public String synchronisedApiCall(String url){
                return webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
    }




}
