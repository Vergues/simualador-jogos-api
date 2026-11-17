# A3 — Implementação da primeira versão

**Projeto:** API de Resultados de Jogos de Futebol

**Equipe:** Guilherme Gabriel Krull dos Santos, Lucas Ferraz Rezeck, Luiz Rogério Ribeiro de Castro Neto e Matheus Vergueiro Silva.

## Objetivo

Registrar a implementação disponível para a primeira versão e separar o que foi alterado do que ainda precisa ser integrado para cumprir integralmente A1, A2 e os requisitos finais.

## Tecnologias e organização

- Java 21 e Spring Boot 4.1.0 no código da API.
- Spring MVC, Spring Data JPA e H2 em arquivo.
- Um cliente Spring Boot separado em `simulador/`, que usa `RestClient` para enviar atualizações por HTTP.
- A aplicação API está em `api/`; relatórios e coleção Insomnia ficam em `docs/` e `insomnia/`.

A revisão acrescentou as oito rotas definidas na A2 sob `/api/v1`, validação de placar/minuto e identificação do time do evento, persistência da atualização no estado da partida e respostas de erro JSON para as rotas v1. O cliente independente possui modo de demonstração de quatro atualizações.

## Banco e consultas

A aplicação existente usa H2 em arquivo (`jdbc:h2:file:./data/futebol-portal`) e JPA para persistir partidas. Os times e atributos vêm de JSON em `api/src/main/resources/data/`. As consultas A2 expostas são times, partidas, detalhe, rodada, jogos ao vivo, eventos e classificação.

## Requisições

Exemplo de consulta incluído na coleção Insomnia:

```http
GET http://localhost:8080/api/v1/jogos/1
```

O formato de detalhe segue o DTO existente `PartidaResponse`, contendo identificador, campeonato, temporada, rodada, data/hora, status, minuto, mandante, visitante, placar e, quando solicitado, eventos e estatísticas.

Exemplo de atualização que o simulador envia:

```http
POST http://localhost:8080/api/v1/jogos/1/atualizacoes
Content-Type: application/json

{"minuto":30,"golsMandante":1,"golsVisitante":0,"status":"AO_VIVO","novoEvento":{"minuto":30,"tipo":"GOL","descricao":"Gol simulado do mandante"}}
```

O exemplo descreve o contrato implementado; **não foi possível observar uma resposta real** porque a aplicação não pôde ser compilada neste ambiente (dependência Maven não baixada). Portanto, ele não é apresentado como evidência de uma execução bem-sucedida.

## Testes e evidências

O comando `bash mvnw test -q` foi iniciado em `api/` com OpenJDK 21.0.13. O Maven parou ao resolver o parent `org.springframework.boot:spring-boot-starter-parent:4.1.0`: `repo.maven.apache.org` falhou com `Temporary failure in name resolution`. A execução não chegou a compilar ou executar os testes existentes. Não houve chamada HTTP real nem medição de resposta nesta sessão.

## Demonstração ao professor

Com Maven Central disponível: inicie a API com `cd api && bash mvnw spring-boot:run`; depois importe a coleção Insomnia ou consulte o Swagger. O modo HTTP de demonstração pode ser iniciado com `cd simulador && mvn spring-boot:run -Dspring-boot.run.arguments="--simulador.demo=true --simulador.partida-id=1 --api.base-url=http://localhost:8080"`.

## Pendências da A3

A base que estava publicada não era o ZIP anexado no pedido e tinha escopo maior: front-end, múltiplas competições e simulação no mesmo aplicativo. A atualização presente não reimplementa o calendário do estado inicial (20 rodadas concluídas/200 jogos e 180 futuros), nem move toda a lógica de simulação para o módulo independente. A geração inicial, a execução agendada configurada por calendário real e a demonstração ainda precisam de validação integrada. O código deve ser compilado e corrigido em ambiente com acesso às dependências antes de ser apresentado como versão funcional final.
