package com.prototipoBotDiscor.prototipoDiscord.gemini;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespostaGeminiDTO {
    private String classificacao;
    private String justificativa;
    private double confianca;
}
