# API de Resultados do Brasileirão

Projeto acadêmico de partidas simuladas do Campeonato Brasileiro, baseado no planejamento A1 e na especificação A2. A API usa Java 21, Spring Boot, Spring Data JPA e H2. O projeto de demonstração em `simulador/` é uma aplicação Spring Boot independente que envia atualizações à API por HTTP.

> **Calendário e execução:** o Brasileirão distribui as partidas de cada rodada pelos sete dias, em horários entre 18h30 e 22h30. A API atualiza os jogos enquanto estiver em execução. Ao iniciar, partidas cujo horário já passou e que ainda não terminaram são simuladas até o fim; partidas encerradas não têm o resultado alterado. Em uma base nova, o histórico anterior à rodada atual é criado como encerrado, com Palmeiras 10 pontos à frente do Flamengo e Corinthians 10 pontos atrás do penúltimo colocado. O H2 é criado localmente em `api/data/futebol-portal.mv.db`.

## Estrutura

```text
api/        Aplicação Spring Boot existente (REST, JPA e H2)
simulador/  Aplicação Spring Boot independente para demonstração HTTP
docs/       Relatórios A3/A4
insomnia/   Coleção de requisições
```

## Pré-requisitos e execução

- JDK 21.
- Maven 3.9+ ou Maven Wrapper incluído em `api/`.
- Acesso ao Maven Central na primeira compilação, para baixar as dependências.

No Eclipse, use **File → Import → Maven → Existing Maven Projects** e importe `api/` e `simulador/` separadamente.

Abra dois terminais na raiz do repositório.

1. Inicie a API:

```bash
cd api
bash mvnw spring-boot:run
```

2. Para demonstrar o envio de atualizações por HTTP, em outro terminal, compile e inicie o cliente de demonstração:

```bash
cd simulador
bash mvnw spring-boot:run -Dspring-boot.run.arguments="--simulador.demo=true --simulador.partida-id=0 --api.base-url=http://localhost:8080"
```

A demonstração envia quatro atualizações HTTP (minutos 1, 30, 60 e 90). Com `--simulador.partida-id=0`, o cliente seleciona uma partida agendada; com um ID positivo, usa a partida informada. A API precisa estar disponível. O calendário automático e as partidas ao vivo são processados pela própria API; o cliente separado serve para demonstrar uma atualização externa.

## API e demonstração

Rotas A2:

| Método | Rota | Finalidade |
|---|---|---|
| GET | `/api/v1/times` | Times do Brasileirão configurado |
| GET | `/api/v1/jogos` | Partidas; aceita `temporada` e `limite` |
| GET | `/api/v1/jogos/{id}` | Detalhe da partida |
| GET | `/api/v1/jogos/rodada/{numero}` | Partidas da rodada |
| GET | `/api/v1/jogos/ao-vivo` | Partidas com status `AO_VIVO` |
| GET | `/api/v1/jogos/{id}/eventos` | Eventos de uma partida |
| GET | `/api/v1/classificacao` | Classificação calculada |
| POST | `/api/v1/jogos/{id}/atualizacoes` | Atualização enviada pelo simulador |

Exemplo de atualização:

```http
POST http://localhost:8080/api/v1/jogos/1/atualizacoes
Content-Type: application/json

{
  "minuto": 30,
  "golsMandante": 1,
  "golsVisitante": 0,
  "status": "AO_VIVO",
  "novoEvento": {
    "minuto": 30,
    "tipo": "GOL",
    "descricao": "Gol simulado do mandante"
  }
}
```

Erros tratados pelas rotas v1 usam JSON com `status`, `mensagem`, `caminho` e `timestamp`. Swagger/OpenAPI: `http://localhost:8080/swagger-ui.html`; documento JSON: `http://localhost:8080/v3/api-docs`.

## Banco de dados

O H2 está configurado em arquivo local em `api/data/futebol-portal.mv.db`, com usuário `sa` e senha vazia. Console: `http://localhost:8080/h2-console`; JDBC URL: `jdbc:h2:file:./data/futebol-portal`.

## Insomnia

Importe `insomnia/Insomnia_Resultado_Brasileirao.json` no Insomnia e ajuste `base_url` se a API estiver em outro endereço. A coleção cobre as oito rotas da A2.

## Compilação e testes

```bash
cd api && bash mvnw test
cd ../simulador && bash mvnw test
```

A validação executada nesta sessão está registrada em `docs/A4_RELATORIO_VALIDACAO.md`. A compilação não pôde baixar o parent Maven do Spring Boot devido à falha de resolução de `repo.maven.apache.org` no ambiente.
