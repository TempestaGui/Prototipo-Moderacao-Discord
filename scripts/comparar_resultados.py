"""""
Gera resultados/comparacao.md
e imprime o mesmo conteúdo no terminal.
"""
import argparse
import csv
import statistics
from collections import OrderedDict

SEGURO = "SEGURO"
CATEGORIAS = ["SPAM", "DISCURSO_DE_ODIO", "ASSEDIO", "SEGURO"]


def carregar(caminho):
    """Lê o CSV, mantém só a última tentativa de cada id e só status OK."""
    with open(caminho, encoding="utf-8", newline="") as f:
        ultimas = OrderedDict((l["id"], l) for l in csv.DictReader(f))
    return {i: l for i, l in ultimas.items() if l["status"] == "OK"}


def pct(a, b):
    return f"{100 * a / b:.1f}%" if b else "n/a"


def metricas(linhas):
    n = len(linhas)
    seguros = [l for l in linhas if l["categoria_esperada"] == SEGURO]
    viols = [l for l in linhas if l["categoria_esperada"] != SEGURO]
    acertos = sum(l["categoria_obtida"] == l["categoria_esperada"] for l in linhas)
    fps = sum(l["categoria_obtida"] != SEGURO for l in seguros)
    fns = sum(l["categoria_obtida"] == SEGURO for l in viols)
    tempos = [int(l["tempo_ms"]) for l in linhas if l["tempo_ms"]]
    return {
        "n": n, "acertos": acertos, "binario": n - fps - fns,
        "fp": fps, "n_seg": len(seguros), "fn": fns, "n_viol": len(viols),
        "mediana": statistics.median(tempos) if tempos else None,
    }


def tabela(cabecalho, linhas):
    out = ["| " + " | ".join(cabecalho) + " |",
           "|" + "|".join(["---"] * len(cabecalho)) + "|"]
    out += ["| " + " | ".join(str(c) for c in l) + " |" for l in linhas]
    return "\n".join(out)


def fmt_tempo(ms):
    if ms is None:
        return "n/a"
    return "< 1 ms" if ms < 1 else f"{ms / 1000:.1f} s"


def acerto_grupo(linhas, chave, valor):
    g = [l for l in linhas if l[chave] == valor]
    a = sum(l["categoria_obtida"] == l["categoria_esperada"] for l in g)
    return a, len(g)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--gemini", default="resultados/resultados-gemini.csv")
    ap.add_argument("--baseline", default="resultados/resultados-baseline.csv")
    ap.add_argument("--saida", default="resultados/comparacao.md")
    args = ap.parse_args()

    gem, bas = carregar(args.gemini), carregar(args.baseline)
    ids = [i for i in gem if i in bas]  # só mensagens avaliadas pelos dois métodos
    G = [gem[i] for i in ids]
    B = [bas[i] for i in ids]
    mg, mb = metricas(G), metricas(B)

    md = [f"# Comparação Gemini x filtro de palavras-chave ({len(ids)} mensagens em comum)\n"]

    md.append("## 1. Visão geral\n")
    md.append(tabela(["Métrica", "Filtro de palavras-chave", "Gemini"], [
        ["Acurácia (4 classes)", f"{mb['acertos']}/{mb['n']} = {pct(mb['acertos'], mb['n'])}",
         f"{mg['acertos']}/{mg['n']} = {pct(mg['acertos'], mg['n'])}"],
        ["Acurácia binária (violação x seguro)", pct(mb["binario"], mb["n"]),
         pct(mg["binario"], mg["n"])],
        ["Falsos positivos", f"{mb['fp']}/{mb['n_seg']} = {pct(mb['fp'], mb['n_seg'])}",
         f"{mg['fp']}/{mg['n_seg']} = {pct(mg['fp'], mg['n_seg'])}"],
        ["Falsos negativos", f"{mb['fn']}/{mb['n_viol']} = {pct(mb['fn'], mb['n_viol'])}",
         f"{mg['fn']}/{mg['n_viol']} = {pct(mg['fn'], mg['n_viol'])}"],
        ["Tempo (mediana por mensagem)", fmt_tempo(mb["mediana"]), fmt_tempo(mg["mediana"])],
    ]))

    md.append("\n## 2. Acerto por categoria esperada\n")
    linhas = []
    for cat in CATEGORIAS:
        ab, nb = acerto_grupo(B, "categoria_esperada", cat)
        ag, ng = acerto_grupo(G, "categoria_esperada", cat)
        linhas.append([cat, f"{ab}/{nb} = {pct(ab, nb)}", f"{ag}/{ng} = {pct(ag, ng)}"])
    md.append(tabela(["Categoria", "Filtro de palavras-chave", "Gemini"], linhas))

    md.append("\n## 3. Acerto por tipo de mensagem (ironia em destaque)\n")
    tipos = list(OrderedDict.fromkeys(l["tipo"] for l in G))
    linhas = []
    for t in tipos:
        ab, nb = acerto_grupo(B, "tipo", t)
        ag, ng = acerto_grupo(G, "tipo", t)
        nome = f"**{t}**" if t == "ironia" else t
        linhas.append([nome, f"{ab}/{nb} = {pct(ab, nb)}", f"{ag}/{ng} = {pct(ag, ng)}"])
    md.append(tabela(["Tipo", "Filtro de palavras-chave", "Gemini"], linhas))

    md.append("\n## 4. Ironia/sarcasmo: mensagem a mensagem\n")
    linhas = []
    for i in ids:
        if gem[i]["tipo"] != "ironia":
            continue
        ok_b = "✓" if bas[i]["categoria_obtida"] == bas[i]["categoria_esperada"] else "✗"
        ok_g = "✓" if gem[i]["categoria_obtida"] == gem[i]["categoria_esperada"] else "✗"
        linhas.append([i, gem[i]["mensagem"], f"{bas[i]['categoria_obtida']} {ok_b}",
                       f"{gem[i]['categoria_obtida']} {ok_g}"])
    md.append(tabela(["id", "Mensagem (esperado: SEGURO)", "Filtro", "Gemini"], linhas))

    md.append("\n## 5. Todas as divergências entre os métodos\n")
    linhas = []
    for i in ids:
        g, b = gem[i], bas[i]
        if g["categoria_obtida"] != b["categoria_obtida"]:
            linhas.append([i, g["tipo"], g["mensagem"], g["categoria_esperada"],
                           b["categoria_obtida"], g["categoria_obtida"]])
    if linhas:
        md.append(tabela(["id", "tipo", "Mensagem", "Esperado", "Filtro", "Gemini"], linhas))
    else:
        md.append("Nenhuma divergência.")

    texto = "\n".join(md) + "\n"
    with open(args.saida, "w", encoding="utf-8") as f:
        f.write(texto)
    print(texto)
    print(f"(salvo em {args.saida})")


if __name__ == "__main__":
    main()