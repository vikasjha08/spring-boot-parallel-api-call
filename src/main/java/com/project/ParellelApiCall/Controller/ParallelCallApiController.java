package com.project.ParellelApiCall.Controller;

import com.project.ParellelApiCall.Service.ApiCallService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@RestController
public class ParallelCallApiController {

    private final ApiCallService apiCallService;

    public ParallelCallApiController(ApiCallService apiCallService){
        this.apiCallService = apiCallService;
    }

    @GetMapping("/parallelCall")
    public String parallelCall(){

        List<String> urls = IntStream.rangeClosed(1, 2000).mapToObj(String::valueOf).toList();
        List<CompletableFuture<String>> futures = urls.stream()
                .map(apiCallService::asynchronisedApiCall)
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return " Operation Completed";

    }

    @GetMapping("/sequentialCall")
    public String sequentialCall(){

        List<String> urls = IntStream.rangeClosed(1, 2000).mapToObj(String::valueOf).toList();
        List<String> finalList = urls.stream()
                .map(apiCallService::synchronisedApiCall)
                .toList();

        return " Operation Completed";

    }



}
