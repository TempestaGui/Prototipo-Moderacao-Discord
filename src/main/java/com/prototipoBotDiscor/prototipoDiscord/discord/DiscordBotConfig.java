package com.prototipoBotDiscor.prototipoDiscord.discord;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@Profile("!test")
public class DiscordBotConfig {

    @Bean
    public JDA jda(@Value("${DISCORD_BOT_TOKEN}") String token,
                   MessageListener messageListener) throws InterruptedException {
        log.info("Conectando ao Discord...");

        // IMPORTANTE: usar createDefault(token) SEM passar intents diretamente no metodo.
        // A sobrecarga createDefault(token, GatewayIntent...) SUBSTITUI o conjunto
        // padrão de intents pelos que forem passados, em vez de adicioná-los. Isso
        // desabilitaria silenciosamente o GatewayIntent.GUILD_MESSAGES (necessário para
        // receber MessageReceivedEvent de canais de servidor) e faria o listener nunca
        // disparar, mesmo com a conexão e o intent MESSAGE_CONTENT ativos no portal.
        // Por isso: createDefault(token) mantém o conjunto padrão (que já inclui
        // GUILD_MESSAGES) e .enableIntents(...) apenas ADICIONA o intent privilegiado
        // MESSAGE_CONTENT por cima, sem remover os demais.
        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(messageListener)
                .build()
                .awaitReady();

        log.info("Bot conectado ao Discord como: {}", jda.getSelfUser().getAsTag());
        return jda;
    }
}