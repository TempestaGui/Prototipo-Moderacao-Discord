package com.prototipoBotDiscor.prototipoDiscord.moderation;

import com.prototipoBotDiscor.prototipoDiscord.gemini.Classificacao;
import com.prototipoBotDiscor.prototipoDiscord.gemini.RespostaGeminiDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RuleEngine {

    private static final double LIMIAR_SPAM = 0.7;
    private static final double LIMIAR_ODIO_ASSEDIO = 0.8;

    public AcaoModeracao avaliar(RespostaGeminiDTO resposta){
        if(resposta == null){
            log.warn("Resposta nula do gemini - aplicando ação padrão de segurança: AVISAR.");
            return AcaoModeracao.AVISAR;
        }

        Classificacao classificacao = resposta.getClassificacao();
        double confianca = resposta.getConfianca();

        if(classificacao == null){
            log.warn("Classificação nula na resposta: Aplicando AVISAR");
            return AcaoModeracao.AVISAR;
        }

        return switch(classificacao){
            case SEGURO -> AcaoModeracao.NENHUMA;
            case SPAM -> confianca > LIMIAR_SPAM ? AcaoModeracao.APAGAR : AcaoModeracao.AVISAR;
            case DISCURSO_DE_ODIO, ASSEDIO -> confianca > LIMIAR_ODIO_ASSEDIO ? AcaoModeracao.BANIR_TEMPORARIO : AcaoModeracao.AVISAR;
        };
    }

}
