package com.prototipoBotDiscor.prototipoDiscord.discord;

import com.prototipoBotDiscor.prototipoDiscord.dto.MensagemDTO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class MessageListener extends ListenerAdapter {

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

        log.info("id do usuario: {} | avatar: {}" , mensagemRecebida.getAutorId(), event.getAuthor().getAvatar());

//        if(mensagemRecebida.getConteudo().equalsIgnoreCase("oi")){
//            event.getMessage().delete().queue(
//                    success -> log.info("Mensagem {} deletada com sucesso", mensagemRecebida.getConteudo()),
//                    error -> log.error("Falha ao apagar mensagem: {}", error.getMessage()));
//        }

//        boolean temImagem = event.getMessage().getAttachments().stream().anyMatch(Message.Attachment::isImage);
//
//        if(temImagem){
//            event.getMessage().delete().queue(
//                    success -> log.info("Mensagem com imagem apagada. Autor: {}", mensagemRecebida.getAutorName()),
//                    error -> log.error("Falha ao apagar mensagem com imagem: {}", error.getMessage())
//            );
//        }

      if(!mensagemRecebida.getAutorName().equalsIgnoreCase("tempesta9351")){
           event.getMessage().delete().queue(
                  success -> log.info("Mensagem {} deletada com sucesso", mensagemRecebida.getConteudo()),
                   error -> log.error("Falha ao apagar mensagem: {}", error.getMessage()));
     }
  }
}