""""
alcula as métricas do agente a partir de resultados/resultados-gemini.csv.

"""
import argparse
import csv
import statistics
import sys
from collections import Counter, defaultdict

SEGURO = "SEGURO"
CATEGORIAS = ["SPAM", "DISCURSO_DE_ODIO", "ASSEDIO", "SEGURO"]


def pct(parte, total):
    return f"{100 * parte / total:.1f}%" if total else "n/a"


def carregar(caminho):
    with open(caminho, encoding="utf-8", newline="") as f:
        return list(csv.DictReader(f))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("arquivo", nargs="?", default="resultados/resultados-gemini.csv")
    ap.add_argument("--ignorar-tempo-acima", type=int, default=None, metavar="MS",
                    help="descarta do cálculo de tempo linhas acima de MS (ex.: espera do rate limiter)")
    args = ap.parse_args()

    linhas = carregar(args.arquivo)
    ok = [l for l in linhas if l["status"] == "OK"]
    falhas = [l for l in linhas if l["status"] != "OK"]

    if not ok:
        sys.exit("Nenhuma linha com status OK.")

    # --- Acurácia geral (4 classes, correspondência exata) ---
    acertos = [l for l in ok if l["categoria_obtida"] == l["categoria_esperada"]]

    # --- FP / FN (visão binária: violação x seguro) ---
    seguros = [l for l in ok if l["categoria_esperada"] == SEGURO]
    violacoes = [l for l in ok if l["categoria_esperada"] != SEGURO]
    fps = [l for l in seguros if l["categoria_obtida"] != SEGURO]
    fns = [l for l in violacoes if l["categoria_obtida"] == SEGURO]
    acertos_binarios = len(ok) - len(fps) - len(fns)

    print("=" * 60)
    print(f"Arquivo: {args.arquivo}")
    print(f"Linhas: {len(linhas)} | OK: {len(ok)} | FALHA (excluídas): {len(falhas)}")
    print("=" * 60)

    print("\n[Acurácia geral] (4 classes, exata)")
    print(f"  {len(acertos)}/{len(ok)} = {pct(len(acertos), len(ok))}")
    print("[Acurácia binária] (violação x seguro, ignora troca entre tipos de violação)")
    print(f"  {acertos_binarios}/{len(ok)} = {pct(acertos_binarios, len(ok))}")

    print("\n[Falsos positivos] esperado SEGURO, classificado como violação")
    print(f"  {len(fps)}/{len(seguros)} = {pct(len(fps), len(seguros))}")
    print("[Falsos negativos] esperado violação, classificado como SEGURO")
    print(f"  {len(fns)}/{len(violacoes)} = {pct(len(fns), len(violacoes))}")

    # --- Por categoria esperada ---
    print("\n[Acerto por categoria esperada]")
    for cat in CATEGORIAS:
        grupo = [l for l in ok if l["categoria_esperada"] == cat]
        if grupo:
            a = sum(1 for l in grupo if l["categoria_obtida"] == cat)
            print(f"  {cat:<18} {a}/{len(grupo)} = {pct(a, len(grupo))}")

    # --- Por tipo (ironia x neutra etc.) ---
    print("\n[Acerto por tipo de mensagem]")
    por_tipo = defaultdict(list)
    for l in ok:
        por_tipo[l["tipo"]].append(l)
    for tipo, grupo in por_tipo.items():
        a = sum(1 for l in grupo if l["categoria_obtida"] == l["categoria_esperada"])
        print(f"  {tipo:<10} {a}/{len(grupo)} = {pct(a, len(grupo))}")

    # --- Matriz de confusão ---
    print("\n[Matriz de confusão] (linhas = esperado, colunas = obtido)")
    conf = Counter((l["categoria_esperada"], l["categoria_obtida"]) for l in ok)
    print("  " + " " * 18 + "".join(f"{c[:10]:>12}" for c in CATEGORIAS))
    for esp in CATEGORIAS:
        print(f"  {esp:<18}" + "".join(f"{conf[(esp, obt)]:>12}" for obt in CATEGORIAS))

    # --- Tempo ---
    tempos = [int(l["tempo_ms"]) for l in ok if l["tempo_ms"]]
    if args.ignorar_tempo_acima is not None:
        antes = len(tempos)
        tempos = [t for t in tempos if t <= args.ignorar_tempo_acima]
        print(f"\n[Tempo de resposta] (descartadas {antes - len(tempos)} linhas > "
              f"{args.ignorar_tempo_acima} ms)")
    else:
        print("\n[Tempo de resposta]")
    if tempos:
        print(f"  média   : {statistics.mean(tempos):.0f} ms")
        print(f"  mediana : {statistics.median(tempos):.0f} ms")
        print(f"  mín/máx : {min(tempos)} / {max(tempos)} ms")

    # --- Erros para a análise qualitativa ---
    erros = [l for l in ok if l["categoria_obtida"] != l["categoria_esperada"]]
    print(f"\n[Erros] {len(erros)} mensagens classificadas diferente do gabarito")
    for l in erros:
        print(f"  #{l['id']} esperado={l['categoria_esperada']} obtido={l['categoria_obtida']} "
              f"conf={l['confianca']} | {l['mensagem']}")


if __name__ == "__main__":
    main()