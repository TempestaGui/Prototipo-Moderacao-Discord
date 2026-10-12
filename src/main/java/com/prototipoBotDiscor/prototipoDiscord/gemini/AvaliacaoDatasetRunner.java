package com.prototipoBotDiscor.prototipoDiscord.gemini;

import com.prototipoBotDiscor.prototipoDiscord.config.DotenvLoader;
import com.prototipoBotDiscor.prototipoDiscord.dto.MensagemDTO;
import com.prototipoBotDiscor.prototipoDiscord.moderation.AcaoModeracao;
import com.prototipoBotDiscor.prototipoDiscord.moderation.ModerationRulesProperties;
import com.prototipoBotDiscor.prototipoDiscord.moderation.RuleEngine;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AvaliacaoDatasetRunner {

    private static final Path DATASET = Path.of("src/main/resources/datasetMensagens/dataset-moderacao.csv");
    private static final Path RESULTADOS = Path.of("resultados/resultados-gemini.csv");

    private static final int LIMITE_POR_EXECUCAO = 18;
    private static final String CABECALHO =
            "id,mensagem,categoria_esperada,tipo,categoria_obtida,confianca,acao,tempo_ms,status";

    public static void main(String[] args) throws IOException {
        DotenvLoader.carregar();

        GeminiService gemini = criarGeminiService();
        RuleEngine ruleEngine = criarRuleEngine();

        List<String[]> dataset = lerCsv(DATASET);
        Set<String> idsConcluidos = carregarIdsConcluidos();

        Files.createDirectories(RESULTADOS.getParent());
        if (!Files.exists(RESULTADOS)) {
            Files.writeString(RESULTADOS, CABECALHO + System.lineSeparator(), StandardCharsets.UTF_8);
        }

        int chamadas = 0;
        for (int i = 1; i < dataset.size(); i++) {
            String[] linha = dataset.get(i);
            String id = linha[0];
            String mensagem = linha[1];
            String esperada = linha[2];
            String tipo = linha[3];

            if (idsConcluidos.contains(id)) {
                continue;
            }
            if (chamadas >= LIMITE_POR_EXECUCAO) {
                System.out.println("Limite de " + LIMITE_POR_EXECUCAO
                        + " chamadas atingido. Rode de novo amanhã para continuar.");
                break;
            }

            MensagemDTO dto = criarMensagemDTO(id, mensagem);

            long inicio = System.nanoTime();
            RespostaGeminiDTO resposta = gemini.analisar(dto);
            long tempoMs = (System.nanoTime() - inicio) / 1_000_000;
            chamadas++;

            String registro;
            if (resposta == null) {
                registro = montarLinha(id, mensagem, esperada, tipo, "", "", "", tempoMs, "FALHA");
            } else {
                AcaoModeracao acao = ruleEngine.avaliar(resposta);
                registro = montarLinha(id, mensagem, esperada, tipo,
                        String.valueOf(resposta.getClassificacao()),
                        String.valueOf(resposta.getConfianca()),
                        String.valueOf(acao), tempoMs, "OK");
            }

            // Grava a cada linha: se der erro no meio, nada se perde.
            Files.writeString(RESULTADOS, registro + System.lineSeparator(),
                    StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
            System.out.printf("[%s] esperado=%s -> %s%n", id, esperada, registro.split(",")[registro.split(",").length - 1]);
        }

        System.out.println("Concluído. Resultados em " + RESULTADOS.toAbsolutePath());
    }


    private static GeminiService criarGeminiService() {
        String apiKey = System.getProperty("GEMINI_API_KEY");
        WebClient webClient = new GeminiWebClientConfig().geminiWebClient();

        PromptService promptService = new PromptService();
        promptService.carregarPrompt();

        return new GeminiService(webClient, apiKey, promptService, new ObjectMapper(), new GeminiRateLimiter());
    }

    private static RuleEngine criarRuleEngine() {
        ModerationRulesProperties props = new ModerationRulesProperties();
        props.getSpam().setThreshold(0.7);
        props.getOdioAssedio().setThreshold(0.8);
        return new RuleEngine(props);
    }

    private static Set<String> carregarIdsConcluidos() throws IOException {
        Set<String> ids = new HashSet<>();
        if (!Files.exists(RESULTADOS)) {
            return ids;
        }
        List<String[]> linhas = lerCsv(RESULTADOS);
        for (int i = 1; i < linhas.size(); i++) {
            String[] l = linhas.get(i);
            if (l.length >= 9 && "OK".equals(l[8])) {
                ids.add(l[0]);
            }
        }
        return ids;
    }

    private static String montarLinha(String id, String mensagem, String esperada, String tipo,
                                      String obtida, String confianca, String acao,
                                      long tempoMs, String status) {
        return String.join(",", id, escapar(mensagem), esperada, tipo,
                obtida, confianca, acao, String.valueOf(tempoMs), status);
    }

    private static String escapar(String valor) {
        return "\"" + valor.replace("\"", "\"\"") + "\"";
    }

    private static List<String[]> lerCsv(Path caminho) throws IOException {
        List<String[]> linhas = new ArrayList<>();
        for (String linha : Files.readAllLines(caminho, StandardCharsets.UTF_8)) {
            if (!linha.isBlank()) {
                linhas.add(dividirLinha(linha));
            }
        }
        return linhas;
    }


    private static String[] dividirLinha(String linha) {
        List<String> campos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean entreAspas = false;

        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '"') {
                if (entreAspas && i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
                    atual.append('"'); // aspas escapadas ("")
                    i++;
                } else {
                    entreAspas = !entreAspas;
                }
            } else if (c == ',' && !entreAspas) {
                campos.add(atual.toString());
                atual.setLength(0);
            } else {
                atual.append(c);
            }
        }
        campos.add(atual.toString());
        return campos.toArray(new String[0]);
    }

    private static MensagemDTO criarMensagemDTO(String id, String conteudo) {
        MensagemDTO dto = new MensagemDTO();
        dto.setAutorId("dataset-autor");
        dto.setAutorName("dataset-autor");
        dto.setCanalId("dataset-canal");
        dto.setCanalNome("dataset");
        dto.setMensagemId("dataset-" + id);
        dto.setConteudo(conteudo);
        return dto;
    }
}

