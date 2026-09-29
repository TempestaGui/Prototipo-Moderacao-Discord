package com.prototipoBotDiscor.prototipoDiscord.gemini;

import com.prototipoBotDiscor.prototipoDiscord.dto.MensagemDTO;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiService {

    private static final String MODEL = "gemini-3-flash-preview";
    private static final int MAX_RETRIES = 2;
    private static final Duration RETRY_BACKOFF_INICIAL = Duration.ofSeconds(2);

    private final WebClient webClient;
    private final String apiKey;
    private final PromptService promptService;
    private final ObjectMapper objectMapper;

    public GeminiService(WebClient geminiWebClient,
                         @Value("${GEMINI_API_KEY}") String apiKey, PromptService promptService, ObjectMapper objectMapper) {
        this.webClient = geminiWebClient;
        this.apiKey = apiKey;
        this.promptService = promptService;
        this.objectMapper = objectMapper;
    }


    public RespostaGeminiDTO analisar(MensagemDTO mensagem) {
        String promptCompleto = promptService.montarPrompt(mensagem.getConteudo());

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", promptCompleto)
                        ))
                )
        );

        try {
            String respostaBruta = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/" + MODEL + ":generateContent").build())
                    .header("x-goog-api-key", apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(MAX_RETRIES, RETRY_BACKOFF_INICIAL)
                            .filter(this::isErroTransitorio)
                            .doBeforeRetry(sinal -> log.warn("Erro transitório na chamada ao Gemini, tentando novamente (tentativa {}): {}",
                                    sinal.totalRetries() + 1, sinal.failure().getMessage())))
                    .block();
            return parsearResposta(respostaBruta);
        } catch (Exception e) {
            log.error("Erro ao chamar a API Gemini: {}", e.getMessage());
            return null;
        }
    }

    private boolean isErroTransitorio(Throwable throwable){
        if (throwable instanceof WebClientResponseException webEx) {
            HttpStatusCode status = webEx.getStatusCode();
            return status.value() == 503 || status.value() == 429;
        }
        return throwable instanceof ReadTimeoutException
                || throwable.getCause() instanceof ReadTimeoutException;
    }

    private RespostaGeminiDTO parsearResposta(String respostaBruta){
        try{
            //Nivel 1: extrai o texto de dentro da casca da API (candidates[0].content.parts[0].text)
            GeminiAPIResponse apiResponse = objectMapper.readValue(respostaBruta, GeminiAPIResponse.class);
            String textoJson = apiResponse.getCandidates().get(0).getContent().getParts().get(0).getText();

            //Nivel 2: Converter esse texto (que é o json definido no nosso prompt) para o dto estruturado
            return objectMapper.readValue(textoJson, RespostaGeminiDTO.class);
        }catch (Exception e){
            log.error("Erro ao parsear resposta do GEMINI: {}", e.getMessage());
            return null;
        }
    }

}