package com.prototipoBotDiscor.prototipoDiscord.gemini;

import com.prototipoBotDiscor.prototipoDiscord.dto.MensagemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GeminiService {

    public RespostaGeminiDTO analisar(MensagemDTO dto){
        log.info("[STUB] - Simulando analise de mensagem: {}", dto.getConteudo());

        return new RespostaGeminiDTO("Seguro", "Resposta Simulada (STUB) para testes", 1.0);
    }
}
