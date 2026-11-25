# A4 — Documentação e testes

## Escopo

Esta revisão cobre a configuração OpenAPI, o tratamento de erros, os testes existentes e a rotina de calendário/simulação. A validação funcional completa depende de executar a API e as requisições em um ambiente com as dependências Maven disponíveis.

## Documentação OpenAPI

A dependência Springdoc está configurada no projeto. URLs previstas quando a aplicação iniciar:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Documento JSON: `http://localhost:8080/v3/api-docs`

Essas páginas ainda não foram verificadas nesta revisão porque a aplicação não chegou a compilar.

## Rotas documentadas

| Método | Rota | Cenário previsto |
|---|---|---|
| GET | `/api/v1/times` | Lista os clubes |
| GET | `/api/v1/jogos` | Lista partidas; aceita temporada e limite |
| GET | `/api/v1/jogos/{id}` | Consulta partida ou retorna 404 |
| GET | `/api/v1/jogos/rodada/{numero}` | Consulta uma rodada |
| GET | `/api/v1/jogos/ao-vivo` | Lista partidas em andamento |
| GET | `/api/v1/jogos/{id}/eventos` | Lista eventos da partida |
| GET | `/api/v1/classificacao` | Calcula a classificação da temporada |
| POST | `/api/v1/jogos/{id}/atualizacoes` | Recebe placar, minuto, status e evento |

Os erros das rotas v1 são centralizados em JSON com `status`, `mensagem`, `caminho` e `timestamp`. A coleção Insomnia em `insomnia/Insomnia_Resultado_Brasileirao.json` contém requisições para as rotas; elas ainda precisam ser executadas contra a API.

## Tratamento da temporada e das partidas

Em uma base nova ou ainda sem resultados, a inicialização encerra as partidas históricas com resultados determinísticos e prepara a classificação solicitada. Os jogos futuros são distribuídos ao longo da semana entre 18h30 e 22h30. A API atualiza partidas a cada cinco segundos enquanto está aberta; partidas que venceram durante o desligamento são concluídas ao iniciar. Partidas encerradas não são reabertas nem têm placar alterado.

Foi adicionado um teste de inicialização que verifica os pontos, as posições solicitadas, o encerramento do histórico e os horários da rodada atual. Os testes de serviço já existentes permanecem no projeto.

## Resultado da execução nesta revisão

| Verificação | Resultado | Observação |
|---|---|---|
| Java 21 | Configurado no projeto | Requisito indicado no `pom.xml` |
| `bash mvnw test` na API | Bloqueado antes da compilação | DNS não resolveu `repo.maven.apache.org`; o parent Spring Boot 4.1.0 não foi baixado |
| Teste de inicialização novo | Não executado | Maven parou antes de compilar os testes |
| Chamadas reais às oito rotas | Pendentes | Requerem a API iniciada; usar Swagger ou Insomnia |
| Swagger UI/OpenAPI JSON | Pendentes | Aplicação não iniciou nesta revisão |
| Persistência H2 após reinício | Pendente | Verificar depois de executar localmente |
| Medição de desempenho | Pendente | Não há tempos medidos; não declarar requisito de desempenho aprovado |
| Demonstração do cliente HTTP | Pendente | Requer API em execução e dependências baixadas |

## Próximas verificações

1. Executar `bash mvnw test` em `api/` e `simulador/` com acesso ao Maven Central.
2. Iniciar a API e consultar as oito rotas usando Swagger ou a coleção Insomnia.
3. Conferir a tabela inicial: Palmeiras em primeiro, dez pontos à frente do Flamengo; Corinthians em último, dez pontos atrás do Vasco.
4. Confirmar horários dos jogos agendados, partida ao vivo e conclusão instantânea de partidas vencidas durante o desligamento.
5. Reiniciar a API e confirmar que partidas encerradas e dados H2 persistem.
6. Medir e registrar tempos de resposta antes de afirmar melhorias de desempenho.

**Conclusão atual:** documentação e tratamento de erros estão configurados e os testes/cenários estão preparados. A aprovação funcional da A4 continua pendente da execução local e do registro das evidências acima.
