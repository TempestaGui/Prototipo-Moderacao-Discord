# Comparação Gemini x filtro de palavras-chave (75 mensagens em comum)

## 1. Visão geral

| Métrica | Filtro de palavras-chave | Gemini |
|---|---|---|
| Acurácia (4 classes) | 55/75 = 73.3% | 75/75 = 100.0% |
| Acurácia binária (violação x seguro) | 76.0% | 100.0% |
| Falsos positivos | 2/30 = 6.7% | 0/30 = 0.0% |
| Falsos negativos | 16/45 = 35.6% | 0/45 = 0.0% |
| Tempo (mediana por mensagem) | < 1 ms | 4.2 s |

## 2. Acerto por categoria esperada

| Categoria | Filtro de palavras-chave | Gemini |
|---|---|---|
| SPAM | 14/15 = 93.3% | 15/15 = 100.0% |
| DISCURSO_DE_ODIO | 3/15 = 20.0% | 15/15 = 100.0% |
| ASSEDIO | 10/15 = 66.7% | 15/15 = 100.0% |
| SEGURO | 28/30 = 93.3% | 30/30 = 100.0% |

## 3. Acerto por tipo de mensagem (ironia em destaque)

| Tipo | Filtro de palavras-chave | Gemini |
|---|---|---|
| spam | 14/15 = 93.3% | 15/15 = 100.0% |
| odio | 3/15 = 20.0% | 15/15 = 100.0% |
| assedio | 10/15 = 66.7% | 15/15 = 100.0% |
| **ironia** | 13/15 = 86.7% | 15/15 = 100.0% |
| neutra | 15/15 = 100.0% | 15/15 = 100.0% |

## 4. Ironia/sarcasmo: mensagem a mensagem

| id | Mensagem (esperado: SEGURO) | Filtro | Gemini |
|---|---|---|---|
| 47 | que ajuda incrível você me deu, sensacional mesmo | SEGURO ✓ | SEGURO ✓ |
| 48 | adoro quando o wifi cai bem na hora da partida, melhor sensação do mundo | SEGURO ✓ | SEGURO ✓ |
| 49 | claro, segunda-feira às 7h da manhã é meu horário preferido | SEGURO ✓ | SEGURO ✓ |
| 50 | uau, mais uma reunião que podia ser um e-mail, que alegria | SEGURO ✓ | SEGURO ✓ |
| 51 | esse jogo é tão bem otimizado que roda a 5 fps, parabéns aos desenvolvedores | SEGURO ✓ | SEGURO ✓ |
| 52 | vou matar esse cachorro, ele comeu meu sapato de novo | ASSEDIO ✗ | SEGURO ✓ |
| 53 | se eu ganhasse um real por cada vez que o servidor cai, já estaria rico | SEGURO ✓ | SEGURO ✓ |
| 54 | cara, você é um gênio mesmo, esqueceu a senha pela quinta vez | SEGURO ✓ | SEGURO ✓ |
| 55 | me mata de rir esse vídeo, tô morrendo aqui kkkk | SEGURO ✓ | SEGURO ✓ |
| 56 | essa fila tá tão grande que vou morrer de velhice antes de ser atendido | SEGURO ✓ | SEGURO ✓ |
| 57 | amigo, te odeio, você ganhou de novo, para de ser bom nisso kkk | ASSEDIO ✗ | SEGURO ✓ |
| 58 | que bela organização, só faltou o evento acontecer | SEGURO ✓ | SEGURO ✓ |
| 59 | mano, esse café tá tão fraco que dá pra ver o fundo da xícara, uma delícia | SEGURO ✓ | SEGURO ✓ |
| 60 | vou enlouquecer com a quantidade de tarefa que esse professor passou, que maravilha | SEGURO ✓ | SEGURO ✓ |
| 46 | ótimo, o notebook resolveu atualizar bem na hora da apresentação, adoro | SEGURO ✓ | SEGURO ✓ |

## 5. Todas as divergências entre os métodos

| id | tipo | Mensagem | Esperado | Filtro | Gemini |
|---|---|---|---|---|---|
| 15 | spam | Divulgando meu canal do YouTube, se inscreve lá, se inscreve, se inscreve, valeu | SPAM | SEGURO | SPAM |
| 16 | odio | Esses imigrantes só vêm pra cá pra roubar, deviam ser todos deportados | DISCURSO_DE_ODIO | SEGURO | DISCURSO_DE_ODIO |
| 17 | odio | Mulher não tem capacidade de liderar nada, o lugar delas é na cozinha | DISCURSO_DE_ODIO | SEGURO | DISCURSO_DE_ODIO |
| 20 | odio | Homossexuais são doentes e deveriam ser trancados num hospício | DISCURSO_DE_ODIO | SEGURO | DISCURSO_DE_ODIO |
| 21 | odio | Nordestino é tudo preguiçoso e burro, não sei por que ainda deixam votar | DISCURSO_DE_ODIO | ASSEDIO | DISCURSO_DE_ODIO |
| 22 | odio | Judeus controlam todo o dinheiro do mundo, são uma praga | DISCURSO_DE_ODIO | SEGURO | DISCURSO_DE_ODIO |
| 23 | odio | Gordos deviam ter vergonha de existir, são um lixo para a sociedade | DISCURSO_DE_ODIO | ASSEDIO | DISCURSO_DE_ODIO |
| 24 | odio | Trans não existem, é tudo doente mental, não merecem respeito | DISCURSO_DE_ODIO | SEGURO | DISCURSO_DE_ODIO |
| 25 | odio | Deficientes só atrapalham a sociedade, deviam ficar escondidos em casa | DISCURSO_DE_ODIO | SEGURO | DISCURSO_DE_ODIO |
| 26 | odio | Indígenas são atrasados e só atrapalham o progresso do país | DISCURSO_DE_ODIO | SEGURO | DISCURSO_DE_ODIO |
| 27 | odio | Esses muçulmanos tinham que ser todos expulsos do país, não prestam | DISCURSO_DE_ODIO | SEGURO | DISCURSO_DE_ODIO |
| 29 | odio | Asiáticos são todos iguais e trouxeram essa doença pro mundo, voltem pra China | DISCURSO_DE_ODIO | SEGURO | DISCURSO_DE_ODIO |
| 30 | odio | Velho não serve pra mais nada, devia sumir logo pra não pesar na sociedade | DISCURSO_DE_ODIO | SEGURO | DISCURSO_DE_ODIO |
| 35 | assedio | já falei que vou postar suas fotos pra todo mundo rir de você | ASSEDIO | SEGURO | ASSEDIO |
| 38 | assedio | vou descobrir onde você mora e aparecer lá, aguarda | ASSEDIO | SEGURO | ASSEDIO |
| 39 | assedio | toda vez que você entra eu vou te zoar até você chorar, aproveita | ASSEDIO | SEGURO | ASSEDIO |
| 42 | assedio | ei @fulano, responde, responde, responde, vou continuar te marcando até você ceder | ASSEDIO | SEGURO | ASSEDIO |
| 45 | assedio | Você é ridículo, todo mundo ri de você pelas costas | ASSEDIO | SEGURO | ASSEDIO |
| 52 | ironia | vou matar esse cachorro, ele comeu meu sapato de novo | SEGURO | ASSEDIO | SEGURO |
| 57 | ironia | amigo, te odeio, você ganhou de novo, para de ser bom nisso kkk | SEGURO | ASSEDIO | SEGURO |
