package com.prototipoBotDiscor.prototipoDiscord.moderation;

import com.prototipoBotDiscor.prototipoDiscord.dto.MensagemDTO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.springframework.stereotype.Service;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ActionExecutor {

    private final String TEXTO_AVISO_DM = "[⚠ ATENÇÂO ⚠] - Sua mensagem foi sinalizada pela moderação automatica. Por favor, revise as diretrizes da comunidade";

    private final JDA jda;
    private final ModerationRulesProperties rules;


    public ActionExecutor(JDA jda, ModerationRulesProperties rules){
        this.jda = jda;
        this.rules = rules;
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

    public void avisarUsuario(MensagemDTO mensagem){
        jda.retrieveUserById(mensagem.getAutorId()).queue(
                user -> user.openPrivateChannel().queue(
                        canalPrivado -> canalPrivado.sendMessage(TEXTO_AVISO_DM).queue(
                                sucesso -> log.info("Aviso enviado por DM para: {}", mensagem.getAutorName()),
                                erro -> tratarFalhaDM(mensagem, erro)
                        )
                ),
                erro -> log.error("Falha ao buscar o usuário {} para enviar aviso: {}", mensagem.getAutorId(), erro.getMessage())
        );
    }

    private void tratarFalhaDM(MensagemDTO mensagem,  Throwable erro){
        boolean dmFechada = erro instanceof ErrorResponseException ere
                && ere.getErrorResponse() == ErrorResponse.CANNOT_SEND_TO_USER;

        if(!dmFechada){
            log.error("Falha inesperada ao enviar DM para {}: {}", mensagem.getAutorName(), erro.getMessage());
            return;
        }

        log.warn("DM fechada para {} — enviando aviso temporário no canal.", mensagem.getAutorName());
        enviarAvisoTemporarioNoCanal(mensagem);
    }

    private void enviarAvisoTemporarioNoCanal(MensagemDTO mensagem){
        TextChannel canal = jda.getTextChannelById(mensagem.getCanalId());

        if (canal == null) {
            log.warn("Canal {} não encontrado — não foi possível enviar aviso de fallback.", mensagem.getCanalId());
            return;
        }

        String textoFallback = "<@" + mensagem.getAutorId() + "> " + TEXTO_AVISO_DM
                + " (não foi possível enviar por DM)";

        canal.sendMessage(textoFallback).queue(
                msgEnviada -> msgEnviada.delete().queueAfter(15, TimeUnit.SECONDS),
                erro -> log.error("Falha ao enviar aviso de fallback no canal: {}", erro.getMessage())
        );
    }


    public void banirTemporario(MensagemDTO mensagem) {
        TextChannel canal = jda.getTextChannelById(mensagem.getCanalId());

        if (canal == null) {
            log.warn("Canal {} não encontrado — não foi possível aplicar timeout.", mensagem.getCanalId());
            return;
        }

        Guild guild = canal.getGuild();
        int duracaoMinutos = rules.getBanimento().getDuracaoMinutos();

        guild.retrieveMemberById(mensagem.getAutorId()).queue(
                member -> guild.timeoutFor(member, Duration.ofMinutes(duracaoMinutos)).queue(
                sucesso -> log.info("Time out de {} min aplicado a {}", duracaoMinutos, mensagem.getAutorName()),
                        erro -> tratarFalhaTimeout(mensagem, erro)
                ),

                erro -> {
                    if(erro instanceof ErrorResponseException ere && ere.getErrorResponse() == ErrorResponse.UNKNOWN_MEMBER){
                        log.warn("Usuário {} não encontrado no servidor (pode ter saído) — timeout não aplicado.",mensagem.getAutorName());
                    } else {
                        log.error("Erro ao buscar membro {} para timeout {}", mensagem.getAutorName(), erro.getMessage());
                    }
                }
        );
    }

    private void tratarFalhaTimeout(MensagemDTO mensagem, Throwable erro) {
        if (erro instanceof InsufficientPermissionException) {
            log.error("Permissão insuficiente para aplicar timeout em {} (verifique o cargo do bot).",
                    mensagem.getAutorName());
        } else {
            log.error("Erro inesperado ao aplicar timeout em {}: {}", mensagem.getAutorName(), erro.getMessage());
        }
    }
}
