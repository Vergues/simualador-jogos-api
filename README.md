# API de Resultados do Brasileirão

Projeto acadêmico de partidas simuladas do Campeonato Brasileiro, baseado no planejamento A1 e na especificação A2. A API usa Java 21, Spring Boot, Spring Data JPA e H2. O projeto de demonstração em `simulador/` é uma aplicação Spring Boot independente que envia atualizações à API por HTTP.

> **Estado desta versão:** a base disponível no GitHub não era o ZIP citado no pedido. O repositório original era uma aplicação monolítica com outras competições e front-end. Nesta revisão, as rotas A2 `/api/v1` e o cliente HTTP de demonstração foram acrescentados e a árvore antiga foi movida para `api/`. Ainda há funções de simulação/calendário no código da API e a temporada não foi convertida para o requisito de 20 rodadas finalizadas + 18 futuras. Leia os relatórios A3/A4 para as limitações verificadas.

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

2. Em outro terminal, compile e inicie o simulador de demonstração:

```bash
cd simulador
bash mvnw spring-boot:run -Dspring-boot.run.arguments="--simulador.demo=true --simulador.partida-id=1 --api.base-url=http://localhost:8080"
```

A demonstração envia quatro atualizações HTTP (minutos 1, 30, 60 e 90). A API precisa estar disponível e a partida informada deve existir e não estar encerrada. O modo demonstração usa força base de 75/70; não substitui a simulação de campeonato pedida na especificação final.

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
