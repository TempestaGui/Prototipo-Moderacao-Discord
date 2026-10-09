package com.prototipoBotDiscor.prototipoDiscord.moderation;

import com.prototipoBotDiscor.prototipoDiscord.gemini.Classificacao;

import java.sql.Timestamp;
import java.time.Instant;

public record RegistroModeracaoDTO(
        String autorNome,
        String canalNome,
        String mensagem,
        Classificacao classificacao,
        double confianca,
        AcaoModeracao acao,
        Instant timestamp
) {}
