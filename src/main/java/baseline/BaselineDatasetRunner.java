package baseline;



import com.prototipoBotDiscor.prototipoDiscord.gemini.Classificacao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BaselineDatasetRunner {

    private static final Path DATASET = Path.of("src/main/resources/datasetMensagens/dataset-moderacao.csv");
    private static final Path RESULTADOS = Path.of("resultados/resultados-baseline.csv");


    public static void main(String[] args) throws IOException {
        FiltroPalavrasChave filtroPalavrasChave = new FiltroPalavrasChave();
        List<String> saida = new ArrayList<>();

        saida.add("id,mensagem,categoria_esperada,tipo,categoria_obtida,confianca,acao,tempo_ms,status");

        List<String> linhas = Files.readAllLines(DATASET, StandardCharsets.UTF_8);
        for(int i = 1; i < linhas.size(); i++){ // comecar com 1 para pular o cabeçaho
            if(linhas.get(i).isBlank()){
                continue;
            }

            String[] c = dividirLinhas(linhas.get(i));
            long inicio = System.nanoTime();
            Classificacao classificacaoObtida = filtroPalavrasChave.classificar(c[1]);
            long tempoMs = (System.nanoTime() - inicio) / 1_000_000;

            saida.add(String.join(",", c[0], "\"" + c[1].replace("\"", "\"\"") + "\"",
                    c[2], c[3], classificacaoObtida.name(), "", "", String.valueOf(tempoMs), "OK"));
        }

        Files.createDirectories(RESULTADOS.getParent());
        Files.write(RESULTADOS, saida, StandardCharsets.UTF_8);
        System.out.println("Baseline gravado em " + RESULTADOS.toAbsolutePath());
    }

    private static String[] dividirLinhas(String linhas){
        List<String> campos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean entreAspas = false;

        for(int i = 0; i<linhas.length(); i++){
            char ch = linhas.charAt(i);

            if(ch == '"') {
                if(entreAspas && i+1 < linhas.length() && linhas.charAt(i + 1) == '"'){
                    atual.append('"');
                    i++;
                } else {
                    entreAspas = !entreAspas;
                }
            } else if (ch == ',' && !entreAspas) {
                campos.add(atual.toString());
                atual.setLength(0);
            } else {
                atual.append(ch);
            }
        }
        campos.add(atual.toString());
        return campos.toArray(new String[0]);
    }
}
