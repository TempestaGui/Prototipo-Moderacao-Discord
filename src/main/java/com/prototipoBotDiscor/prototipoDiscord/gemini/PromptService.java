package com.prototipoBotDiscor.prototipoDiscord.gemini;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Slf4j
@Service
public class PromptService {

    private static final String PROMPT_PATH = "prompts/moderation-prompt.txt";
    private static final String PLACEHOLDER = "{MENSAGEM}";

    private String promptBase;

    @PostConstruct
    private void carregarPrompt() {
        try {
            ClassPathResource resource = new ClassPathResource(PROMPT_PATH);
            promptBase = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
            log.info("Prompt de moderação carregado: {} caracteres", promptBase.length());

        } catch (Exception e){
            throw new IllegalStateException("Falha ao carregar prompt base: "+PROMPT_PATH, e);
        }
    }

    public String montarPrompt(String conteudoMensagem){
        return promptBase.replace(PLACEHOLDER, conteudoMensagem);
    }
}
