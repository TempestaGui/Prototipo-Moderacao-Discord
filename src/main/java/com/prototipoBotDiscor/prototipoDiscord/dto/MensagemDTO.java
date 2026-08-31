package com.prototipoBotDiscor.prototipoDiscord.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data //responsavel pelos getters, setters
@NoArgsConstructor //cria o constructor sem argumentos
@AllArgsConstructor //cria o constructor com todos argumentos
public class MensagemDTO {
    private String autorId;
    private String autorName;
    private String canalId;
    private String canalNome;
    private String mensagemId;
    private String conteudo;
    private Instant timestamp;
}
