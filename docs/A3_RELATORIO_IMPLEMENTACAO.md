# A3 — Implementação da primeira versão

**Projeto:** API de Resultados de Jogos de Futebol

**Equipe:** Guilherme Gabriel Krull dos Santos, Lucas Ferraz Rezeck, Luiz Rogério Ribeiro de Castro Neto e Matheus Vergueiro Silva.

## Implementação disponível no repositório

- API REST em Java 21 e Spring Boot, com Spring Data JPA e banco H2 em arquivo.
- Oito rotas A2 sob `/api/v1`: times, jogos, detalhe, rodada, partidas ao vivo, eventos, classificação e atualização de partida.
- Calendário de 20 clubes, 38 rodadas, 380 partidas e turno/returno.
- A classificação é calculada a partir dos jogos encerrados.
- Uma base nova encerra o histórico anterior à rodada atual e preserva esses resultados. O histórico inicial é preparado para deixar o Palmeiras dez pontos à frente do Flamengo e o Corinthians dez pontos atrás do Vasco; o Palmeiras também tem derrota registrada.
- A partir da rodada atual, as partidas são distribuídas ao longo dos sete dias, entre 18h30 e 22h30. A API mantém a simulação automática enquanto estiver em execução.
- Partidas agendadas cujo horário passou durante o desligamento são concluídas ao iniciar a API. Resultados já encerrados não são alterados pela simulação automática nem pela rota de atualização.

## Banco de dados

O arquivo H2 não é versionado no GitHub. A aplicação o cria localmente em `api/data/futebol-portal.mv.db` quando é executada. O esquema JPA está configurado para atualização. Em bases existentes, partidas encerradas são preservadas; partidas futuras ainda agendadas podem receber novos horários.

## Demonstração

Com a API em execução, o Swagger está disponível em `http://localhost:8080/swagger-ui.html` e a coleção Insomnia está em `insomnia/Insomnia_Resultado_Brasileirao.json`. O módulo `simulador/` é um cliente HTTP de demonstração que seleciona uma partida agendada quando iniciado com `--simulador.partida-id=0`.

## Situação da validação

Foi acrescentado um teste de inicialização para conferir o congelamento do histórico, os pontos definidos e os horários da rodada atual. A execução de `bash mvnw test` nesta revisão não chegou à compilação: o ambiente não conseguiu resolver `repo.maven.apache.org` ao tentar baixar o parent do Spring Boot. A demonstração real da API e das rotas precisa ser repetida em uma máquina com acesso ao Maven Central.
