package com.prototipoBotDiscor.prototipoDiscord.configuration;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DiscordBotConfig {

    @Bean
    public JDA jda(@Value("${bot-token}") String token) throws InterruptedException {
        log.info("Conectando ao Discord...");

        JDA jda = JDABuilder.createDefault(token, GatewayIntent.MESSAGE_CONTENT)
                .build()
                .awaitReady();

        log.info("Bot conectado ao Discord como: {}", jda.getSelfUser().getName());
        return jda;
    }
}
