package com.prototipoBotDiscor.prototipoDiscord.gemini;

import com.prototipoBotDiscor.prototipoDiscord.dto.MensagemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiService {

    private static final String MODEL = "gemini-3-flash-preview";

    private final WebClient webClient;
    private final String apiKey;

    public GeminiService(WebClient geminiWebClient,
                         @Value("${GEMINI_API_KEY}") String apiKey) {
        this.webClient = geminiWebClient;
        this.apiKey = apiKey;
    }

    // Retorno provisório (String bruta) — o parsing estruturado
    // pro RespostaGeminiDTO vem nas demandas TCCM-018/019.
    public String analisar(MensagemDTO mensagem) {
        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", mensagem.getConteudo())
                        ))
                )
        );

        try {
            return webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/" + MODEL + ":generateContent").build())
                    .header("x-goog-api-key", apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Erro ao chamar a API Gemini: {}", e.getMessage());
            return null;
        }
    }
}