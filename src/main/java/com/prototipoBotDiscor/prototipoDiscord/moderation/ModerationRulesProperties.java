package com.prototipoBotDiscor.prototipoDiscord.moderation;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "moderation.rules")
public class ModerationRulesProperties {

    private Limiar spam = new Limiar();
    private Limiar odioAssedio = new Limiar();
    private Banimento banimento = new Banimento();
    private String canalAuditoriaId;

    @Data
    public static class Limiar{
        private double threshold;
    }

    @Data
    public static class Banimento{
        private int duracaoMinutos = 10;
    }
}
