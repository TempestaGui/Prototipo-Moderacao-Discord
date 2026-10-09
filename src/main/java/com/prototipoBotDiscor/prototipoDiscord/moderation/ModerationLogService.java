package com.prototipoBotDiscor.prototipoDiscord.moderation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
@Service
public class ModerationLogService {
    private static final Path LOG_PATH = Path.of("logs", "moderacao.jsonl");

    private final ObjectMapper objectMapper;

    public ModerationLogService(ObjectMapper objectMapper) throws IOException {
        this.objectMapper = objectMapper;
        criarDiretorioSeNecessario();
    }

    private void criarDiretorioSeNecessario() throws IOException {
        try{
            Files.createDirectories(LOG_PATH.getParent());
        } catch(IOException e){
            log.error("Falha ao criar diretório de logs: {}", e.getMessage());
        }
    }

    public synchronized void registrar(RegistroModeracaoDTO registro){
        try{
            String linhaJson = objectMapper.writeValueAsString(registro);
            Files.writeString(LOG_PATH, linhaJson + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch(IOException e){
            log.error("Falha ao gravar registro de moderação no log: {}", e.getMessage());
        }
    }
}
