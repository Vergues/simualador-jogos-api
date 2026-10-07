# Linha de Fundo

Portal acadêmico de futebol: resultados, jogos ao vivo, calendário, classificação, artilharia e desempenho dos clubes. Interface de consulta em HTML/CSS/JavaScript, sem frameworks.

## Executar

Java 21 e `JAVA_HOME` configurado para o JDK 21. O Maven Wrapper está incluído.

```powershell
.\mvnw.cmd spring-boot:run
```

Abra http://localhost:8080. Para testar e gerar o JAR:

```powershell
.\mvnw.cmd clean verify
java -jar target/simulador-jogos-api-0.0.1-SNAPSHOT.jar
```

No Linux/macOS, use `./mvnw`. Tecnologias: Java 21, Spring Boot 4.1, Spring MVC, JPA e H2.

## Páginas

- `/`: jogos de hoje, próximos jogos, últimos resultados, classificação e artilheiros.
- `/jogos`: filtros por competição, temporada, rodada, fase, clube, data e status; histórico completo.
- `/competicoes` e `/competicoes/{id}`: formatos, jogos, tabelas e fases.
- `/classificacao`, `/artilharia` e `/calendario`: tabelas e calendário mensal geral.
- `/times` e `/times/palmeiras`: elenco, desempenho, posição e jogos do clube.
- `/partidas/{id}`: placar, eventos, estatísticas, cartões e situação dos jogadores. O endereço antigo `/partida.html?id=...` continua funcionando.

## Competições e calendário

Regras simplificadas para apresentação acadêmica, sem pretensão de reproduzir os regulamentos oficiais:

| Competição | Formato |
|---|---|
| Brasileirão (`brasileirao`) | 20 clubes, turno e returno, 38 rodadas e 380 jogos, a partir de abril. |
| Paulista (`paulista`) | 16 clubes, quatro grupos de quatro, turno e returno; dois por grupo avançam às quartas, semifinal e final únicas. Janeiro–março, 55 jogos. |
| Copa do Brasil (`copa-do-brasil`) | 40 clubes; 16 disputam preliminar e 24 entram nos dezesseis avos. Ida e volta até a semifinal; final única. 77 jogos. |
| Libertadores (`libertadores`) | 32 clubes de nove países, oito grupos de quatro, turno e returno; dois por grupo passam às oitavas. Ida e volta até a semifinal; final única. 125 jogos. |

O calendário evita jogos do mesmo clube com menos de 48 horas de intervalo. Datas-base ficam em `src/main/resources/data/competicoes.json`; conflitos deslocam partidas para o próximo dia disponível. As fases eliminatórias são geradas somente após definição dos classificados. Empates no agregado são decididos por pênaltis persistidos; não há gol qualificado fora de casa. Uma temporada completa no formato ampliado tem 637 jogos. Calendários iniciados permanecem com seus participantes e resultados; nesta instalação, a expansão completa está disponível em 2027. Escolha essa temporada no topo.

## API

Todos os endpoints de consulta usam GET:

| Endpoint | Consulta |
|---|---|
| `/api/portal?temporada=2026` | Resumo da página inicial |
| `/api/competicoes?temporada=2026` | Formatos, clubes, fases e campeões |
| `/api/jogos` | Filtros `temporada`, `competicao`, `rodada`, `fase`, `time`, `data`, `status`, `limite` |
| `/api/jogos/{id}` | Detalhe de uma partida |
| `/api/classificacao?competicao=brasileirao` | Liga ou grupos; o mata-mata aparece pelos confrontos |
| `/api/artilharia` | Artilharia geral; filtro opcional `competicao` e `temporada` |
| `/api/times` e `/api/times/{slug}` | Clubes e desempenho na temporada |

Datas usam `AAAA-MM-DD`. Status: `proximos`, `andamento`, `encerrados`. Em `/api/jogos`, `temporada=0` consulta todo o histórico; limite de 1 a 1500. Anos: 2000 a 2100. Respostas de erro têm `status` e `erro`.

Os endpoints antigos `/api/brasileirao/...` foram preservados. Operações administrativas continuam disponíveis via POST, sem botões no portal: `/api/competicoes/calendario?ano=2026`, `/api/brasileirao/gerar-calendario?ano=2026`, `/api/brasileirao/jogo/{id}/simular` e `/api/jogos/simular?casa=PAL&fora=COR`. São recursos locais de demonstração, sem autenticação.

## Simulação e persistência

