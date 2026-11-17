# A4 — Relatório de documentação e validação

## Objetivo e ambiente

Verificar a cobertura das rotas da A2, o tratamento de erros e as condições para executar os testes. Ambiente observado: OpenJDK 21.0.13, Linux, repositório clonado da branch `main`. O Maven Wrapper está em `api/`. A conexão de saída para GitHub funcionou para clonar o repositório; o acesso ao Maven Central não resolveu o host durante a tentativa de teste.

## Documentação OpenAPI

Foi adicionada a dependência Springdoc OpenAPI e os metadados da API. URLs previstas quando a aplicação iniciar:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

As páginas não puderam ser abertas nesta validação porque a API não chegou a compilar/iniciar.

## Rotas cobertas no código

| Método | Rota | Cenário previsto |
|---|---|---|
| GET | `/api/v1/times` | Lista os clubes da competição cadastrada |
| GET | `/api/v1/jogos` | Lista partidas; permite temporada e limite |
| GET | `/api/v1/jogos/{id}` | Consulta partida ou retorna 404 |
| GET | `/api/v1/jogos/rodada/{numero}` | Consulta rodada; valida número |
| GET | `/api/v1/jogos/ao-vivo` | Filtra status `AO_VIVO` |
| GET | `/api/v1/jogos/{id}/eventos` | Retorna eventos ou lista vazia |
| GET | `/api/v1/classificacao` | Calcula classificação persistida |
| POST | `/api/v1/jogos/{id}/atualizacoes` | Valida e salva placar, minuto, status e evento |

As respostas de exceção das rotas v1 foram centralizadas nos formatos JSON com os campos `status`, `mensagem`, `caminho` e `timestamp`. Erros de estado de recurso usam o status HTTP da exceção (por exemplo, 404); JSON/parâmetros inválidos usam 400; exceções não previstas retornam 500 genérico.

## Resultado observado

| Verificação | Resultado nesta sessão | Evidência |
|---|---|---|
| Versão Java | Disponível | `java -version`: OpenJDK 21.0.13 |
| Build da API | Bloqueado antes da compilação | Falha ao baixar parent Spring Boot 4.1.0; DNS de `repo.maven.apache.org` falhou |
| Build do simulador | Bloqueado antes da compilação | Mesmo parent Maven e mesma falha de DNS |
| Testes unitários/integrados | Não executados | Maven não conseguiu ler o POM |
| Sintaxe JSON da coleção Insomnia | Aprovada | `python -m json.tool insomnia/Insomnia_Resultado_Brasileirao.json` terminou com código 0 |
| Espaços/conflitos do diff | Aprovada | `git diff --check` terminou com código 0 |
| Inicialização da API/H2 | Não verificada | Aplicação não foi iniciada |
| Requisições reais aos oito endpoints | Não executadas | API não estava rodando |
| Comunicação HTTP simulador/API | Implementada no código, não executada | Sem build devido ao mesmo bloqueio de dependências |
| Swagger UI | Configurado no código, não acessado | Aplicação não iniciou |
| Medição de desempenho | Não realizada | Não houve servidor disponível para requisições |

O comando `bash mvnw test -q` foi tentado separadamente em `api/` e `simulador/`. Ambos terminaram antes da compilação com `Non-resolvable parent POM` e `Temporary failure in name resolution`. Nenhum resultado de teste, latência ou resposta HTTP é declarado como sucesso.

## Casos que devem ser repetidos com dependências disponíveis

1. Executar `bash mvnw clean verify` em `api/` e `mvn verify` em `simulador/`.
2. Inicializar a API duas vezes e conferir que times, rodadas, partidas e eventos não duplicam.
3. Fazer GET de cada rota da tabela e guardar status, corpo e tempo de resposta.
4. Enviar atualização válida e inválida (minuto negativo, placar negativo, time não participante, partida inexistente e partida encerrada).
5. Reiniciar a API e confirmar persistência H2.
6. Executar modo de demonstração do simulador contra API iniciada e consultar placar/eventos após o fluxo.
7. Conferir Swagger UI e importar a coleção Insomnia.
8. Validar distribuição 20 times/38 rodadas/380 partidas, 19 mandos de cada lado, 200 finalizadas e 180 agendadas; executar simulação estatística de ratings e registrar os dados gerados.
9. Medir uma amostra repetível de consultas locais antes de afirmar o requisito de até dois segundos da A2.

## Limitações e conclusão

O contrato das oito rotas A2 foi acrescentado, mas sua execução não pôde ser validada. A implementação herdada ainda contém lógica de simulação na aplicação API e código de competições/páginas além do Brasileirão; o módulo `simulador/` atual é um cliente de demonstração HTTP, não a extração integral do simulador original. Também não foi comprovado o calendário inicial exigido nos requisitos finais nem o volume de dados descrito. Assim, A4 está documentada parcialmente: a configuração e os cenários estão preparados, mas a aprovação funcional depende de rodar build, testes e chamadas reais em ambiente com Maven Central disponível e completar as pendências acima.
