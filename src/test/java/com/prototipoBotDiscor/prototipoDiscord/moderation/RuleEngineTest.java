package com.prototipoBotDiscor.prototipoDiscord.moderation;

import com.prototipoBotDiscor.prototipoDiscord.gemini.Classificacao;
import com.prototipoBotDiscor.prototipoDiscord.gemini.RespostaGeminiDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RuleEngineTest {

    private final RuleEngine rule = new RuleEngine(CriarRulesPadrao());

    private static ModerationRulesProperties CriarRulesPadrao(){
        ModerationRulesProperties prop = new ModerationRulesProperties();
        prop.getSpam().setThreshold(0.7);
        prop.getOdioAssedio().setThreshold(0.8);
        return prop;
    }

    @ParameterizedTest(name = "{0} com confiança {1} deve resultar em {2}")
    @CsvSource({
            "SEGURO, 0.0, NENHUMA",
            "SEGURO, 0.5, NENHUMA",
            "SEGURO, 0.99, NENHUMA",

            "SPAM, 0.0, AVISAR",
            "SPAM, 0.5, AVISAR",
            "SPAM, 0.7, AVISAR",
            "SPAM, 0.71, APAGAR",
            "SPAM, 0.9, APAGAR",
            "SPAM, 1.0, APAGAR",

            "DISCURSO_DE_ODIO, 0.0, AVISAR",
            "DISCURSO_DE_ODIO, 0.5, AVISAR",
            "DISCURSO_DE_ODIO, 0.8, AVISAR",
            "DISCURSO_DE_ODIO, 0.81, BANIR_TEMPORARIO",
            "DISCURSO_DE_ODIO, 1.0, BANIR_TEMPORARIO",

            "ASSEDIO, 0.0, AVISAR",
            "ASSEDIO, 0.5, AVISAR",
            "ASSEDIO, 0.8, AVISAR",
            "ASSEDIO, 0.81, BANIR_TEMPORARIO",
            "ASSEDIO, 1.0, BANIR_TEMPORARIO"
    })
    void deveAvaliarCorretamenteConformeMatrizDeDecisao(Classificacao classificacao, double confianca, AcaoModeracao esperado){

        RespostaGeminiDTO resposta = new RespostaGeminiDTO(classificacao, "justificativa de teste", confianca);

        AcaoModeracao resultado = rule.avaliar(resposta);

        assertEquals(esperado, resultado);
    }

    @Test
    void deveRetornarAvisarQuandoRespostaForNula(){
        assertEquals(AcaoModeracao.AVISAR, rule.avaliar(null));
    }

    @Test
    void deveRetornarAvisarQuandoClassificacaoForNula(){
        RespostaGeminiDTO resposta = new RespostaGeminiDTO(null, "teste", 0.9);
        assertEquals(AcaoModeracao.AVISAR, rule.avaliar(resposta));
    }

    @Test
    void deveSerDeterministico(){
        RespostaGeminiDTO resposta = new RespostaGeminiDTO(Classificacao.DISCURSO_DE_ODIO, "teste", 0.95);

        AcaoModeracao resultado1 = rule.avaliar(resposta);
        AcaoModeracao resultado2 = rule.avaliar(resposta);
        AcaoModeracao resultado3 = rule.avaliar(resposta);

        assertEquals(resultado1, resultado2);
        assertEquals(resultado2, resultado3);
    }

}