Ao iniciar, a aplicação prepara as quatro competições do ano atual e do próximo e atualiza os jogos cuja data já chegou. Um agendamento acompanha o relógio: partidas antigas encerram automaticamente; partidas atuais avançam minuto a minuto. Resultados, eventos, escalações atuais e decisões eliminatórias ficam no H2 `data/futebol-portal.mv.db`, ignorado pelo Git. Reiniciar não sorteia novamente resultados existentes.

Esta versão usa um schema novo de desenvolvimento. O antigo `data/simulador.mv.db` não é lido. Estatísticas acumuladas são derivadas dos resultados e eventos, sem tabelas extras: pontos, jogos, vitórias, empates, derrotas, gols pró/contra, saldo, aproveitamento, gols por jogador e cartões. Jogadores são identificados pelo clube e índice no elenco. A interface acompanha partidas automaticamente e para de consultar detalhes ao encerrar.

## Estrutura

12 arquivos Java principais: aplicação/configuração, dois controllers, quatro services (simulação, campeonato, calendário e consultas), um repository, três modelos e uma resposta de partida. Frontend: `index.html`, `style.css` e `app.js`. Dois arquivos de testes com 13 testes para calendário, persistência, encerramento, substituições, homônimos, duplicidade, grupos, eliminatórias, estatísticas, preservação de temporadas e regressão das forças.

## Força dos clubes

A força determina a distribuição das jogadas ofensivas, incluindo finalizações e gols: chance do mandante = 53 + força efetiva do mandante − força efetiva do visitante, limitada entre 10% e 90%. O mando dá três pontos percentuais; cada expulso reduz a força efetiva em oito. A diferença de força também influencia a posse. O minuto e a seed persistida sorteiam os eventos; resultados não são fixos. Os atributos dos jogadores pesam na escolha dos titulares e dos protagonistas dos eventos.

Essa regra foi comparada com `EventoService` no commit `32a74e2` e preservada. Palmeiras passou de 95 a 96, Flamengo de 90 a 92; Corinthians permanece em 50. Os demais clubes mantêm as forças dos JSONs.

Teste regressivo: 2.000 partidas por clube, mesmos dez adversários, dois mandos e seeds controladas. Foram avaliadas médias, não posições em uma única temporada:

| Clube | Força | Pontos/jogo | Vitórias | Gols/jogo | Finalizações/jogo |
|---|---:|---:|---:|---:|---:|
| Palmeiras | 96 | 1,782 | 48,35% | 1,001 | 8,166 |
| Flamengo | 92 | 1,687 | 44,80% | 0,945 | 7,687 |
| Santos | 80 | 1,363 | 33,70% | 0,760 | 6,232 |
| Goiás | 62 | 0,885 | 18,50% | 0,477 | 4,008 |
| Corinthians | 50 | 0,621 | 10,75% | 0,307 | 2,565 |

Outro teste isola a força usando o mesmo elenco: em 2.000 jogos, força 96 venceu força 50 em 69,55%; empates em 26,50% e zebras em 3,95%. Os testes exigem diferenças relevantes, mas preservam empates e vitórias dos fracos.

## Clubes e escudos

42 clubes novos foram integrados do pacote fornecido: 20 brasileiros e 22 estrangeiros, totalizando 62. Seus elencos são fictícios para simulação. Os elencos existentes foram mantidos para preservar a identidade dos jogadores nos eventos salvos; apenas as forças de Palmeiras e Flamengo foram atualizadas. O Brasileirão continua com 20 clubes. Paulista, Copa do Brasil e Libertadores usam os participantes definidos em `data/competicoes.json`.

Os 62 escudos SVG locais estilizados estão em `static/img/times/`. A associação por sigla vem de `escudos-manifest.json`; são imagens fornecidas no pacote, não escudos oficiais. Nenhum escudo é armazenado no H2. A interface mostra siglas quando falta imagem ou seu carregamento falha.

Para adicionar outro clube, inclua seu JSON em `data/`, registre o arquivo em `times-index.json` e sua sigla nas competições desejadas. Reutilize o formato existente:

```json
{
  "nome": "Nome do clube", "sigla": "ABC", "forca": 80, "pais": "País",
  "jogadores": [
    {"nome": "Nome do jogador", "posicao": "GOL", "overall": 80,
     "ataque": 40, "defesa": 85, "finalizacao": 30, "fisico": 75}
  ]
}
```

O exemplo mostra somente a estrutura: um elenco válido tem ao menos 11 jogadores e um goleiro. `pais`, `slug` e `escudo` são opcionais; posições: GOL, ZAG, LD, LE, VOL, MC, MEI, PD, PE, SA e ATA. Não inclua atributos sem uso como passe, velocidade e regraEspecial.
