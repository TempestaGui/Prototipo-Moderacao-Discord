package com.prototipoBotDiscor.prototipoDiscord.configuration;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().isBot()){
            log.info("É bot: {}", event.getAuthor().getName());
            return;
        }

        log.info("Mensagem recebida de {}: {}",
                event.getAuthor().getName(),
                event.getMessage().getContentDisplay());
    }
}