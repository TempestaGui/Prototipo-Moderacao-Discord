package com.prototipoBotDiscor.prototipoDiscord.gemini;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

@Configuration
public class GeminiWebClientConfig {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models";
    private static final int TIMEOUT_SEGUNDOS = 30;

    @Bean
    public WebClient geminiWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT_SEGUNDOS * 1000)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)));

        return WebClient.builder()
                .baseUrl(BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}

