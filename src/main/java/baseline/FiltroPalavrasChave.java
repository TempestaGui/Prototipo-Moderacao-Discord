package baseline;

import com.prototipoBotDiscor.prototipoDiscord.gemini.Classificacao;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

public class FiltroPalavrasChave {

    private static final Map<Classificacao, Pattern> REGRAS = new LinkedHashMap<>();

    static {
        REGRAS.put(Classificacao.SPAM, compilar(
                List.of("clique aqui", "clique no link", "link na bio", "ganhe", "gratis",
                        "promocao", "oferta", "sorteio", "seguidores", "curtidas",
                        "lucro garantido", "chama no privado", "chama no pv", "chama no zap",
                        "emprestimo", "aposta", "pix"),
                "https?://", "www\\.", "\\b[a-z0-9-]+\\.(com|net|org|br)\\b",
                "bit\\.ly", "t\\.me", "discord\\.gg"));

        REGRAS.put(Classificacao.DISCURSO_DE_ODIO, compilar(
                List.of("macaco", "crioulo", "viado", "bicha", "sapatao", "retardado",
                        "mongoloide", "aleijado", "nazista", "terrorista", "inferior", "raca")));

        REGRAS.put(Classificacao.ASSEDIO, compilar(
                List.of("idiota", "otario", "imbecil", "babaca", "burro", "inutil", "patetico",
                        "fracassado", "lixo", "merda", "fuder", "foder", "porra", "caralho",
                        "cala a boca", "some daqui", "desinstala", "vou te matar",
                        "vou matar", "odeio", "feio", "gordo")));
    }

    public static Pattern compilar(List<String> termos, String... regexExtras){
        List<String> partes = new ArrayList<>();
        for (String termo: termos){
            partes.add("\\b" + Pattern.quote(termo));
        }
        partes.addAll(Arrays.asList(regexExtras));
        return Pattern.compile(String.join("|", partes));
    }

    public Classificacao classificar(String mensagem){
        String texto = normalizar(mensagem);
        for(Map.Entry<Classificacao, Pattern> regra: REGRAS.entrySet()){
            if(regra.getValue().matcher(texto).find()){
                return regra.getKey();
            }
        }
        return Classificacao.SEGURO;
    }

    private static String normalizar(String mensagem){
        return Normalizer.normalize(mensagem, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
