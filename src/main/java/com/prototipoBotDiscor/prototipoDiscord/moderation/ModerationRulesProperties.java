package com.prototipoBotDiscor.prototipoDiscord.moderation;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "moderation.rules")
public class ModerationRulesProperties {

    private Limiar spam = new Limiar();
    private Limiar odioAssedio = new Limiar();

    @Data
    public static class Limiar{
        private double threshold;
    }
}
