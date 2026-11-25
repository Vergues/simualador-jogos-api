# A4 — Documentação e testes

## Documentação OpenAPI

Springdoc/OpenAPI está configurado no projeto. A interface Swagger UI abriu localmente em `http://localhost:8080/swagger-ui.html`; o documento OpenAPI está disponível em `http://localhost:8080/v3/api-docs`.

## Rotas verificadas

| Método e rota | Resultado observado |
|---|---|
| GET `/api/v1/times` | Sucesso pelo Swagger |
| GET `/api/v1/jogos?temporada=2026&limite=3` | Retornou três partidas agendadas |
| GET `/api/v1/jogos/{id}` | Consulta por ID retornou a partida solicitada |
| GET `/api/v1/jogos/rodada/34?temporada=2026` | Consulta da rodada funcionou; partidas e horários foram conferidos |
| GET `/api/v1/jogos/ao-vivo` | Sucesso; lista vazia quando não havia partida ao vivo |
| GET `/api/v1/jogos/{id}/eventos` | Sucesso para uma partida consultada |
| GET `/api/v1/classificacao` | HTTP 200; Palmeiras 50 pontos, Flamengo 40, Vasco 26 e Corinthians 16 |
| GET `/api/v1/jogos/999999` | Retornou o erro 404 esperado |

A atualização externa de partida e a validação de erros também são cobertas pelos testes de serviço automatizados. A rota POST ainda não foi exercitada manualmente pela interface HTTP.

## Calendário, histórico e persistência

Em uma base nova, o histórico anterior à rodada atual fica encerrado e não é alterado pela simulação automática. Os próximos jogos são distribuídos ao longo dos sete dias entre 18h30 e 22h30. A API foi iniciada localmente, conectou ao H2 e respondeu na porta 8080. A conclusão instantânea de partidas vencidas durante uma parada e a persistência após reinício ainda precisam de uma verificação separada.

## Testes automatizados

| Módulo | Comando | Resultado |
|---|---|---|
| API | `mvnw.cmd test` | 11 testes, 0 falhas, 0 erros; BUILD SUCCESS |
| Cliente `simulador` | `mvnw.cmd test` | BUILD SUCCESS; o módulo não possui testes próprios |

## Medição de resposta local

Cinco chamadas a `GET /api/v1/classificacao` retornaram HTTP 200. A primeira levou 0,488913 s; as quatro seguintes levaram 0,009511 s, 0,009350 s, 0,008751 s e 0,008855 s. A média das quatro chamadas subsequentes foi aproximadamente 0,00912 s (9,12 ms). É uma amostra de uma execução local, sem uma medição anterior comparável; portanto, ela registra o tempo observado, mas não comprova uma melhoria percentual de desempenho.

## Conclusão

Swagger, consultas principais, classificação, tratamento de 404, inicialização com H2 e testes automatizados foram verificados. Permanecem pendentes o POST manual, o teste de persistência após reinício, a conclusão de um jogo durante a parada da aplicação e a comparação de desempenho antes/depois. Não declarar esses itens como aprovados sem executar as verificações correspondentes.
