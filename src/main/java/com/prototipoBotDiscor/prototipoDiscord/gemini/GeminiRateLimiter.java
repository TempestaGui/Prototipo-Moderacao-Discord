package com.prototipoBotDiscor.prototipoDiscord.gemini;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;

@Slf4j
@Component
public class GeminiRateLimiter {

    private static final int MAX_REQUISICOES_POR_JANELA = 5;
    private static final long JANELA_MS = Duration.ofMinutes(1).toMillis();

    private final Deque<Long> timestamps = new ArrayDeque<>();

    public synchronized void aguardarPermissao() {
        while (true) {
            long agora = System.currentTimeMillis();

            while (!timestamps.isEmpty() && (agora - timestamps.peekFirst()) >= JANELA_MS) {
                timestamps.pollFirst();
            }

            if (timestamps.size() < MAX_REQUISICOES_POR_JANELA) {
                timestamps.addLast(agora);
                return;
            }

            long tempoEspera = JANELA_MS - (agora - timestamps.peekFirst());
            log.info("Limite de {} req/min atingido. Aguardando {} ms antes da próxima chamada ao Gemini.",
                    MAX_REQUISICOES_POR_JANELA, tempoEspera);
            try {
                Thread.sleep(tempoEspera + 50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrompido enquanto aguardava rate limit", e);
            }
        }
    }
}