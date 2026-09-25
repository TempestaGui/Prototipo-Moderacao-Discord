package com.prototipoBotDiscor.prototipoDiscord.discord;

import com.prototipoBotDiscor.prototipoDiscord.dto.MensagemDTO;
import com.prototipoBotDiscor.prototipoDiscord.moderation.ModerationController;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class MessageListener extends ListenerAdapter {

    private final ModerationController moderationController;

    public MessageListener(ModerationController moderationController){
        this.moderationController = moderationController;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().isBot()){
            log.info("É bot: {}", event.getAuthor().getName());
            return;
        }

        MensagemDTO mensagemRecebida = new MensagemDTO(
                event.getAuthor().getId(),
                event.getAuthor().getName(),
                event.getChannel().getId(),
                event.getChannel().getName(),
                event.getMessageId(),
                event.getMessage().getContentDisplay(),
                Instant.now()
        );

        log.info("Mensagem recebida de autor - {} | no canal: {} | conteudo da mensagemRecebida: {}",
                mensagemRecebida.getAutorName(),
                mensagemRecebida.getCanalNome(), mensagemRecebida.getConteudo());

        moderationController.moderar(mensagemRecebida);
  }
}