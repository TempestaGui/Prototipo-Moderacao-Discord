package com.prototipoBotDiscor.prototipoDiscord.moderation;

import com.prototipoBotDiscor.prototipoDiscord.dto.MensagemDTO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;

@Slf4j
@Service
public class ActionExecutor {

    private final JDA jda;

    public ActionExecutor(JDA jda){
        this.jda = jda;
    }

    public void apagarMensagem(MensagemDTO resposta){
        TextChannel canal = jda.getTextChannelById(resposta.getCanalId());

        if(canal == null){
            log.warn("Canal {} não encontrado então não foi possivel apagr a mensagem: {}", resposta.getCanalNome(), resposta.getConteudo());
            return;
        }

        canal.deleteMessageById(resposta.getMensagemId()).queue(
                sucesso -> log.info("Mensagem: {} apagada com sucessono canal: {}", resposta.getConteudo(), resposta.getCanalNome()),
                erro -> {
                    if (erro instanceof ErrorResponseException ere) {
                        log.warn("Falha ao apagar mensagem {}: {}",
                                resposta.getConteudo(), ere.getMessage());
                    } else {
                        log.error("Erro inesperado ao apagar mensagem {}: {}",
                                resposta.getConteudo(), erro.getMessage());
                    }
                }
        );
    }
}
