package com.prototipoBotDiscor.prototipoDiscord.moderation;

import com.prototipoBotDiscor.prototipoDiscord.dto.MensagemDTO;
import com.prototipoBotDiscor.prototipoDiscord.gemini.GeminiService;
import com.prototipoBotDiscor.prototipoDiscord.gemini.RespostaGeminiDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ModerationController {

    private final GeminiService geminiService;

    public ModerationController(GeminiService geminiService){
        this.geminiService = geminiService;
    }

    public void moderar(MensagemDTO dto){
        log.info("ModerationController recebeu para moderar: {}", dto.getConteudo());

        String resposta = geminiService.analisar(dto);

        log.info("Resposta bruta do gemini: {}",resposta);
    }

}
