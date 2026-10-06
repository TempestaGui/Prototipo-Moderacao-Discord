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
    private final RuleEngine ruleEngine;
    private final ActionExecutor actionExecutor;

    public ModerationController(GeminiService geminiService, RuleEngine ruleEngine, ActionExecutor actionExecutor){
        this.geminiService = geminiService;
        this.ruleEngine = ruleEngine;
        this.actionExecutor = actionExecutor;
    }

    public void moderar(MensagemDTO dto){
        log.info("ModerationController recebeu para moderar: {}", dto.getConteudo());

        RespostaGeminiDTO resposta = geminiService.analisar(dto);

        if(resposta == null){
            log.warn("Não foi possivel obter uma classificação valida para a mensagem: {}", dto.getConteudo());
            return;
        }

        AcaoModeracao acao = ruleEngine.avaliar(resposta);

        log.info("Ação concluida: {} (mensagem: {})", acao, dto.getConteudo());
        log.info("Resultado da anlise do GEMINI: Classificação: {} | Confiança: {} | Justificativa: {}",
                resposta.getClassificacao(), resposta.getConfianca(), resposta.getJustificativa());

        switch (acao) {
            case APAGAR -> actionExecutor.apagarMensagem(dto);
            case AVISAR -> actionExecutor.avisarUsuario(dto);
            case BANIR_TEMPORARIO -> actionExecutor.banirTemporario(dto);
            case NENHUMA -> {
                // nenhuma ação necessária
            }
        }
    }

}
