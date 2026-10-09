package com.prototipoBotDiscor.prototipoDiscord.moderation;

import com.prototipoBotDiscor.prototipoDiscord.dto.MensagemDTO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.HierarchyException;
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
        executarComTratamentoDePermissao(() -> {
            TextChannel canal = jda.getTextChannelById(resposta.getCanalId());

            if (canal == null) {
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
        }, "apagar mensagem de", resposta);
    }

    public void avisarUsuario(MensagemDTO mensagem){
        executarComTratamentoDePermissao(() ->
            jda.retrieveUserById(mensagem.getAutorId()).queue(
                    user -> user.openPrivateChannel().queue(
                            canalPrivado -> canalPrivado.sendMessage(TEXTO_AVISO_DM).queue(
                                    sucesso -> log.info("Aviso enviado por DM para: {}", mensagem.getAutorName()),
                                    erro -> tratarFalhaDM(mensagem, erro)
                            )
                    ),
                    erro -> log.error("Falha ao buscar o usuário {} para enviar aviso: {}", mensagem.getAutorId(), erro.getMessage())
        ), "Avisar", mensagem);
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
        executarComTratamentoDePermissao(() -> {
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
        }, "enviar aviso de fallback para", mensagem);
    }


    public void banirTemporario(MensagemDTO mensagem) {
        executarComTratamentoDePermissao(() -> {
            TextChannel canal = jda.getTextChannelById(mensagem.getCanalId());
            if (canal == null) {
                log.warn("Canal {} não encontrado — não foi possível aplicar timeout.", mensagem.getCanalId());
                return;
            }

            Guild guild = canal.getGuild();
            int duracaoMinutos = rules.getBanimento().getDuracaoMinutos();

            guild.retrieveMemberById(mensagem.getAutorId()).queue(
                    member -> executarComTratamentoDePermissao(() ->
                            guild.timeoutFor(member, Duration.ofMinutes(duracaoMinutos)).queue(
                                    sucesso -> log.info("Timeout de {} min aplicado a {}.",
                                            duracaoMinutos, mensagem.getAutorName()),
                                    erro -> log.error("Erro ao aplicar timeout em {}: {}",
                                            mensagem.getAutorName(), erro.getMessage())
                            ), "aplicar timeout em", mensagem),
                    erro -> {
                        if (erro instanceof ErrorResponseException ere && ere.getErrorResponse() == ErrorResponse.UNKNOWN_MEMBER) {
                            log.warn("Usuário {} não encontrado no servidor (pode ter saído) — timeout não aplicado.",
                                    mensagem.getAutorName());
                        } else {
                            log.error("Erro ao buscar membro {} para timeout: {}", mensagem.getAutorName(), erro.getMessage());
                        }
                    }
            );
        }, "banir temporariamente", mensagem);
    }

    private void executarComTratamentoDePermissao(Runnable acao, String descricaoAcao, MensagemDTO mensagem){
        try{
            acao.run();
        } catch(InsufficientPermissionException | HierarchyException  e){
            String mensagemErro = String.format(
                    "Permissão insuficiente para %s a mensagem de %s (canal: %s): %s", descricaoAcao, mensagem.getAutorName(),
                    mensagem.getCanalNome(), e.getMessage()
            );

            log.error(mensagemErro);
            notificarAuditoria("⚠️ " + mensagemErro);
        }
    }

    private void notificarAuditoria(String texto){
        String canalAuditoriaId = rules.getCanalAuditoriaId();
        if(canalAuditoriaId == null || canalAuditoriaId.isBlank()){
            return;
        }

        TextChannel canalAuditoria = jda.getTextChannelById(canalAuditoriaId);
        if(canalAuditoria != null){
            canalAuditoria.sendMessage(texto).queue();
        } else {
            log.warn("Canal de auditoria configurado ({}) não foi encontrado.", canalAuditoriaId);
        }
    }
}
