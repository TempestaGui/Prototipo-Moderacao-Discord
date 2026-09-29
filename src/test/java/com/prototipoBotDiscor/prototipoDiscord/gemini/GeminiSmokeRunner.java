package com.prototipoBotDiscor.prototipoDiscord.gemini;

import com.prototipoBotDiscor.prototipoDiscord.config.DotenvLoader;
import com.prototipoBotDiscor.prototipoDiscord.dto.MensagemDTO;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

public class GeminiSmokeRunner {

    public static void main(String[] args) {
        DotenvLoader.carregar();

        String API_KEY = System.getProperty("GEMINI_API_KEY");
        WebClient webClient = new GeminiWebClientConfig().geminiWebClient();

        PromptService promptService = new PromptService();
        promptService.carregarPrompt();

        GeminiRateLimiter rateLimiter = new GeminiRateLimiter();

        GeminiService geminiService = new GeminiService(webClient, API_KEY, promptService, new ObjectMapper(), rateLimiter);

        List<MensagemDTO> mensagensDeTeste = List.of(
                mensagem("CLIQUE AQUI AGORA!!! GANHE DINHEIRO RÁPIDO www.link-suspeito.com CLIQUE JÁ"),
                mensagem("você é um idiota inútil, ninguém gosta de você mesmo"),
                mensagem("nossa, que sorte a minha, perdi o ônibus de novo, dia perfeito"),
                mensagem("bom dia pessoal, alguém sabe que horas começa a reunião?")
        );

        for(MensagemDTO mensagem : mensagensDeTeste){
            System.out.println("Mensagem: " + mensagem.getConteudo() +" ===");
            RespostaGeminiDTO resposta = geminiService.analisar(mensagem);

            if(resposta == null){
                System.out.println("Falha ao obter classificação :(");
            } else {
                System.out.println("Classificação: "+resposta.getClassificacao());
                System.out.println("Confiança: "+resposta.getConfianca());
                System.out.println("Justificativa: "+resposta.getJustificativa());
            }
        }
    }

    private static MensagemDTO mensagem(String mensagem){
        return new MensagemDTO("autor id", "autorTeste", "canal id",
                "canalTeste", "memsagem id", mensagem, Instant.now());
    }
}
