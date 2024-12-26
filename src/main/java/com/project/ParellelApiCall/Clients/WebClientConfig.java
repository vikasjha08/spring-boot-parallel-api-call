package com.project.ParellelApiCall.Clients;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(5))
                .doOnConnected(
                        connection -> {
                            connection.addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS)); // Read timeout
                            connection.addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS)); // Write timeout
                        }
                );


        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(loggingFilter()) //For Logging
                .filter(errorHandlingFilter()) //For Error Handling
                .filter(retryFilter()) //retry Filter
                //.filter(authorisationFilter(authKey)) //One of the important check is auth check, can handle it as well
                .defaultHeader("Content-Type", "application/json") // Default headers
                .defaultHeader("Accept", "application/json") //Can be used to add default header
                .baseUrl("https://jsonplaceholder.typicode.com/photos/");

    }

    private ExchangeFilterFunction retryFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(
                response -> {
                    //These are dummy handlers, can implement actual retry logic here
                    if (response.statusCode().is4xxClientError()) {
                        return Mono.error(new RuntimeException("4xx Client Error"));
                    }
                    if (response.statusCode().is5xxServerError()) {
                        return Mono.error(new RuntimeException("5xx Server Error"));
                    }
                    return Mono.just(response);
                });
    }

    private ExchangeFilterFunction errorHandlingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(
                response -> {
                    if (response.statusCode().isError()) {
                        System.err.println("Error response: " + response.statusCode());
                        //Can throw custom exception here
                        //throw new RuntimeException();
                    }
                    return Mono.just(response);
                });
    }


    private ExchangeFilterFunction loggingFilter(){

        return ExchangeFilterFunction.ofRequestProcessor(request -> {
                    System.out.println("Request: " + request.method() + " " + request.url());
                    return Mono.just(request);
                })
                .andThen(
                        ExchangeFilterFunction.ofResponseProcessor( response -> {
                            System.out.println("Response: " + response.statusCode());
                            return Mono.just(response);
                        })
                );


    }



}
