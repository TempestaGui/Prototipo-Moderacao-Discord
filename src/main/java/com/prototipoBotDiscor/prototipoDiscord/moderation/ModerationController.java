package com.prototipoBotDiscor.prototipoDiscord.moderation;

import com.prototipoBotDiscor.prototipoDiscord.dto.MensagemDTO;
import com.prototipoBotDiscor.prototipoDiscord.gemini.GeminiService;
import com.prototipoBotDiscor.prototipoDiscord.gemini.RespostaGeminiDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
public class ModerationController {

    private final GeminiService geminiService;
    private final RuleEngine ruleEngine;
    private final ActionExecutor actionExecutor;
    private final ModerationLogService moderationLogService;

    public ModerationController(GeminiService geminiService, RuleEngine ruleEngine, ActionExecutor actionExecutor, ModerationLogService moderationLogService){
        this.geminiService = geminiService;
        this.ruleEngine = ruleEngine;
        this.actionExecutor = actionExecutor;
        this.moderationLogService = moderationLogService;
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

        RegistroModeracaoDTO registroDTO = new RegistroModeracaoDTO(
                dto.getAutorName(),
                dto.getCanalNome(),
                dto.getConteudo(),
                resposta != null ? resposta.getClassificacao() : null,
                resposta != null ? resposta.getConfianca() : 0.0,
                acao,
                Instant.now());

        moderationLogService.registrar(registroDTO);

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
